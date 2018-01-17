import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Utils.HostChannel;
import Utils.Logger;
import Utils.MsgPacket;
import Utils.RouteTable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Host {
	public UI ui;

	public ServerSocket serverSocket = null;
	public List<HostChannel> connList = new ArrayList<>();
	public RouteTable routeTable;
	private static Lock lock = new ReentrantLock(true);
	public String localIP = "";

	public static final int LISTENING_PORT = 8000;
	private static SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");

	public Host() throws IOException {
		routeTable = new RouteTable();
		try {
			serverSocket = new ServerSocket(LISTENING_PORT);
			// 获得本机IP
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));

			System.out.println("please input your IP address");
			localIP = br.readLine();
			ui = new UI(this);
			ui.setLocalIP("本机IP:" + localIP);
			// 监听邻居节点的连接请求
			while (true) {
				Socket socket = serverSocket.accept();
				ui.appendLogInfo(sd.format(new Date()) + " / " + "main" + ": " + "about connecting to " + socket.getInetAddress());
				Logger.i("main",
						"about connecting to " + socket.getInetAddress());
				new ConnRequestHandler(socket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}
	}

	/**
	 * 广播路由表 该方法同一时刻只会被一个线程调用
	 * 
	 * @param address
	 *            `IP:port`
	 */
	public synchronized void broadcast() {
		synchronized (this) {
			for (HostChannel hc : connList) {
				try {
					RouteTable rt = routeTable.deepClone();
					hc.sendRouteTable(rt);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 广播路由表 该方法同一时刻只会被一个线程调用
	 * 
	 * @param address
	 *            `IP:port`
	 */
	public synchronized void sendMessagePacket(MsgPacket msgPacket) {
		synchronized (this) {
			if (msgPacket.getDesIP().equals(localIP)) {
				ui.appendLogInfo(msgPacket.getMessage());
				return;
			}
			String nextIp = routeTable.getNextRouteAddress(msgPacket, localIP, connList); // 得到下一跳路由
			for (HostChannel hc : connList) { // 向下一跳路由发送msgPacket
				try {
					String temString = hc.getIP();
					if (temString.charAt(0) == '/') {
						temString = temString.substring(1);
					}
					if (nextIp.equals(temString)) {
						hc.sendMessage(msgPacket);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 处理邻居节点的连接请求
	 */
	private class ConnRequestHandler extends Thread {
		private HostChannel neighbor;

		public ConnRequestHandler(Socket socket) {
			try {
				neighbor = new HostChannel(socket);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			ui.appendLogInfo(sd.format(new Date()) + " / " + "requestHandler" + ": " + "setup connection with " + neighbor.getAddress());
			Logger.i("requestHandler",
					"setup connection with " + neighbor.getAddress());
			connList.add(neighbor);
			start();
		}

		@Override
		public void run() {
			try {
				MsgPacket msgPacket = null;
				while (true) {
					msgPacket = (MsgPacket) neighbor.getOis().readObject();
					if (msgPacket != null) {
						if (msgPacket.getType() == 0) { // 收到的是路由表
							ui.appendRouteInfo(sd.format(new Date()));
							ui.appendRouteInfo(" / Receive from " + neighbor.getIP() + ", text " + msgPacket.getRouteTable().toString());
							Logger.logRouteTable(msgPacket.getRouteTable(),
									neighbor.getIP());
							// 如果路由表有改动，广播新路由表
							lock.lock();
							boolean isChanged = routeTable
									.updateTable(msgPacket.getRouteTable());
							lock.unlock();
							if (isChanged) {
								broadcast();
							}


							// Logger.logRouteTable(routeTable);
						} else { // 收到的是信息包
							sendMessagePacket(msgPacket);
							ui.appendLogInfo(sd.format(new Date()));
							ui.appendLogInfo(" /packet Receive from " + neighbor.getIP() + ", text " + msgPacket.toString());
							Logger.logMsgPacket(msgPacket, neighbor.getIP());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					neighbor.close();
					connList.remove(neighbor);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 向邻居请求连接
	 */
	private class ConnRequest extends Thread {
		private HostChannel neighbor;

		public ConnRequest(String IP, int distance) {
			try {
				Socket socket = new Socket(IP, LISTENING_PORT);
				lock.lock();
				routeTable.updateTable(new RouteTable(localIP, IP, distance));
				lock.unlock();
				neighbor = new HostChannel(socket, distance);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			ui.appendLogInfo(sd.format(new Date()) + " / " + "connRequest" + ": " + "setup connection with " + neighbor.getAddress());
			Logger.i("connRequest",
					"setup connection with " + neighbor.getAddress());
			connList.add(neighbor);
			start();
		}

		@Override
		public void run() {
			try {
				// 向邻居发送初始化的链路信息
				broadcast();

				MsgPacket msgPacket = null;
				while (true) {
					msgPacket = (MsgPacket) neighbor.getOis().readObject();
					if (msgPacket != null) {
						if (msgPacket.isRouteTablePacket()) { // 收到的是路由表
							ui.appendRouteInfo(sd.format(new Date()));
							ui.appendRouteInfo(" / Receive from " + neighbor.getIP() + ", text " + msgPacket.getRouteTable());
							Logger.logRouteTable(msgPacket.getRouteTable(),
									neighbor.getIP());
							// 如果路由表有改动，广播新路由表
							lock.lock();
							boolean isChanged = routeTable
									.updateTable(msgPacket.getRouteTable());
							lock.unlock();
							if (isChanged) {
								broadcast();
							}
							// Logger.logRouteTable(routeTable);
						} else if (msgPacket.isStringPacket()){ // 收到的是信息包
							sendMessagePacket(msgPacket);
							ui.appendLogInfo(sd.format(new Date()));
							ui.appendLogInfo(" /packet Receive from " + neighbor.getIP() + ", text " + msgPacket.toString());
							Logger.logMsgPacket(msgPacket, neighbor.getIP());
						}
					}
				}
			} catch (Exception e) {
				System.out.println(connList.size());
				e.printStackTrace();
			} finally {
				try {
					neighbor.close();
					connList.remove(neighbor);
					// broadcast()
					// 监听路由表
					System.out.println(routeTable.toString());
					System.out.println(connList.size());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void createConnet(String IP, int distance) {
		new ConnRequest(IP, distance);
	}

	public static void main(String[] args) throws IOException {
		new Host();
	}

}

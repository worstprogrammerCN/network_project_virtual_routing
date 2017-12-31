package Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class RouteTable implements Serializable {
	// ����ֵ�� `IP:port`
	private Map<String, Map<String, Integer>> table;

	// 16��Ϊ���ɴ�
	public static final int INF = 16;

	public RouteTable() {
		table = new HashMap<>();
	}

	public RouteTable(String addr1, String addr2, Integer distance) {
		table = new HashMap<>();
		addVertex(addr1);
		addVertex(addr2);
		table.get(addr1).put(addr2, distance);
		table.get(addr2).put(addr1, distance);
	}

	public Map<String, Map<String, Integer>> getTable() {
		return table;
	}

	private void addVertex(String address) {
		Map<String, Integer> newEntry = new HashMap<>();
		for (String addr : table.keySet()) { // addr�� address֮��ľ��붼����ΪINF
			newEntry.put(addr, INF);
			table.get(addr).put(address, INF);
		}
		newEntry.put(address, 0); // ������ľ���Ϊ0
		table.put(address, newEntry);
	}

	public boolean updateTable(RouteTable rt) {
		boolean changed = false;
		Map<String, Map<String, Integer>> otherTable = rt.getTable();
		// ������Ĵ�С
		for (String key : otherTable.keySet()) {
			if (!table.keySet().contains(key)) {
				addVertex(key);
				changed = true;
			}
		}
		// ����·��
		for (String addr1 : otherTable.keySet()) {
			for (String addr2 : otherTable.get(addr1).keySet()) {
				Integer distance = otherTable.get(addr1).get(addr2);
				// �и��ŵ�·��
				if (distance < table.get(addr1).get(addr2)) {
					table.get(addr1).put(addr2, distance);
					changed = true;
				}
			}
		}

		// ȷ���Ƿ�Ϊ���ž���  floyd�㷨���������ڵ�֮�����̾���   //LS
		Set<String> hosts = table.keySet();
		for (String a : hosts) {
			for (String b : hosts) {
				if (a.equals(b))
					continue;
				for (String c : hosts) {
					if (b.equals(c))
						continue;
					if (table.get(a).get(b) + table.get(b).get(c) < table
							.get(a).get(c)) {
						table.get(a).put(c,
								table.get(a).get(b) + table.get(b).get(c));
						changed = true;
					}
				}
			}
		}

		return changed;
	}

	public String getNextRouteAddress(MsgPacket msgPacket, String currentIp,
			List<HostChannel> connList) {		// ����õ�Ҫ�������Ŀ��IP����Ҫ��������һ��·��
		String resIP = "";
		int minDis = Integer.MAX_VALUE;
		Logger.logMsgPacket(msgPacket);
		System.out.println("finding best next hop for msgPacket");
		for (int i = 0; i < connList.size(); i++) { // Ѱ�Ҿ�����̵��м̵�
			HostChannel neg = connList.get(i);
			String negIp = neg.getIP();
			int disFromHereToNeg = neg.getDistance();
			int disFromNegToDes = table.get(negIp).get(msgPacket.getDesIP());
			int curDis = disFromHereToNeg + disFromNegToDes;
			System.out.println("current distance sum is " + curDis +
					"(" + disFromHereToNeg + " + " + disFromNegToDes + ")");
			if (minDis > curDis) {
				minDis = curDis;
				resIP = negIp;
			}
		}
		System.out.println("best next hop is " + resIP + " with distance of " + minDis);
		return resIP;
	}

	@Override
	public String toString() {
		String str = "\n==================================\n";
		boolean isTableHead = true;
		for (String addr1 : table.keySet()) {
			if (isTableHead) {
				// �����ͷ
				isTableHead = false;
				for (String addr2 : table.get(addr1).keySet()) {
					str += "\t" + addr2;
				}
				str += "\n";
			}
			str += addr1 + ":";
			for (String addr2 : table.get(addr1).keySet()) {
				str += "\t" + table.get(addr1).get(addr2);
			}
			str += "\n";
		}
		str += "==================================\n";
		return str;
	}

	public RouteTable deepClone() {
		RouteTable clone = new RouteTable();
		clone.updateTable(this);
		return clone;
	}
}

package Utils;

import java.io.Serializable;
import java.util.Map;

public class MsgPacket implements Serializable {
	public static int ROUTE_TABLE_PACKET = 0;
	public static int STRING_PACKET = 1;
	private RouteTable routeTable;
	private String message;
	private int distance;
	// 信息包的类型 0 :转发路由表  1：转发信息包  2:邻接distance信息传递
	private int type;
	private String srcIP;
	private String desIP;
	
	public MsgPacket() {
		routeTable = new RouteTable();
		message = "";
		type = -1;
		srcIP = "";
		desIP = "";
	}
	
	public MsgPacket(RouteTable rt, String msg, int type) { // routeTable只需要被 广播， 不用设定srcIP与desIP
		this.routeTable = rt;
		this.message = msg;
		this.type = type;
	}
	
	public MsgPacket(String msg, int type, String srcIP, String desIP) {
		this.routeTable = new RouteTable();
		this.message = msg;
		this.type = type;
		this.srcIP = srcIP;
		this.desIP = desIP;
	}
	
	public MsgPacket(int distance, int type, String srcIP, String desIP) {
		this.distance = distance;
		this.type = type;
		this.srcIP = srcIP;
		this.desIP = desIP;
	}
	
	public void setRouteTable(RouteTable rt) {
		routeTable = rt.deepClone();
	}
	
	public RouteTable getRouteTable() {
		return routeTable;
	}
	
	public void setMessage(String msg) {
		message = msg;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setSrcIP(String ip) {
		srcIP = ip;
	}
	
	public String getSrcIP() {
		return message;
	}
	
	public void setDesIP(String ip) {
		desIP = ip;
	}
	
	public String getDesIP() {
		return desIP;
	}
	
	public void setType(int _type) {
		type = _type;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isRouteTablePacket () {
		return this.type == ROUTE_TABLE_PACKET;
	}
	
	public boolean isStringPacket () {
		return this.type == STRING_PACKET;
	}
	
    @Override
    public String toString() {
        String str = "\n==================================\n";
        boolean isTableHead = true;
        if (type == ROUTE_TABLE_PACKET) {
        	str = "from: [" + srcIP + "] to [" + desIP + "]\nmessage: ";
			str += message;
	        str += "\n==================================\n";
        	return str;
		}
        // is string packet
        Map<String, Map<String, Integer>> table = routeTable.getTable();
        for (String addr1: table.keySet()) {
            if (isTableHead) {
                // 插入表头
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
}

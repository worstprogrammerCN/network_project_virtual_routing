package Utils;

import java.io.Serializable;
import java.util.Map;

public class MsgPacket implements Serializable {
	private RouteTable routeTable;
	private String message;
	// type: 0 转发路由表 1：转发信息包	
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
	
	public MsgPacket(RouteTable rt, String msg, int _type) {
		routeTable = rt;
		message = msg;
		type = _type;
	}
	
	public MsgPacket(String msg, int _type, String _srcIP, String _desIP) {
		routeTable = new RouteTable();
		message = msg;
		type = _type;
		srcIP = _srcIP;
		desIP = _desIP;
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
	
    @Override
    public String toString() {
        String str = "\n==================================\n";
        boolean isTableHead = true;
        if (type == 1) {
        	str = "from: [" + srcIP + "] to [" + desIP + "]\nmessage: ";
			str += message;
	        str += "\n==================================\n";
        	return str;
		}
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

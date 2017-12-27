package center_routing;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Set;

// routing = "dst=x.x.x.x&next=x.x.x.x&cost=x"
public class RoutingTable {
	
    Map<String, Map<String, RoutingDetail>> routingTable;
    
    public class RoutingDetail {
        private String next;
        private int cost;

        public void setNext(String next) {
            this.next = next;
        }
        public String getNext() {
            return next;
        }
        public void setCost(int cost) {
            this.cost = cost;
        }
        public int getCost() {
            return cost;
        }
        public RoutingDetail() {
            this.next = "255.255.255.255";
            this.cost = 16;
        }
        public RoutingDetail(String next, int cost) {
            this.next = next;
            this.cost = cost;
        }
        public void setDetail(String next, int cost) {
            this.next = next;
            this.cost = cost;
        }
        public void setDetail(RoutingDetail detail) {
            this.next = detail.next;
            this.cost = detail.cost;
        }
        public String toString() {
            return "next=" + next + "&cost=" + Integer.toString(cost);
        }
    }

    RoutingTable() {
        routingTable = null;
    }
    
    RoutingTable(String routingTable) {
        parseRoutingTable(routingTable);
    }

    public Map<String, Map<String, RoutingDetail>> getRoutingTable() {
        return routingTable;
    }

    public int size() {
        if (routingTable == null) return 0;
        return routingTable.size();
    }

    public boolean checkIP(String ip) {
        StringTokenizer st = new StringTokenizer(ip, ".");
        if (st.countTokens() != 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            int seg;
            try {
                seg = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (seg < 0 || seg > 255) {
                return false;
            }
        }
        return true;
    }

    public Map<String, Map<String, RoutingDetail>> parseRoutingTable(
        String routingTable) {
        Map<String, Map<String, RoutingDetail>> map = new HashMap<>();
        StringTokenizer st = new StringTokenizer(routingTable, "?\\$");
        if (st.countTokens() % 2 != 0) {
            return null;
        }
        while (st.hasMoreTokens()) {
            String head = st.nextToken();
            int index = head.indexOf("=");
            String src;
            if (index == -1) {
                return null;
            }
            if (!head.substring(0, index).equals("src")) {
                return null;
            }
            src = head.substring(index + 1);
            if (!checkIP(src)) {
                return null;
            }
            Map<String, RoutingDetail> singleTable =
                parseSingleTable(st.nextToken());
            if (singleTable == null) {
                return null;
            }
            map.put(src, singleTable);
        }
        this.routingTable = map;
        return map;
    }
    
    public Map<String, RoutingDetail> parseSingleTable(String singleTable) {
        Map<String, RoutingDetail> map = new HashMap<>();
        StringTokenizer st = new StringTokenizer(singleTable, "=&|");
        if (st.countTokens() % 6 != 0) {
            return null;
        }
        while (st.hasMoreTokens()) {
            String label_dst = st.nextToken();
            String dst = st.nextToken();
            String label_next = st.nextToken();
            String next = st.nextToken();
            String label_cost = st.nextToken();
            String dist = st.nextToken();
            int cost;
            try {
                cost = Integer.parseInt(dist);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if (!label_dst.equals("dst")
                    || !label_next.equals("next")
                    || !label_cost.equals("cost")
                    || !checkIP(dst)
                    || !checkIP(next)) {
                return null;
            }
            map.put(dst, new RoutingDetail(next, cost));
        }
        return map;
    }

    public String singleTableToString(Map<String, RoutingDetail> singleTable) {
        String singleTableString = "";
        boolean isFirst = true;
        Set<String> dstSet = singleTable.keySet();
        for (String dst : dstSet) {
            if (isFirst) {
                isFirst = false;
            } else {
                singleTableString += "|";
            }
            singleTableString += "dst=" + dst + "&" + singleTable.get(dst).toString();
        }
        return singleTableString;
    }

    public String routingTableToString(
        Map<String, Map<String, RoutingDetail>> routingTable) {
        String routingTableString = "";
        boolean isFirst = true;
        Set<String> srcSet = routingTable.keySet();
        for (String src : srcSet) {
            if (isFirst) {
                isFirst = false;
            } else {
                routingTableString += "$";
            }
            routingTableString += "src=" + src + "?" + singleTableToString(routingTable.get(src));
        }
        return routingTableString;
    }

    public String toString() {
        return routingTableToString(routingTable);
    }
}
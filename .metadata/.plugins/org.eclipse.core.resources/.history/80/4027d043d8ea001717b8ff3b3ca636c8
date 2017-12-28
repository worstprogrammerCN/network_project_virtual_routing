package center_routing;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class Controller {
    private JFrame frame;
    private JPanel portPanel;                    
    private JTextField portField;
    private JButton switchButton;
    private JSplitPane mainPanel;
    private JScrollPane HostListPanel;              // Show online hosts
    private DefaultListModel<String> listModel;     // Keep update online host list
    private JList<String> memberList;
    private JPanel talkPanel;
    private JScrollPane mesPanel;                   // Show system message 
    private JTextArea mesField = null;
    private boolean active;                         // Whether controller is started up
    private int MAX = 5;                            // Max amount of online host
    private int PORT;
    private int CONTROL_PORT = 6666;              
    private int INFINITY = 16;
    private ServerSocket welcomeSocket;             // Listen for host
    private ControllerThread controllerThread;      // Thread to run controller
    private ArrayList<HostThread> hostThreads;    // Thread list of online host
    private RoutingTable route = new RoutingTable();
    private boolean modifying = false;
    
    Map<String, Map<String, RoutingTable.RoutingDetail>> routingTable = new HashMap<>();
    ArrayList<String> nodes = new ArrayList<>();  // All online nodes
    Map<String, Map<String, Integer>> linksCost = new HashMap<>();

    public static void main(String[] args) {
        new Controller();
    }

    public Controller() {
        active = false;
        frame = new JFrame("Controller");
        portPanel = new JPanel();
        portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.X_AXIS));
        portField = new JTextField(CONTROL_PORT + "");
        switchButton = new JButton("Start");
        portPanel.add(switchButton);
        listModel = new DefaultListModel<>(); 
        memberList = new JList<>(listModel);
        HostListPanel = new JScrollPane(memberList);
        HostListPanel.setBorder(new TitledBorder("Hosts"));
        mesField = new JTextArea();
        mesField.setEditable(false);
        mesPanel = new JScrollPane(mesField);

        talkPanel = new JPanel();
        talkPanel.setLayout(new BorderLayout());
        talkPanel.add(mesPanel, BorderLayout.CENTER);
        talkPanel.setBorder(new TitledBorder("Message"));
        
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, talkPanel,  HostListPanel);
        mainPanel.setDividerLocation(450);

        frame.setLayout(new BorderLayout());
        frame.add(portPanel, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setSize(700, 500);
        frame.setVisible(true);

        // Close window
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (active) stopController();
                System.exit(0);
            }
        });
        
        switchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchButton.setEnabled(false);
                if (!active) {
                    mesField.append("Starting controller...\n");
                    try {
                        try {
                            PORT = Integer.parseInt(portField.getText());
                        } catch (Exception exception) {
                            throw new Exception("Invalid Port");
                        }
                        if (PORT <= 0) throw new Exception("Invalid Port");
                        startController();
                        portField.setEnabled(false);
                        mesField.append("Controller starts successfully!\n\n");
                        switchButton.setText("Shutdown");
                    } catch (Exception exception) {
                        switchButton.setEnabled(true);
                        mesField.append("Fail to start Controller!\n\n");
                    }
                } else {
                    mesField.append("Shutting down controller...\n");
                    try {
                        stopController();
                        portField.setEnabled(true);
                        mesField.append("Controller shut down successfully!\n\n");
                        switchButton.setText("Start");
                    } catch (Exception exception) {
                        switchButton.setEnabled(true);
                        mesField.append("Fail to shut down Controller!\n\n");
                    }
                }
                switchButton.setEnabled(true);
            }
        });
    }

    private void startController() throws BindException {
        try {
            hostThreads = new ArrayList<>();
            welcomeSocket = new ServerSocket(PORT);
            controllerThread = new ControllerThread(welcomeSocket, MAX);
            controllerThread.start();
            active = true;
        } catch (Exception e) {
            active = false;
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void stopController() {
        try {
            if (controllerThread != null) controllerThread.stop();
            for (int i = hostThreads.size() - 1; i >= 0; i--) {
                hostThreads.get(i).getWriter().println("CLOSE");
                hostThreads.get(i).getWriter().flush();
                hostThreads.get(i).stop();
                hostThreads.get(i).getReader().close();
                hostThreads.get(i).getWriter().close();
                hostThreads.get(i).connectionSocket.close();
                hostThreads.remove(i);
            }
            if (welcomeSocket != null) {
                welcomeSocket.close();
            }
            listModel.removeAllElements();
            active = false;
        } catch (IOException e) {
            e.printStackTrace();
            active = true;
        }
    }

    // Process connection request from hosts
    private class ControllerThread extends Thread {
        private ServerSocket welcomeSocket;
        private int MAX;

        public ControllerThread(ServerSocket welcomeSocket, int max) {
            this.welcomeSocket = welcomeSocket;
            this.MAX = max;
        }

        public void run() {
            while (true) {
                try {
                    Socket connectionSocket = welcomeSocket.accept();
                    if (hostThreads.size() >= MAX) {
                        BufferedReader inFromMember = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                        PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream());
                        String clientSentence = inFromMember.readLine();
                        outToClient.println("Sorry!Too many hosts currently, try again later\n");
                        outToClient.flush();
                        inFromMember.close();
                        outToClient.close();
                        connectionSocket.close();
                        continue;
                    }
                    HostThread memberThread = new HostThread(connectionSocket);
                    if (memberThread != null && memberThread.getActive()) {
                        memberThread.start();
                        hostThreads.add(memberThread);
                        listModel.addElement(memberThread.getIP());
                        mesField.append(memberThread.getIP() + " is online!\n\n");
                    } 
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class HostThread extends Thread {
        private Socket connectionSocket;
        private BufferedReader inFromMember;
        private PrintWriter writer;
        private String ip;
        private boolean at = false;
        public Socket getSocket() { return connectionSocket; }
        public BufferedReader getReader() { return inFromMember; }
        public PrintWriter getWriter() { return writer; }
        public String getIP() { return ip; }

        public boolean getActive() {
            return at;
        }

        @SuppressWarnings("deprecation")
        public HostThread(Socket socket) {
            try {
                this.connectionSocket = socket;
                inFromMember = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());

                ip = inFromMember.readLine();
                //System.out.println(ip + " connection successfully!");
                writer.println(ip + " connection successfully!");
                writer.flush();
                Map<String, Integer> newLinkCost = new HashMap<>();
                newLinkCost.put(ip, 0);
                for (String node : nodes) {
                    newLinkCost.put(node, INFINITY);
                }
                updateLinksCost(ip, newLinkCost);
                // Send list of online clients to new connection
                String temp = "MEMBERLIST$";
                for (int i = hostThreads.size() - 1; i >= 0; i--) {
                    if (i != hostThreads.size() - 1) {
                        temp += "&";
                    }
                    temp += hostThreads.get(i).getIP();
                }
                writer.println(temp);
                writer.flush();
                // Tell other online clients to update their list of online clients
                for (int i = hostThreads.size() - 1; i >= 0; i--) {
                    hostThreads.get(i).getWriter().println(
                            "ADD$" + ip);
                    hostThreads.get(i).getWriter().flush();
                }
                at = true;  
                // Tell other online hosts to update their list of online hosts
            } catch (Exception e) {
                at = false;
                stop();
                e.printStackTrace();
            }
        }

        @SuppressWarnings("deprecation")
        public void run() {
            String message = null;
            while (true) {
                try {
                    message = inFromMember.readLine();
                    handleMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        // handle the message
        @SuppressWarnings("deprecation")
        public void handleMessage(String message) {
        	String mode = getMode(message);
        	switch (mode) {
        		case "CLOSE":				// Host disconnects
                    try {
                        mesField.append(ip + " is offline!\n\n");
                        inFromMember.close();
                        writer.close();
                        connectionSocket.close();
                        for (int i = hostThreads.size() - 1; i >= 0; i--) {
                            if (hostThreads.get(i).getIP() != ip) {
                                hostThreads.get(i).getWriter().println("DELETE$" + ip);
                                hostThreads.get(i).getWriter().flush();
                            }
                        }
                        Map<String, Integer> newLinkCost = new HashMap<>();
                        for (String node : nodes) {
                            newLinkCost.put(node, INFINITY);
                        }
                        updateLinksCost(ip, newLinkCost);
                        updateTable();
                        // Update list of online clients in server
                        listModel.removeElement(ip);
                        routingTable.remove(ip);
                        linksCost.remove(ip);
                        nodes.remove(ip);
                        for (String node : nodes) {
                            routingTable.get(node).remove(ip);
                            linksCost.get(node).remove(ip);
                        }
                        distributeTable(ip);
                        for (int i = hostThreads.size() - 1; i >= 0; i--) {
                            if (hostThreads.get(i).getIP() == ip) {
                                HostThread temp = hostThreads.get(i);
                                hostThreads.remove(i);
                                temp.stop();
                            }
                        }
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    
                case "IF_MODIFIED":
                    if (!modifying) {
                        writer.println("NOT_MODIFIED");
                        writer.flush();
                    }
                    break;
                    
        		case "UPDATE":				// Host asks for updating
                    modifying = true;
                    Map<String, Integer> newLinkCost = new HashMap<>();
                    String linkCost = getContent(message);
                    StringTokenizer st = new StringTokenizer(linkCost, "=&|");
                    if (st.countTokens() % 4 != 0) {
                        break;
                    }
                    while (st.hasMoreTokens()) {
                        String label = st.nextToken();
                        String dst;
                        int cost;
                        if (!label.equals("dst")) {
                            break;
                        }
                        dst = st.nextToken();
                        if (!route.checkIP(dst)) {
                            break;
                        }
                        label = st.nextToken();
                        if (!label.equals("cost")) {
                            break;
                        }
                        try {
                            cost = Integer.parseInt(st.nextToken());
                        } catch (Exception e) {
                            break;
                        }
                        newLinkCost.put(dst, cost);
                    }
                    updateLinksCost(ip, newLinkCost);
        			updateTable();
        			distributeTable();
                    modifying = false;
        			break;
        		default:
        			break;
        	}
        }
        private String getContent(String message) {
            if (message == null || "".equals(message)) {
                return "";
            }
            int index = message.indexOf("$");
            if (index == -1) {
                return "";
            }
            return message.substring(index + 1);
        }
        private String getMode(String message) {
            if (message == null || "".equals(message)) {
                return "";
            }
            int index = message.indexOf("$");
            if (index == -1) {
                return message;
            }
            return message.substring(0, index);
        }
    }

    public class DpPair {
        private int dist;
        private String prev;
        public DpPair(int dist, String prev) {
            this.dist = dist;
            this.prev = prev;
        }
        public void setPair(int dist, String prev) {
            this.dist = dist;
            this.prev = prev;
        }
        public void setDistance(int dist) {
            this.dist = dist;
        }
        public void setPrevious(String prev) {
            this.prev = prev;
        }
        public int getDistance() {
            return dist;
        }
        public String getPrevious() {
            return prev;
        }
    }
    
    public int getNeighbor(int[] old, int[] newState) {
    	return 0;
    }
    
    public boolean isNeighbor(String one, String other) {
        if (one.equals(other)) {
            return false;
        }
        if (linksCost.get(one).get(other) < INFINITY) {
            return true;
        }
        return false;
    }
    public ArrayList<String> getNeighbor(String src) {
        ArrayList<String> neighborList = new ArrayList<>();
        for (String node : nodes) {
            if (isNeighbor(src, node)) {
                neighborList.add(node);
            }
        }
        return neighborList;
    }
    public void updateTable() {
    	// use Dijkstra algorithm
        for (String u : nodes) {
            Map<String, DpPair> linkState = new HashMap<>();
            ArrayList<String> remaining = new ArrayList<>();
            for (String v : nodes) {
                int cost = linksCost.get(u).get(v);
                if (cost < INFINITY) {  // v is a neighbor of u
                    linkState.put(v, new DpPair(cost, u));  // <v, <D(v), p(v)> = <c(u, v), v>>
                } else {
                    linkState.put(v, new DpPair(INFINITY, "255.255.255.255"));  // <v, <D(v), p(v)> = <INFINITY, null>>
                }
                if (!v.equals(u)) {
                    remaining.add(v);
                }
            }
            String curMinCostNode = null;  // w
            int curMinCost = INFINITY;
            while (remaining.size() > 0) {
                curMinCostNode = null;
                curMinCost = INFINITY;
                for (String node : remaining) { // find w not in N' such that D(w) is a minimum
                    if (curMinCostNode == null) {
                        curMinCostNode = node;
                    }
                    int cost = linkState.get(node).getDistance();
                    if (cost < curMinCost) {
                        curMinCost = cost;
                        curMinCostNode = node;
                    }
                }
                remaining.remove(curMinCostNode);
                ArrayList<String> neighborsOfMinCostNode
                    = getNeighbor(curMinCostNode);
                for (String neighbor : neighborsOfMinCostNode) {
                    int distThroughMinCostNodeToNeighbor
                        = curMinCost + linksCost.get(curMinCostNode).get(neighbor);
                    if (linkState.get(neighbor).getDistance()
                            > distThroughMinCostNodeToNeighbor) {
                        linkState.put(neighbor,
                            new DpPair(distThroughMinCostNodeToNeighbor,
                                curMinCostNode));
                    }
                }
            }
            for (String node : nodes) {
                if (node.equals(u)) {
                    continue;
                }
                String current = node;
                String previous = linkState.get(node).getPrevious();
                while (!previous.equals(u)) {
                    current = previous;
                    if (previous.equals("255.255.255.255")) {
                        break;
                    }
                    previous = linkState.get(current).getPrevious();
                }
                routingTable.get(u).get(node).setDetail(current, linkState.get(node).getDistance());
                routingTable.get(node).get(u).setDetail(
                    linkState.get(node).getPrevious(), linkState.get(node).getDistance());
            }
        }
    }

    public void updateLinksCost(String src, Map<String, Integer> newLinkCost) {
        routingTable.put(src, new HashMap<String, RoutingTable.RoutingDetail>());
        for (String node : nodes) {
            if (!src.equals(node)) {
                linksCost.get(node).put(src, newLinkCost.get(node));
                routingTable.get(node).put(src, new RoutingTable().new RoutingDetail());
                routingTable.get(src).put(node, new RoutingTable().new RoutingDetail());
            }
        }
        if (!nodes.contains(src)) {
            nodes.add(src);
        }
        linksCost.put(src, newLinkCost);
    }

    public void distributeChange(int neighbor, int[] newState) {
        for (int i = hostThreads.size() - 1; i >= 0; i--) {
            String src = hostThreads.get(i).getIP();
            String out = "UPDATELS$";
            boolean first = true;
            for (String node : nodes) {
                if (!node.equals(src)) {
                    if (first) {
                        first = false;
                    } else {
                        out += "|";
                    }
                    out += "dst=" + node + "&cost=" + linksCost.get(src).get(node);
                }
            }
            hostThreads.get(i).getWriter().println(out);
            hostThreads.get(i).getWriter().flush();
        }
    }
    public void distributeTable() {
        for (int i = hostThreads.size() - 1; i >= 0; i--) {
            String src = hostThreads.get(i).getIP();
            hostThreads.get(i).getWriter().println("UPDATE$"+route.singleTableToString(routingTable.get(src)));
            hostThreads.get(i).getWriter().flush();
        }
    }
    public void distributeTable(String except) {
        for (int i = hostThreads.size() - 1; i >= 0; i--) {
            String src = hostThreads.get(i).getIP();
            if (!src.equals(except)) {
                hostThreads.get(i).getWriter().println("UPDATE$"+route.singleTableToString(routingTable.get(src)));
                hostThreads.get(i).getWriter().flush();
            }
        }
    }

}
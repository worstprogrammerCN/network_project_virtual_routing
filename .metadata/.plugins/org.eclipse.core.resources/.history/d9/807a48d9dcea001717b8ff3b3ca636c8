package center_routing;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class Host {

    private JFrame frame;
    private JPanel controllerMesPanel;                 
    private JLabel ipLabel;
    private JLabel pingLabel;
    private JTextField ipField;
    private JButton switchButton;
    private JSplitPane mainPanel;
    
    
    // 由{@code linkStateMiddlePanel}和{@code routingRightPanel}横向排列而成
    // 包含了收敛后本节点到其它节点的路由策略， 以及加入这个路由拓扑的host的信息
    private JSplitPane routingPanel;
    private JSplitPane routingRightPanel; // 显示加入这个路由拓扑的host的信息
    private JScrollPane linkStateMiddlePanel; // 显示经过收敛，本节点与 其它节点之间的cost，以及到该节点需要经过的下一跳路由
    
    
    // 由{@code mesPanel}与{@code pingPanel}纵向排列而成
    // 显示调试信息， 以及提供ping其它节点的功能
    private JPanel talkPanel;
    
    private JScrollPane mesPanel; // 显示调试信息，比如路由表更新， 本节点收到信息，等等
    private JTextArea mesField; // 依附于{@code mesPanel}

    // 用于ping其它host
    private JPanel pingPanel;
    private JTextField pingField; // 用于输入要ping的其它节点的ip
    private JButton pingButton; // 点击后向其它节点尝试发送ping
    
    // 用于储存界面右边栏每一行的信息
    // 每个{@code LinkStateItem}是一个其它的节点与本节点之间的关系
    private Vector<LinkStateItem> rows;  
    private JList<LinkStateItem> linkState;
    
    // 界面右下方的"Update!"按钮 
    // 点击后会把自己与其它节点是否相连的情况进行广播
    private JButton sendNewLinksStateButton;
    
    // 界面中间的路由信息面板
    // 储存了经过收敛后，本节点与其它节点之间的cost,
    private JScrollPane routingMessageField;
    
    // 是{@code routingMessageField}的Model
    // 每一项都是一个String, 类似于"{@code dest}  {@code next}  {@code cost}"的形式
    private DefaultListModel<String> listModel;
    private JList<String> routingMessage;
    
    // Whether connect to a controller
    private boolean active;

    // controller的 ip
    private String CONTROL_IP;
    
    // 一个{@code ServerSocket}， 接受监听来自其它节点的信息 
    private ServerSocket welcomeSocket;
    
    // 用于向controller交流信息
    private Socket memberSocket;
    
    // 用于读取来自controller的流
    private BufferedReader inFromController;
    // 用于 将流发送给controller
    private PrintWriter outToController;
    
    private MessageThread messageThread;
    //private int MAX = 5;                           // Max amount of online host
    private String path= "";
    
    // controller的port
    private int CONTROL_PORT = 6666;
    private int MEMBER_PORT = 7777;
    private int INFINITY = 16;

    RoutingTable route = new RoutingTable();
    Map<String, RoutingTable.RoutingDetail> routingTable = null;
    String localIP = "";
    PingThread pingThread;
    boolean checking = false;
 
    public static void main(String[] args) {
        new Host();
    }

    public Host() {
        active = false;
        frame = new JFrame("Host");
        controllerMesPanel = new JPanel();
        controllerMesPanel.setLayout(new BoxLayout(controllerMesPanel, BoxLayout.X_AXIS));
        ipLabel = new JLabel("Controller IP Address");
        ipField = new JTextField("172.18.71.17");
        switchButton = new JButton("Connect");
        controllerMesPanel.add(Box.createHorizontalStrut(12));
        controllerMesPanel.add(ipLabel);
        controllerMesPanel.add(Box.createHorizontalStrut(6));
        controllerMesPanel.add(ipField); 
        controllerMesPanel.add(Box.createGlue());
        controllerMesPanel.add(Box.createHorizontalStrut(28));
        controllerMesPanel.add(switchButton);

        rows = new Vector<>();
        linkState = new JList<>(rows);
        linkState.setCellRenderer(new ListCellRenderer<LinkStateItem>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends LinkStateItem> list, LinkStateItem value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                return value;
            }
        });
        
        linkState.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (rows.isEmpty()) {
                    return;
                }
                int index = linkState.locationToIndex(e.getPoint());
                if (index == -1) {
                    return;
                }
                LinkStateItem item = rows.get(index);
                //left key of mouse is clicked
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    item.checkBox.setSelected(!item.checkBox.isSelected());
                    item.field.setEditable(item.checkBox.isSelected());
                    linkState.updateUI();
                } else { //right key of mouse is clicked
                    if (item.checkBox.isSelected()) {
                        SimpleDialog dialog = new SimpleDialog(linkState, item.field, item.label2);
                        dialog.setVisible(true);
                    }
                }
            }
        });
        
        linkStateMiddlePanel = new JScrollPane(linkState);
        linkStateMiddlePanel.setBorder(new TitledBorder("Links State (NeighborOrNot / Dest / Cost)"));
        sendNewLinksStateButton = new JButton("Update!");
        sendNewLinksStateButton.setEnabled(false);
        routingRightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, linkStateMiddlePanel, sendNewLinksStateButton);
        routingRightPanel.setDividerLocation(360);
        
        listModel = new DefaultListModel<>();
        routingMessage = new JList<>(listModel);
        routingMessageField = new JScrollPane(routingMessage);
        routingMessageField.setBorder(new TitledBorder("Routing Table (Dest / NextHop / Cost)"));

        routingPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, routingMessageField, routingRightPanel);
        routingPanel.setDividerLocation(240);

        mesField = new JTextArea();
        mesField.setEditable(false);
        mesPanel = new JScrollPane(mesField);
        
        pingLabel = new JLabel(" Ping IP Address ");
        pingPanel = new JPanel();
        pingPanel.setLayout(new BorderLayout());
        pingField = new JTextField();
        pingButton = new JButton("Ping");
        pingButton.setEnabled(false);
        	
        pingPanel.add(pingLabel, BorderLayout.WEST);
        pingPanel.add(pingField, BorderLayout.CENTER);
        pingPanel.add(pingButton, BorderLayout.EAST);

        talkPanel = new JPanel();
        talkPanel.setLayout(new BorderLayout());
        talkPanel.add(mesPanel, BorderLayout.CENTER);
        talkPanel.add(pingPanel, BorderLayout.SOUTH);
        talkPanel.setBorder(new TitledBorder("Message"));
        
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, talkPanel, routingPanel);
        mainPanel.setDividerLocation(300);

        frame.setLayout(new BorderLayout());
        frame.add(controllerMesPanel, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setSize(850, 500);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (active)
                    disconnect();
                System.exit(0);
            }
        });

        switchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchButton.setEnabled(false);
                if (!active) {  // connect to controller
                    try {
                        CONTROL_IP = ipField.getText().trim();
                        if (CONTROL_IP.equals("")) {
                            throw new Exception("IP address cannot be empty!");
                        }
                        if (!route.checkIP(CONTROL_IP)) {
                            throw new Exception("Invalid IP address");
                        }
                        mesField.append("Connecting to the Controller...\n");
                        if (!connect()) {
                            throw new Exception("Failed to connect!");
                        }
                        ipField.setEnabled(false);
                        sendNewLinksStateButton.setEnabled(true);
                        pingButton.setEnabled(true);
                        mesField.append("Connect successfully!\n\n");
                        switchButton.setText("Disconnect");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        switchButton.setEnabled(true);
                        JOptionPane.showMessageDialog(null, "Error: " + exception.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {  // disconnect the controller
                    try {
                        mesField.append("Disonnecting...\n");
                        if (!disconnect()) {
                            throw new Exception("Fail to disconnect!");
                        }
                        ipField.setEnabled(true);
                        pingButton.setEnabled(false);
                        mesField.append("Host disconnects successfully!\n\n");
                        switchButton.setText("Connect");
                    } catch (Exception exception) {
                        switchButton.setEnabled(true);
                        JOptionPane.showMessageDialog(null, "Error: " + exception.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                switchButton.setEnabled(true);
            }
        });

        sendNewLinksStateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendNewLinksState();
            }
        });
        // Click to ping message
        pingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ping(pingField.getText());
            }
        });
        pingField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ping(pingField.getText());
            }
        });
    }

    private void sendNewLinksState() {
        if (outToController == null) {
            return;
        }
        String updateMessage = "UPDATE$dst=" + localIP + "&cost=0";
        int dst_num = rows.size();
        for (int i = 0; i < dst_num; i++) {
            LinkStateItem item = rows.get(i);
            String dst = item.label.getText();
            String cost;
            if (item.checkBox.isSelected()) {
                // cost = item.field.getText();                                    test
            	cost = "1"; //                                                     test
                if (cost.equalsIgnoreCase("inf")) {
                    cost = "" + INFINITY;
                    System.out.println("this PC is neighbour to " + dst);
                } else {
                	System.out.println("this PC is not neighbour to " + dst);
                }
            } else {
                cost = "" + INFINITY;
            }
            updateMessage += "|dst=" + dst + "&cost=" + cost;
        }
        outToController.println(updateMessage);
        outToController.flush();
    }
    
    @SuppressWarnings("deprecation")
    private boolean connect() {
        try {
            if (pingThread == null) {
                welcomeSocket = new ServerSocket(MEMBER_PORT);
                pingThread = new PingThread(welcomeSocket);
                pingThread.start();
            }
            memberSocket = new Socket(CONTROL_IP, CONTROL_PORT);
            outToController = new PrintWriter(memberSocket.getOutputStream());
            inFromController = new BufferedReader(new InputStreamReader(memberSocket.getInputStream()));
            localIP = memberSocket.getLocalAddress().toString();
            System.out.println("local ip " + localIP);
            if (localIP.charAt(0) == '/') {
                localIP = localIP.substring(1);
            }
            outToController.println(localIP);
            outToController.flush();
            String message = "";
            while (true) {
                message = inFromController.readLine();
                StringTokenizer stringTokenizer = new StringTokenizer(
                        message, "/\\$&");
                String command = stringTokenizer.nextToken();
                if (command.equals("MAX")) { // 加入的节点数达到上限
                    mesField.append("Error3: The chatroom is too crowded\n");
                    throw new Exception("Max users");
                } else if (command.equals("MEMBERLIST")) { // 信息有关于其它的成员节点
                    rows.removeAllElements();
                    String memberIP = null;
                    System.out.println("收到来自controller的message: " + message);
                    while (stringTokenizer.hasMoreTokens()) {
                        memberIP = stringTokenizer.nextToken();
                        if (!route.checkIP(memberIP)) {
                            continue;
                        }
                        rows.add(new LinkStateItem(false, memberIP, "INF"));
                    }
                    linkState.updateUI();
                    listModel.removeAllElements();
                    break;
                }
            }
            active = true;
            messageThread = new MessageThread(inFromController, mesField);
            messageThread.start();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
            active = false;
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private boolean disconnect() {
        try {
            outToController.println("CLOSE");
            outToController.flush();
            active = false;
            if (pingThread != null) {
                pingThread.close();
                pingThread.stop();
                pingThread = null;
            }
            if (inFromController != null) inFromController.close();
            if (outToController != null) outToController.close();
            if (memberSocket != null) memberSocket.close();
           
            rows.removeAllElements();
            linkState.updateUI();
            listModel.removeAllElements();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void ping(String dest_IP) {
        if ("clear".equals(dest_IP)) {
            mesField.setText("");
            pingField.setText("");
            return;
        }
        if (!active || pingThread == null) {
            return;
        }
        if (dest_IP.equals(localIP)) {
            return;
        }
        if (!route.checkIP(dest_IP)) {
            JOptionPane.showMessageDialog(null, "Destination ip is invalid!", "error", JOptionPane.ERROR_MESSAGE);
        } else if (memberSocket == null) {
            JOptionPane.showMessageDialog(null, "Not connected to controller!", "error", JOptionPane.ERROR_MESSAGE);
        } else {
            Socket otherSocket = null;
            PrintWriter outToOther = null;
            try {
                String next = routingTable.get(dest_IP).getNext();
                otherSocket = new Socket(next, MEMBER_PORT);
                outToOther = new PrintWriter(otherSocket.getOutputStream());
                outToOther.println("src=" + localIP + "?dst=" + dest_IP + "&data=" + localIP);
                outToOther.flush();
                mesField.append("Send to next node: "+ next + "\n");
                otherSocket.close();
                outToOther.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String[] decodePing(String message) {
        String remain = message;
        String label;
        String[] res = new String[3];
        String[] labels = new String[] {"src", "dst", "data"};
        String[] separators = new String[] {"?", "&"};
        for (int i = 0; i < 3; i++) {
            int index = remain.indexOf("=");
            if (index == -1) {
                return null;
            }
            label = remain.substring(0, index);
            if (!labels[i].equals(label)) {
                return null;
            }
            remain = remain.substring(index + 1);
            if (i == 2) {
                res[i] = remain;
                //System.out.println("ping from " + res[0] + " to " + res[1] + " path: " + res[2]);
                path = "ping from " + res[0] + " to " + res[1];//+ " path: " + res[2];
                return res;
            }
            index = remain.indexOf(separators[i]);
            if (index == -1) {
                return null;
            }
            res[i] = remain.substring(0, index);
            if (!route.checkIP(res[i])) {
                return null;
            }
            remain = remain.substring(index + 1);
        }
        return null;
    }
    
    // Ping thread
    private class PingThread extends Thread {
        private BufferedReader inFromOther;
        private ServerSocket welcomeSocket;
        public PingThread(ServerSocket welcomeSocket) {
            this.welcomeSocket = welcomeSocket;
        }

        public synchronized void close() throws Exception {
            listModel.removeAllElements();
            rows.removeAllElements();
            if (inFromOther != null) inFromOther.close();
            if (welcomeSocket != null) welcomeSocket.close();
        }
        public void run() {
            Socket otherSocket = null;
            PrintWriter outToOther = null;
            while (true) {
                try {
                    Socket connectionSocket = welcomeSocket.accept();
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    String msg = inFromClient.readLine();
                    //System.out.println("host " + msg);
                    String[] ping = decodePing(msg);
                    if (ping == null) {
                        continue;
                    }
                   if (ping[1].equals(localIP)) {
                        mesField.append("\nReceive message:\nFrom: " + ping[0] + "\nTo: " + ping[1] + "\nRouting Path: " + ping[2] + ">>" + localIP + "\n\n");
                    } else {
                        String next = routingTable.get(ping[1]).getNext();
                        otherSocket = new Socket(next, MEMBER_PORT);
                        outToOther = new PrintWriter(otherSocket.getOutputStream());
                        outToOther.println(msg + ">" + localIP);
                        outToOther.flush();
                        mesField.append("Send to next node: "+ next + "\n"+ path + "\n");
                        otherSocket.close();
                        outToOther.close();
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                   
                }
            }
        }
    }

    // Process message from controller
    private class MessageThread extends Thread {
        private BufferedReader inFromController;
        private JTextArea mesField;

        public MessageThread(BufferedReader inFromController, JTextArea mesField) {
            this.inFromController = inFromController;
            this.mesField = mesField;
        }

        public synchronized void close() throws Exception {
            active = false;
            listModel.removeAllElements();
            if (inFromController != null) inFromController.close();
            if (outToController != null) outToController.close();
            if (memberSocket != null) memberSocket.close();
        }

        @SuppressWarnings("deprecation")
        public void run() {
            String message = "";
            while (active) {
                try {
                    message = inFromController.readLine();
                    if (message == null) {
                        continue;
                    }
                    String mode = getMode(message);
                    if ("ADD".equals(mode)) {
                        String memberIP = getContent(message); // 新增的成员的 ip
                        if (!route.checkIP(memberIP)) {
                            continue;
                        }
                        rows.add(new LinkStateItem(false, memberIP, "INF"));
                        linkState.updateUI();
                    } else if ("UPDATE".equals(mode)) {
                        routingTable = route.parseSingleTable(getContent(message));
                        updateRoutingTableField();
                        mesField.append("Update routing table.\n");
                        checking = false;
                    } else if ("DELETE".equals(mode)) {
                        String memberIP = getContent(message);
                        if (!route.checkIP(memberIP)) {
                            continue;
                        }
                        LinkStateItem delete = null;
                        for (LinkStateItem row : rows) {
                            if (row.label.getText().equals(memberIP)) {
                                delete = row;
                            }
                        }
                        if (delete != null) {
                            rows.remove(delete);
                        }
                        System.out.println("DELETE" + memberIP);
                        linkState.updateUI();
                    } else if ("UPDATELS".equals(mode)) {
                        updateLinkStateMiddlePanel(getContent(message));
                        linkState.updateUI();
                    } else if ("NOT_MODIFIED".equals(mode)) {
                        checking = false;
                    } else if ("CLOSE".equals(mode)) {
                        mesField.append("Controller is shut down.\n");
                        if (pingThread != null) {
                            pingThread.close();
                            pingThread.stop();
                            pingThread = null;
                        }
                        if (inFromController != null) inFromController.close();
                        if (outToController != null) outToController.close();
                        if (memberSocket != null) memberSocket.close();
                        active = false;
                        rows.removeAllElements();
                        linkState.updateUI();
                        listModel.removeAllElements();
                        ipField.setEnabled(true);
                        pingButton.setEnabled(false);
                        mesField.append("Host disconnects.\n\n");
                        switchButton.setText("Connect");
                        close();
                        checking = false;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
    }
    
    public String padding(String orign, int size) {
        String res = orign;
        while (res.length() < size) {
            res += " ";
        }
        return res;
    }

    public void updateLinkStateMiddlePanel(String linkCost) {
        Map<String, Integer> newLinkCost = new HashMap<>();
        StringTokenizer st = new StringTokenizer(linkCost, "=&|");
        if (st.countTokens() % 4 != 0) {
            return;
        }
        while (st.hasMoreTokens()) {
            String label = st.nextToken();
            String dst;
            int cost;
            if (!label.equals("dst")) {
                return;
            }
            dst = st.nextToken();
            if (!route.checkIP(dst)) {
                return;
            }
            label = st.nextToken();
            if (!label.equals("cost")) {
                return;
            }
            try {
                cost = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
                return;
            }
            newLinkCost.put(dst, cost);
        }
        rows.removeAllElements();
        boolean isNeighbor;
        Set<String> dstSet = newLinkCost.keySet();
        for (String dst : dstSet) {
            int cost = newLinkCost.get(dst);
            if (cost >= INFINITY) {
                isNeighbor = false;
            } else {
                isNeighbor = true;
            }
            rows.addElement(new LinkStateItem(isNeighbor, dst, cost + ""));
        }
        
    }
    
    public void updateRoutingTableField() {
        if (routingTable == null) {
            return;
        }
        listModel.removeAllElements();
        Set<String> dstSet = routingTable.keySet();
        for (String dst : dstSet) {
            RoutingTable.RoutingDetail detail = routingTable.get(dst);
            listModel.addElement(padding(dst, 16) + padding(detail.getNext(), 16) + detail.getCost());
        }
    }

}

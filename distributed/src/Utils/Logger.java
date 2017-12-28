package Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");

    public static void logRouteTable(RouteTable rt, String IP) {
        System.out.print(sd.format(new Date()));
        System.out.println(" / Receive from " + IP + ", text " + rt.toString());
    }

    public static void logRouteTable(RouteTable rt) {
        System.out.print(sd.format(new Date()));
        System.out.println(" / RouteTable in this host" + rt.toString());
    }

    public static void i(String label, String text) {
        System.out.println(sd.format(new Date()) + " / " + label + ": " + text);
    }
    
    public static void logMsgPacket(MsgPacket msg) {
        System.out.print(sd.format(new Date()));
        System.out.println(" / Message Packet in this host" + msg.toString());
    }
    
    public static void logMsgPacket(MsgPacket msg, String IP) {
        System.out.print(sd.format(new Date()));
        System.out.println(" /packet Receive from " + IP + ", text " + msg.toString());
    }
}

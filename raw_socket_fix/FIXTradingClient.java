import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic FIX Protocol Trading Client for NYSE
 * This implementation demonstrates core FIX message structure and flow
 */
public class FIXTradingClient {
    
    private static final String SOH = "\u0001"; // FIX field delimiter (Start of Header)
    private static final DateTimeFormatter FIX_TIME_FORMAT = 
        DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private AtomicInteger msgSeqNum = new AtomicInteger(1);
    
    // FIX Session Configuration
    private String senderCompID;
    private String targetCompID;
    private String fixVersion = "FIX.4.4"; // NYSE typically uses FIX 4.4 or 4.2
    
    public FIXTradingClient(String senderCompID, String targetCompID) {
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
    }
    
    /**
     * Connect to NYSE FIX Gateway
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        
        // Send Logon message (MsgType=A)
        sendLogon();
    }
    
    /**
     * Send FIX Logon message (MsgType=A)
     */
    private void sendLogon() {
        StringBuilder message = new StringBuilder();
        
        // Standard Header
        message.append("35=A").append(SOH); // MsgType = Logon
        message.append("49=").append(senderCompID).append(SOH); // SenderCompID
        message.append("56=").append(targetCompID).append(SOH); // TargetCompID
        message.append("34=").append(msgSeqNum.getAndIncrement()).append(SOH); // MsgSeqNum
        message.append("52=").append(getCurrentTimestamp()).append(SOH); // SendingTime
        
        // Logon specific fields
        message.append("98=0").append(SOH); // EncryptMethod = None
        message.append("108=30").append(SOH); // HeartBtInt = 30 seconds
        message.append("141=Y").append(SOH); // ResetSeqNumFlag
        
        sendMessage(message.toString());
    }
    
    /**
     * Send New Order Single (MsgType=D)
     */
    public void sendNewOrder(String symbol, String side, double quantity, double price, 
                             String orderType, String timeInForce) {
        StringBuilder message = new StringBuilder();
        String clOrdID = generateOrderID();
        
        // Standard Header
        message.append("35=D").append(SOH); // MsgType = NewOrderSingle
        message.append("49=").append(senderCompID).append(SOH);
        message.append("56=").append(targetCompID).append(SOH);
        message.append("34=").append(msgSeqNum.getAndIncrement()).append(SOH);
        message.append("52=").append(getCurrentTimestamp()).append(SOH);
        
        // Order fields
        message.append("11=").append(clOrdID).append(SOH); // ClOrdID
        message.append("55=").append(symbol).append(SOH); // Symbol
        message.append("54=").append(side).append(SOH); // Side (1=Buy, 2=Sell)
        message.append("60=").append(getCurrentTimestamp()).append(SOH); // TransactTime
        message.append("38=").append((int)quantity).append(SOH); // OrderQty
        message.append("40=").append(orderType).append(SOH); // OrdType (1=Market, 2=Limit)
        
        if ("2".equals(orderType)) { // Limit order
            message.append("44=").append(price).append(SOH); // Price
        }
        
        message.append("59=").append(timeInForce).append(SOH); // TimeInForce (0=Day, 3=IOC, 4=FOK)
        
        sendMessage(message.toString());
        System.out.println("Order sent: " + clOrdID + " for " + symbol);
    }
    
    /**
     * Send Order Cancel Request (MsgType=F)
     */
    public void sendCancelOrder(String origClOrdID, String symbol, String side, double quantity) {
        StringBuilder message = new StringBuilder();
        String clOrdID = generateOrderID();
        
        message.append("35=F").append(SOH); // MsgType = OrderCancelRequest
        message.append("49=").append(senderCompID).append(SOH);
        message.append("56=").append(targetCompID).append(SOH);
        message.append("34=").append(msgSeqNum.getAndIncrement()).append(SOH);
        message.append("52=").append(getCurrentTimestamp()).append(SOH);
        
        message.append("11=").append(clOrdID).append(SOH); // ClOrdID (new)
        message.append("41=").append(origClOrdID).append(SOH); // OrigClOrdID
        message.append("55=").append(symbol).append(SOH); // Symbol
        message.append("54=").append(side).append(SOH); // Side
        message.append("60=").append(getCurrentTimestamp()).append(SOH); // TransactTime
        message.append("38=").append((int)quantity).append(SOH); // OrderQty
        
        sendMessage(message.toString());
        System.out.println("Cancel request sent for order: " + origClOrdID);
    }
    
    /**
     * Send Heartbeat (MsgType=0)
     */
    public void sendHeartbeat() {
        StringBuilder message = new StringBuilder();
        
        message.append("35=0").append(SOH); // MsgType = Heartbeat
        message.append("49=").append(senderCompID).append(SOH);
        message.append("56=").append(targetCompID).append(SOH);
        message.append("34=").append(msgSeqNum.getAndIncrement()).append(SOH);
        message.append("52=").append(getCurrentTimestamp()).append(SOH);
        
        sendMessage(message.toString());
    }
    
    /**
     * Send complete FIX message with header and trailer
     */
    private void sendMessage(String body) {
        StringBuilder fullMessage = new StringBuilder();
        
        // BeginString (tag 8)
        fullMessage.append("8=").append(fixVersion).append(SOH);
        
        // BodyLength (tag 9) - calculated
        int bodyLength = body.length();
        fullMessage.append("9=").append(bodyLength).append(SOH);
        
        // Body
        fullMessage.append(body);
        
        // CheckSum (tag 10) - calculated
        String checksum = calculateChecksum(fullMessage.toString());
        fullMessage.append("10=").append(checksum).append(SOH);
        
        // Send the message
        writer.print(fullMessage.toString());
        writer.flush();
        
        System.out.println("SENT: " + fullMessage.toString().replace(SOH, "|"));
    }
    
    /**
     * Calculate FIX checksum
     */
    private String calculateChecksum(String message) {
        int sum = 0;
        for (char c : message.toCharArray()) {
            sum += c;
        }
        sum = sum % 256;
        return String.format("%03d", sum);
    }
    
    /**
     * Receive and process incoming FIX messages
     */
    public void receiveMessages() {
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    processMessage(line);
                }
            } catch (IOException e) {
                System.err.println("Error receiving messages: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Process incoming FIX message
     */
    private void processMessage(String message) {
        System.out.println("RECEIVED: " + message.replace(SOH, "|"));
        
        String msgType = extractField(message, "35");
        
        switch (msgType) {
            case "A":
                System.out.println("Logon acknowledgment received");
                break;
            case "0":
                System.out.println("Heartbeat received");
                break;
            case "1":
                System.out.println("Test request received - sending heartbeat");
                sendHeartbeat();
                break;
            case "8":
                handleExecutionReport(message);
                break;
            case "9":
                handleOrderCancelReject(message);
                break;
            case "3":
                System.out.println("Reject received");
                break;
            case "5":
                System.out.println("Logout received");
                break;
            default:
                System.out.println("Unknown message type: " + msgType);
        }
    }
    
    /**
     * Handle Execution Report (MsgType=8)
     */
    private void handleExecutionReport(String message) {
        String clOrdID = extractField(message, "11");
        String execType = extractField(message, "150");
        String ordStatus = extractField(message, "39");
        String symbol = extractField(message, "55");
        String side = extractField(message, "54");
        String lastQty = extractField(message, "32");
        String lastPx = extractField(message, "31");
        
        System.out.println("Execution Report:");
        System.out.println("  ClOrdID: " + clOrdID);
        System.out.println("  Symbol: " + symbol);
        System.out.println("  ExecType: " + getExecTypeDesc(execType));
        System.out.println("  OrdStatus: " + getOrdStatusDesc(ordStatus));
        System.out.println("  Side: " + (side.equals("1") ? "Buy" : "Sell"));
        
        if (lastQty != null && !lastQty.isEmpty()) {
            System.out.println("  Filled: " + lastQty + " @ " + lastPx);
        }
    }
    
    /**
     * Handle Order Cancel Reject (MsgType=9)
     */
    private void handleOrderCancelReject(String message) {
        String clOrdID = extractField(message, "11");
        String reason = extractField(message, "102");
        System.out.println("Order Cancel Reject for " + clOrdID + " - Reason: " + reason);
    }
    
    /**
     * Extract field value from FIX message
     */
    private String extractField(String message, String tag) {
        String[] fields = message.split(SOH);
        for (String field : fields) {
            if (field.startsWith(tag + "=")) {
                return field.substring(tag.length() + 1);
            }
        }
        return null;
    }
    
    /**
     * Send logout and disconnect
     */
    public void disconnect() throws IOException {
        // Send Logout message (MsgType=5)
        StringBuilder message = new StringBuilder();
        message.append("35=5").append(SOH);
        message.append("49=").append(senderCompID).append(SOH);
        message.append("56=").append(targetCompID).append(SOH);
        message.append("34=").append(msgSeqNum.getAndIncrement()).append(SOH);
        message.append("52=").append(getCurrentTimestamp()).append(SOH);
        
        sendMessage(message.toString());
        
        // Close connections
        if (reader != null) reader.close();
        if (writer != null) writer.close();
        if (socket != null) socket.close();
    }
    
    // Helper methods
    
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(FIX_TIME_FORMAT);
    }
    
    private String generateOrderID() {
        return "ORD" + System.currentTimeMillis();
    }
    
    private String getExecTypeDesc(String execType) {
        switch (execType) {
            case "0": return "New";
            case "1": return "Partial Fill";
            case "2": return "Fill";
            case "4": return "Canceled";
            case "8": return "Rejected";
            default: return execType;
        }
    }
    
    private String getOrdStatusDesc(String ordStatus) {
        switch (ordStatus) {
            case "0": return "New";
            case "1": return "Partially Filled";
            case "2": return "Filled";
            case "4": return "Canceled";
            case "8": return "Rejected";
            default: return ordStatus;
        }
    }
    
    /**
     * Example usage
     */
    public static void main(String[] args) {
        try {
            // Configure your credentials
            String senderCompID = "YOUR_SENDER_ID";
            String targetCompID = "NYSE";
            String host = "fix-gateway.nyse.com"; // Example - use actual NYSE FIX gateway
            int port = 9876; // Example port
            
            FIXTradingClient client = new FIXTradingClient(senderCompID, targetCompID);
            
            // Connect to NYSE
            client.connect(host, port);
            
            // Start receiving messages
            client.receiveMessages();
            
            // Wait for logon confirmation
            Thread.sleep(2000);
            
            // Send a buy order for 100 shares of AAPL at market price
            client.sendNewOrder(
                "AAPL",           // Symbol
                "1",              // Side (1=Buy, 2=Sell)
                100,              // Quantity
                0,                // Price (not used for market orders)
                "1",              // OrderType (1=Market, 2=Limit)
                "0"               // TimeInForce (0=Day)
            );
            
            // Keep alive
            Thread.sleep(60000);
            
            // Disconnect
            client.disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

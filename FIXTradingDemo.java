import java.util.Scanner;

/**
 * Interactive Demo of FIX Trading Client
 * Demonstrates basic order management functionality
 */
public class FIXTradingDemo {
    
    private static FIXTradingClient client;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        
        try {
            System.out.println("=== FIX Trading Client Demo ===\n");
            
            // Option 1: Load from config file
            System.out.println("Load configuration from file? (y/n)");
            String loadConfig = scanner.nextLine().trim().toLowerCase();
            
            if (loadConfig.equals("y")) {
                System.out.print("Enter config file path (default: fix.properties): ");
                String configPath = scanner.nextLine().trim();
                if (configPath.isEmpty()) {
                    configPath = "fix.properties";
                }
                
                FIXSessionConfig config = FIXSessionConfig.loadFromFile(configPath);
                System.out.println("Configuration loaded: " + config);
                
                client = new FIXTradingClient(
                    config.getSenderCompID(),
                    config.getTargetCompID()
                );
                
                System.out.println("\nConnecting to " + config.getHost() + ":" + config.getPort());
                client.connect(config.getHost(), config.getPort());
            } else {
                // Option 2: Manual configuration
                System.out.print("Enter Sender CompID: ");
                String senderCompID = scanner.nextLine().trim();
                
                System.out.print("Enter Target CompID (default: NYSE): ");
                String targetCompID = scanner.nextLine().trim();
                if (targetCompID.isEmpty()) {
                    targetCompID = "NYSE";
                }
                
                System.out.print("Enter FIX Gateway Host: ");
                String host = scanner.nextLine().trim();
                
                System.out.print("Enter FIX Gateway Port: ");
                int port = Integer.parseInt(scanner.nextLine().trim());
                
                client = new FIXTradingClient(senderCompID, targetCompID);
                
                System.out.println("\nConnecting to " + host + ":" + port);
                client.connect(host, port);
            }
            
            // Start receiving messages
            client.receiveMessages();
            
            System.out.println("Waiting for logon confirmation...");
            Thread.sleep(2000);
            
            System.out.println("\n=== Connected! ===\n");
            
            // Interactive menu
            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        placeOrder();
                        break;
                    case "2":
                        cancelOrder();
                        break;
                    case "3":
                        sendHeartbeat();
                        break;
                    case "4":
                        viewMessageExample();
                        break;
                    case "5":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
            
            // Disconnect
            System.out.println("\nDisconnecting...");
            client.disconnect();
            System.out.println("Disconnected. Goodbye!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static void printMenu() {
        System.out.println("\n=== FIX Trading Menu ===");
        System.out.println("1. Place New Order");
        System.out.println("2. Cancel Order");
        System.out.println("3. Send Heartbeat");
        System.out.println("4. View Message Examples");
        System.out.println("5. Disconnect and Exit");
        System.out.print("\nEnter choice: ");
    }
    
    private static void placeOrder() {
        try {
            System.out.println("\n=== Place New Order ===");
            
            System.out.print("Enter Symbol (e.g., AAPL): ");
            String symbol = scanner.nextLine().trim().toUpperCase();
            
            System.out.print("Enter Side (1=Buy, 2=Sell): ");
            String side = scanner.nextLine().trim();
            
            System.out.print("Enter Quantity: ");
            double quantity = Double.parseDouble(scanner.nextLine().trim());
            
            System.out.print("Enter Order Type (1=Market, 2=Limit): ");
            String orderType = scanner.nextLine().trim();
            
            double price = 0;
            if (orderType.equals("2")) {
                System.out.print("Enter Limit Price: ");
                price = Double.parseDouble(scanner.nextLine().trim());
            }
            
            System.out.print("Enter Time In Force (0=Day, 1=GTC, 3=IOC, 4=FOK): ");
            String timeInForce = scanner.nextLine().trim();
            
            // Send the order
            client.sendNewOrder(symbol, side, quantity, price, orderType, timeInForce);
            
            System.out.println("\nOrder sent successfully!");
            System.out.println("Summary:");
            System.out.println("  Symbol: " + symbol);
            System.out.println("  Side: " + (side.equals("1") ? "Buy" : "Sell"));
            System.out.println("  Quantity: " + (int)quantity);
            System.out.println("  Type: " + (orderType.equals("1") ? "Market" : "Limit @ " + price));
            System.out.println("  TIF: " + getTimeInForceDesc(timeInForce));
            
        } catch (Exception e) {
            System.err.println("Error placing order: " + e.getMessage());
        }
    }
    
    private static void cancelOrder() {
        try {
            System.out.println("\n=== Cancel Order ===");
            
            System.out.print("Enter Original ClOrdID: ");
            String origClOrdID = scanner.nextLine().trim();
            
            System.out.print("Enter Symbol: ");
            String symbol = scanner.nextLine().trim().toUpperCase();
            
            System.out.print("Enter Side (1=Buy, 2=Sell): ");
            String side = scanner.nextLine().trim();
            
            System.out.print("Enter Quantity: ");
            double quantity = Double.parseDouble(scanner.nextLine().trim());
            
            client.sendCancelOrder(origClOrdID, symbol, side, quantity);
            
            System.out.println("\nCancel request sent for order: " + origClOrdID);
            
        } catch (Exception e) {
            System.err.println("Error canceling order: " + e.getMessage());
        }
    }
    
    private static void sendHeartbeat() {
        client.sendHeartbeat();
        System.out.println("Heartbeat sent.");
    }
    
    private static void viewMessageExample() {
        System.out.println("\n=== FIX Message Examples ===\n");
        
        System.out.println("1. NEW ORDER SINGLE (Buy 100 AAPL at Market):");
        System.out.println("8=FIX.4.4|9=156|35=D|49=SENDER|56=NYSE|34=1|52=20260213-14:30:00.000|");
        System.out.println("11=ORD123456|55=AAPL|54=1|60=20260213-14:30:00.000|38=100|40=1|59=0|10=123|");
        
        System.out.println("\n2. NEW ORDER SINGLE (Sell 50 TSLA at Limit $250.00):");
        System.out.println("8=FIX.4.4|9=168|35=D|49=SENDER|56=NYSE|34=2|52=20260213-14:31:00.000|");
        System.out.println("11=ORD123457|55=TSLA|54=2|60=20260213-14:31:00.000|38=50|40=2|44=250.00|59=0|10=234|");
        
        System.out.println("\n3. ORDER CANCEL REQUEST:");
        System.out.println("8=FIX.4.4|9=142|35=F|49=SENDER|56=NYSE|34=3|52=20260213-14:32:00.000|");
        System.out.println("11=ORD123458|41=ORD123456|55=AAPL|54=1|60=20260213-14:32:00.000|38=100|10=145|");
        
        System.out.println("\n4. EXECUTION REPORT (Fill):");
        System.out.println("8=FIX.4.4|9=198|35=8|49=NYSE|56=SENDER|34=1|52=20260213-14:30:05.000|");
        System.out.println("11=ORD123456|37=12345678|17=EX001|150=2|39=2|55=AAPL|54=1|38=100|");
        System.out.println("32=100|31=150.25|14=100|151=0|6=150.25|60=20260213-14:30:05.000|10=156|");
        
        System.out.println("\nNote: | represents SOH (Start of Header) character (ASCII 0x01)");
        System.out.println("\nCommon Field Tags:");
        System.out.println("  8 = BeginString     35 = MsgType        49 = SenderCompID");
        System.out.println("  56 = TargetCompID   34 = MsgSeqNum      52 = SendingTime");
        System.out.println("  11 = ClOrdID        55 = Symbol         54 = Side");
        System.out.println("  38 = OrderQty       40 = OrdType        44 = Price");
        System.out.println("  59 = TimeInForce    150 = ExecType      39 = OrdStatus");
        System.out.println("  32 = LastQty        31 = LastPx         10 = CheckSum");
    }
    
    private static String getTimeInForceDesc(String tif) {
        switch (tif) {
            case "0": return "Day";
            case "1": return "GTC (Good Till Cancel)";
            case "3": return "IOC (Immediate or Cancel)";
            case "4": return "FOK (Fill or Kill)";
            default: return tif;
        }
    }
}

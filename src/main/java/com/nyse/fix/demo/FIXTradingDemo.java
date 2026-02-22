package com.nyse.fix.demo;

import com.nyse.fix.IFIXTradingClient;
import com.nyse.fix.client.FIXTradingClient;
import com.nyse.fix.client.ArtioFIXTradingClient;
import com.nyse.fix.config.FIXSessionConfig;
import java.util.Scanner;

/**
 * Interactive Demo of FIX Trading Client
 * Demonstrates basic order management functionality
 * Supports both socket-based and Artio-based FIX implementations
 */
public class FIXTradingDemo {

    private static IFIXTradingClient client;
    private static Scanner scanner;

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        try {
            System.out.println("=== FIX Trading Client Demo ===\n");

            // Select implementation
            System.out.println("Choose FIX Implementation:");
            System.out.println("1. Socket-based (Basic)");
            System.out.println("2. Artio-based (High-performance)");
            System.out.print("Enter choice (1 or 2, default: 1): ");
            String implChoice = scanner.nextLine().trim();
            if (implChoice.isEmpty()) {
                implChoice = "1";
            }

            boolean useArtio = implChoice.equals("2");

            // Option 1: Load from config file
            System.out.println("\nLoad configuration from file? (y/n)");
            String loadConfig = scanner.nextLine().trim().toLowerCase();

            String senderCompID;
            String targetCompID;
            String host;
            int port;

            if (loadConfig.equals("y")) {
                System.out.print("Enter config file path (default: fix.properties): ");
                String configPath = scanner.nextLine().trim();
                if (configPath.isEmpty()) {
                    configPath = "fix.properties";
                }

                FIXSessionConfig config = FIXSessionConfig.loadFromFile(configPath);
                System.out.println("Configuration loaded: " + config);

                senderCompID = config.getSenderCompID();
                targetCompID = config.getTargetCompID();
                host = config.getHost();
                port = config.getPort();

            } else {
                // Option 2: Manual configuration
                System.out.print("Enter Sender CompID: ");
                senderCompID = scanner.nextLine().trim();

                System.out.print("Enter Target CompID (default: NYSE): ");
                targetCompID = scanner.nextLine().trim();
                if (targetCompID.isEmpty()) {
                    targetCompID = "NYSE";
                }

                System.out.print("Enter FIX Gateway Host: ");
                host = scanner.nextLine().trim();

                System.out.print("Enter FIX Gateway Port: ");
                port = Integer.parseInt(scanner.nextLine().trim());
            }

            // Create appropriate client implementation
            if (useArtio) {
                System.out.println("\nUsing Artio-based FIX client (high-performance)");
                client = new ArtioFIXTradingClient(senderCompID, targetCompID);
            } else {
                System.out.println("\nUsing socket-based FIX client (basic)");
                client = new FIXTradingClient(senderCompID, targetCompID);
            }

            System.out.println("Connecting to " + host + ":" + port);
            client.connect(host, port);

            // Start receiving messages in background
            client.receiveMessages();

            // Wait for logon
            System.out.println("\nWaiting for logon confirmation...");
            Thread.sleep(2000);

            if (!client.isLoggedOn()) {
                System.out.println("Warning: Not logged on yet. Continuing anyway...");
            } else {
                System.out.println("✓ Logged on successfully!");
            }

            // Main menu loop
            mainMenu();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Main interactive menu
     */
    private static void mainMenu() throws Exception {
        boolean running = true;

        while (running && client.isConnected()) {
            System.out.println("\n=== FIX Trading Menu ===");
            System.out.println("1. Place New Order");
            System.out.println("2. Cancel Order");
            System.out.println("3. Send Heartbeat");
            System.out.println("4. View Message Examples");
            System.out.println("5. Disconnect and Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    placeOrder();
                    break;
                case "2":
                    cancelOrder();
                    break;
                case "3":
                    client.sendHeartbeat();
                    break;
                case "4":
                    viewMessageExamples();
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        System.out.println("\nDisconnecting...");
        client.disconnect();
        System.out.println("Goodbye!");
    }

    /**
     * Place a new order
     */
    private static void placeOrder() throws Exception {
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
        if ("2".equals(orderType)) {
            System.out.print("Enter Limit Price: ");
            price = Double.parseDouble(scanner.nextLine().trim());
        }

        System.out.print("Enter Time In Force (0=Day, 1=GTC, 3=IOC, 4=FOK): ");
        String timeInForce = scanner.nextLine().trim();

        // Send order
        client.sendNewOrder(symbol, side, quantity, price, orderType, timeInForce);

        System.out.println("\n✓ Order sent successfully!");
        System.out.println("Summary:");
        System.out.println("  Symbol: " + symbol);
        System.out.println("  Side: " + ("1".equals(side) ? "Buy" : "Sell"));
        System.out.println("  Quantity: " + (int)quantity);
        if ("2".equals(orderType)) {
            System.out.println("  Price: " + price);
        }
        System.out.println("  Type: " + ("1".equals(orderType) ? "Market" : "Limit"));
    }

    /**
     * Cancel an existing order
     */
    private static void cancelOrder() throws Exception {
        System.out.println("\n=== Cancel Order ===");

        System.out.print("Enter Original Order ID (ClOrdID): ");
        String origClOrdID = scanner.nextLine().trim();

        System.out.print("Enter Symbol: ");
        String symbol = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Side (1=Buy, 2=Sell): ");
        String side = scanner.nextLine().trim();

        System.out.print("Enter Original Quantity: ");
        double quantity = Double.parseDouble(scanner.nextLine().trim());

        // Send cancel request
        client.sendCancelOrder(origClOrdID, symbol, side, quantity);

        System.out.println("\n✓ Cancel request sent!");
    }

    /**
     * View FIX message examples
     */
    private static void viewMessageExamples() {
        System.out.println("\n=== FIX Message Examples ===\n");

        String SOH = "|";

        System.out.println("1. Logon Message (MsgType=A):");
        System.out.println("8=FIX.4.4" + SOH + "9=156" + SOH + "35=A" + SOH + "49=TRADER1" + SOH +
                          "56=NYSE" + SOH + "34=1" + SOH + "52=20260222-14:30:00.000" + SOH +
                          "98=0" + SOH + "108=30" + SOH + "141=Y" + SOH + "10=123\n");

        System.out.println("2. New Order Single (MsgType=D):");
        System.out.println("8=FIX.4.4" + SOH + "9=198" + SOH + "35=D" + SOH + "49=TRADER1" + SOH +
                          "56=NYSE" + SOH + "34=2" + SOH + "52=20260222-14:30:01.000" + SOH +
                          "11=ORD123456" + SOH + "55=AAPL" + SOH + "54=1" + SOH +
                          "60=20260222-14:30:01.000" + SOH + "38=100" + SOH + "40=2" + SOH +
                          "44=150.25" + SOH + "59=0" + SOH + "10=124\n");

        System.out.println("3. Order Cancel Request (MsgType=F):");
        System.out.println("8=FIX.4.4" + SOH + "9=176" + SOH + "35=F" + SOH + "49=TRADER1" + SOH +
                          "56=NYSE" + SOH + "34=3" + SOH + "52=20260222-14:30:05.000" + SOH +
                          "11=ORD654321" + SOH + "41=ORD123456" + SOH + "55=AAPL" + SOH +
                          "54=1" + SOH + "60=20260222-14:30:05.000" + SOH + "38=100" + SOH + "10=125\n");

        System.out.println("4. Execution Report (MsgType=8):");
        System.out.println("8=FIX.4.4" + SOH + "9=212" + SOH + "35=8" + SOH + "49=NYSE" + SOH +
                          "56=TRADER1" + SOH + "34=4" + SOH + "52=20260222-14:30:02.000" + SOH +
                          "11=ORD123456" + SOH + "37=EXC123" + SOH + "150=2" + SOH + "39=2" + SOH +
                          "55=AAPL" + SOH + "54=1" + SOH + "38=100" + SOH + "32=100" + SOH +
                          "31=150.25" + SOH + "14=100" + SOH + "6=150.25" + SOH + "10=126\n");

        System.out.println("5. Heartbeat (MsgType=0):");
        System.out.println("8=FIX.4.4" + SOH + "9=89" + SOH + "35=0" + SOH + "49=TRADER1" + SOH +
                          "56=NYSE" + SOH + "34=5" + SOH + "52=20260222-14:30:30.000" + SOH + "10=127\n");

        System.out.println("\nKey FIX Tags:");
        System.out.println("  8 = BeginString (FIX version)");
        System.out.println("  9 = BodyLength");
        System.out.println("  10 = CheckSum");
        System.out.println("  35 = MsgType");
        System.out.println("  49 = SenderCompID");
        System.out.println("  56 = TargetCompID");
        System.out.println("  34 = MsgSeqNum");
        System.out.println("  52 = SendingTime");
        System.out.println("  11 = ClOrdID (Client Order ID)");
        System.out.println("  55 = Symbol");
        System.out.println("  54 = Side (1=Buy, 2=Sell)");
        System.out.println("  38 = OrderQty");
        System.out.println("  40 = OrdType (1=Market, 2=Limit)");
        System.out.println("  44 = Price");
        System.out.println("  59 = TimeInForce");
    }
}


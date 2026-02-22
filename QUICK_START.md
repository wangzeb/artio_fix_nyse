# Quick Start Guide - Artio FIX Trading Client

## 1. Prerequisites

- Java 11 or higher: `java -version`
- Maven 3.6+: `mvn -version`
- Access to a FIX gateway (NYSE test environment recommended for development)

## 2. Build the Project

```bash
# Navigate to project directory
cd artio_fix_nyse

# Option A: Build with Maven (recommended)
mvn clean package -DskipTests

# Option B: Compile directly
javac -cp artio_fix_nyse artio_fix_nyse/*.java
```

## 3. Configure Credentials

Edit `fix.properties`:

```properties
fix.senderCompID=YOUR_TRADER_ID
fix.targetCompID=NYSE
fix.host=fix-gateway.nyse.com
fix.port=9876
fix.version=FIX.4.4
fix.heartbeatInterval=30
```

## 4. Run the Application

### Option A: Using Maven

```bash
# Interactive demo
mvn exec:java -Dexec.mainClass="FIXTradingDemo"
```

### Option B: Using Java directly

```bash
# Compile first
javac -cp artio_fix_nyse artio_fix_nyse/*.java

# Run demo
java -cp artio_fix_nyse FIXTradingDemo
```

### Option C: Using executable JAR

```bash
# JAR is created during maven build
java -jar target/artio-fix-trading-uber.jar
```

## 5. Interactive Demo Menu

When you run the demo, you'll see:

```
=== FIX Trading Client Demo ===

Choose FIX Implementation:
1. Socket-based (Basic)           ← Choose for development
2. Artio-based (High-performance) ← Choose for production
Enter choice (1 or 2, default: 1):
```

### Selection Guide

- **Socket-Based (1)**: Good for learning, testing, development
- **Artio-Based (2)**: Production-grade, high-performance trading

## 6. Demo Commands

Once connected, you can:

```
=== FIX Trading Menu ===
1. Place New Order      ← Buy/Sell stocks
2. Cancel Order         ← Cancel existing orders
3. Send Heartbeat       ← Test connection
4. View Message Examples ← See FIX message format
5. Disconnect and Exit  ← Close connection
```

## 7. Example Trading Session

### Placing a Buy Order

```
Enter choice: 1

=== Place New Order ===
Enter Symbol (e.g., AAPL): AAPL
Enter Side (1=Buy, 2=Sell): 1
Enter Quantity: 100
Enter Order Type (1=Market, 2=Limit): 1
Enter Time In Force (0=Day, 1=GTC, 3=IOC, 4=FOK): 0

Order sent successfully!
Summary:
  Symbol: AAPL
  Side: Buy
  Quantity: 100
  Type: Market
  TIF: Day
```

### Checking Connection Status

```
Enter choice: 3

Heartbeat sent.
```

## 8. Code Integration

### In Your Own Java Code

```java
import java.io.IOException;

public class MyTradingApp {
    public static void main(String[] args) throws IOException {
        // Create client (choose one)
        IFIXTradingClient client = new FIXTradingClient("TRADER1", "NYSE");
        // OR for high-performance:
        // IFIXTradingClient client = new ArtioFIXTradingClient("TRADER1", "NYSE");
        
        // Connect
        client.connect("fix-gateway.nyse.com", 9876);
        client.receiveMessages();
        
        // Wait for logon
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        
        if (client.isLoggedOn()) {
            // Place an order
            client.sendNewOrder(
                "AAPL",     // Symbol
                "1",        // Side: 1=Buy, 2=Sell
                100,        // Quantity
                150.50,     // Price (for limit orders)
                "2",        // OrderType: 2=Limit, 1=Market
                "0"         // TimeInForce: 0=Day
            );
            
            // Keep running for 60 seconds
            try { Thread.sleep(60000); } catch (InterruptedException e) {}
        }
        
        // Disconnect
        client.disconnect();
    }
}
```

## 9. Monitoring and Logs

### View Recent Messages

Most recent FIX messages appear in console output with format:
```
SENT: 8=FIX.4.4|9=156|35=D|49=TRADER1|56=NYSE|34=1|...
RECEIVED: 8=FIX.4.4|9=198|35=8|49=NYSE|56=TRADER1|34=1|...
```

### Artio Logs

When using Artio implementation, logs are in:
```
artio-logs/
├── aeron/          # Aeron transport logs
├── engine-monitor  # FIX engine metrics
└── library-monitor # FIX library metrics
```

## 10. Troubleshooting

### Connection Refused
```
Error: java.net.ConnectException: Connection refused
```
- **Check**: Is the FIX gateway running?
- **Check**: Correct host and port in fix.properties?
- **Check**: Network connectivity: `ping fix-gateway.nyse.com`

### Not Logged On
```
Not logged on. Cannot send order.
```
- **Check**: Wait 2-3 seconds for logon handshake
- **Check**: Verify credentials in fix.properties
- **Check**: Check FIX gateway logs for rejection reason

### Compilation Error
```
error: cannot find symbol class FIXTradingClient
```
- **Fix**: Ensure all .java files are in artio_fix_nyse directory
- **Fix**: Recompile: `javac -cp artio_fix_nyse artio_fix_nyse/*.java`

### Maven Build Failure
```
[ERROR] Failed to execute goal on project artio-fix-trading
```
- **Fix**: Clear cache: `mvn clean`
- **Fix**: Rebuild: `mvn clean package -DskipTests`
- **Fix**: Check Java version: `java -version` (need 11+)

## 11. Next Steps

1. **Test with NYSE Test Environment**
   - Create account in NYSE test (paper trading)
   - Get test credentials
   - Test with socket-based client first

2. **Optimize for Production**
   - Switch to Artio-based client
   - Configure monitoring
   - Implement error handling and reconnection logic

3. **Scale Your Trading**
   - Increase message throughput
   - Monitor latency
   - Implement risk controls

## 12. FIX Message Reference

### Order Types
```
1 = Market Order
2 = Limit Order
```

### Sides
```
1 = Buy
2 = Sell
```

### Time In Force
```
0 = Day
1 = Good Till Cancelled (GTC)
3 = Immediate Or Cancel (IOC)
4 = Fill Or Kill (FOK)
```

### Common Message Types
```
A = Logon
D = New Order Single
F = Order Cancel Request
8 = Execution Report
0 = Heartbeat
5 = Logout
```

## 13. Performance Tips

### Socket-Based Client
- Good for: Development, testing
- Latency: 1-10ms
- Typical throughput: 100-1000 msgs/sec

### Artio-Based Client
- Good for: Production trading
- Latency: 10-100 microseconds
- Typical throughput: Millions msgs/sec
- Use when: High-frequency trading required

## 14. Important Notes

⚠️ **IMPORTANT**: 
- This is for educational purposes and testing
- **Always test in NYSE test environment first**
- Requires NYSE FIX certification for production
- Follow all regulatory requirements
- Implement proper risk controls before live trading

## 15. Additional Resources

- **Full Guide**: Read `ARTIO_INTEGRATION.md`
- **Original README**: See `README.md`
- **FIX Specification**: https://www.fixtrading.org/
- **Artio Project**: https://github.com/real-logic/artio
- **Aeron Project**: https://github.com/real-logic/aeron

---

**You're ready to start!** Run the demo and choose your implementation. 🚀

```bash
java -jar target/artio-fix-trading-uber.jar
```


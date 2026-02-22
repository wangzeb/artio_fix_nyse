# Artio FIX Engine Integration Guide

## Overview

This document describes how Artio, a high-performance FIX engine from Real Logic, has been integrated into the FIX Trading Client for NYSE. The integration provides both a traditional socket-based implementation and a high-performance Artio-based implementation.

## What is Artio?

Artio is a production-grade, low-latency FIX (Financial Information eXchange) engine built on top of Aeron (a high-performance messaging system). Key features include:

- **Ultra-low latency**: Microsecond-level message latency
- **High throughput**: Handles millions of messages per second
- **Reliable**: Built-in resend mechanisms and session management
- **Persistent**: Message logging and recovery capabilities
- **Real-time**: Optimized for high-frequency trading and real-time applications

## Project Structure

The integration includes:

### Core Files
- **FIXTradingClient.java** - Socket-based implementation (original)
- **ArtioFIXTradingClient.java** - Artio-based high-performance implementation
- **IFIXTradingClient.java** - Common interface for both implementations
- **FIXTradingDemo.java** - Interactive demo supporting both implementations
- **FIXSessionConfig.java** - Configuration management
- **FIXMessageParser.java** - Message parsing utilities

### Build Configuration
- **pom.xml** - Maven configuration with Artio and Aeron dependencies

## Installation and Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Linux, macOS, or Windows

### Building the Project

```bash
# Clone or navigate to the project directory
cd artio_fix_nyse

# Download dependencies and build
mvn clean install

# Run tests (if available)
mvn test

# Create executable JAR
mvn clean package
```

### Dependencies

The project includes:
- **Artio 1.24.0** - FIX engine library
- **Aeron 1.40.0** - High-performance messaging
- **SLF4J 1.7.36** - Logging API
- **Logback 1.2.11** - Logging implementation

## Using the Integrated Clients

### Option 1: Socket-Based Client (Original)

The socket-based implementation is suitable for:
- Learning and development
- Lower-frequency trading
- Testing and debugging
- Educational purposes

**Usage:**
```java
IFIXTradingClient client = new FIXTradingClient("YOUR_SENDER_ID", "NYSE");
client.connect("fix-gateway.nyse.com", 9876);
client.receiveMessages();
client.sendNewOrder("AAPL", "1", 100, 150.25, "2", "0");
client.disconnect();
```

### Option 2: Artio-Based Client (High-Performance)

The Artio implementation is designed for:
- High-frequency trading
- Low-latency requirements
- Production deployments
- High-volume message processing

**Usage:**
```java
IFIXTradingClient client = new ArtioFIXTradingClient("YOUR_SENDER_ID", "NYSE");
client.connect("fix-gateway.nyse.com", 9876);
client.receiveMessages();
client.sendNewOrder("AAPL", "1", 100, 150.25, "2", "0");
client.disconnect();
```

### Unified Interface

Both implementations implement `IFIXTradingClient`, allowing you to switch between them seamlessly:

```java
// Create client based on configuration or preference
IFIXTradingClient client;
if (useHighPerformance) {
    client = new ArtioFIXTradingClient(senderCompID, targetCompID);
} else {
    client = new FIXTradingClient(senderCompID, targetCompID);
}

// Use the same interface for both
client.connect(host, port);
client.sendNewOrder(symbol, side, qty, price, type, tif);
client.disconnect();
```

## Running the Interactive Demo

The demo allows you to:
- Select between socket-based or Artio-based implementation
- Load configuration from file or enter manually
- Place orders interactively
- Cancel orders
- View FIX message examples
- Send heartbeats

**Running the demo:**

```bash
# Compile
javac -cp target/classes artio_fix_nyse/*.java

# Run the demo
java -cp target/classes FIXTradingDemo

# Or if using the packaged JAR
java -jar target/artio-fix-trading-uber.jar
```

**Demo interaction:**
```
=== FIX Trading Client Demo ===

Choose FIX Implementation:
1. Socket-based (Basic)
2. Artio-based (High-performance)
Enter choice (1 or 2, default: 1): 2

Load configuration from file? (y/n)
y
Enter config file path (default: fix.properties): fix.properties
Configuration loaded: ...

Connecting to fix-gateway.nyse.com:9876

=== Connected! ===

=== FIX Trading Menu ===
1. Place New Order
2. Cancel Order
3. Send Heartbeat
4. View Message Examples
5. Disconnect and Exit

Enter choice: 1
```

## Artio Configuration

When using the Artio client, configuration files are created in the `artio-logs/` directory:

- **artio-logs/aeron/** - Aeron messaging logs
- **artio-logs/engine-monitor** - Engine monitoring file
- **artio-logs/library-monitor** - Library monitoring file

These directories are created automatically on first run.

### Environment Variables

Optional environment variables for Artio:
- `ARTIO_LOG_DIR` - Override the log directory (default: `artio-logs`)

## Message Format and FIX Protocol

Both implementations use FIX 4.4 by default. Messages follow the standard FIX format:

```
BeginString | BodyLength | MsgType | ... fields ... | CheckSum
8=FIX.4.4   | 9=length   | 35=D    | ... more ...   | 10=checksum
```

### Common Message Types

| Type | Name | Used For |
|------|------|----------|
| A | Logon | Session initiation |
| 0 | Heartbeat | Keep-alive |
| 1 | Test Request | Connection verification |
| D | New Order Single | Placing orders |
| F | Order Cancel Request | Canceling orders |
| 8 | Execution Report | Order updates |
| 9 | Order Cancel Reject | Cancel rejection |
| 5 | Logout | Session termination |

## Performance Considerations

### Socket-Based Client
- **Latency**: Typically 1-10ms
- **Throughput**: Suitable for 100-1000 messages/second
- **Memory**: Minimal overhead
- **Use case**: Development, testing, low-frequency trading

### Artio-Based Client
- **Latency**: Typically 10-100 microseconds
- **Throughput**: Supports millions of messages/second
- **Memory**: Higher memory footprint for buffering
- **Use case**: Production, high-frequency trading, low-latency requirements

## Error Handling

Both clients handle common FIX errors:
- Connection failures (IOException)
- Message rejections (MsgType=3)
- Order rejections (ExecType=8)
- Cancel rejections (MsgType=9)

### Graceful Disconnection

Both clients support graceful shutdown:
```java
client.disconnect(); // Sends logout and closes connections
```

## Logging and Monitoring

### Socket-Based Client
- Console output with message tracing
- Uses standard Java logging
- Simple debugging with `replace(SOH, "|")` for visualization

### Artio-Based Client
- Detailed Aeron transport logs
- Engine-level monitoring files
- Performance metrics in monitoring files
- Can be integrated with SLF4J/Logback for structured logging

## Production Deployment

### Recommendations for Production

1. **Choose Artio for production trading**
   - Lower latency
   - Better performance under load
   - Production-hardened implementation

2. **Configure proper logging**
   ```bash
   # Use logback.xml for production logging
   cp logback-production.xml logback.xml
   ```

3. **Set up monitoring**
   - Monitor engine health via monitoring files
   - Alert on connection issues
   - Track message latency and throughput

4. **Implement proper error handling**
   - Catch and log all exceptions
   - Implement reconnection logic
   - Handle sequence number gaps

5. **Security**
   - Use TLS/SSL for network connections
   - Encrypt credentials in configuration
   - Implement access controls
   - Follow NYSE security requirements

6. **Testing**
   - Always test in NYSE test environment first
   - Validate message formats thoroughly
   - Test error scenarios and edge cases
   - Load test with expected volume

## Troubleshooting

### Socket-Based Client Issues

**Connection refused**
- Verify host and port are correct
- Check if FIX gateway is running
- Ensure network connectivity and firewall rules

**Messages not received**
- Verify logon was successful
- Check if receiveMessages() is called
- Ensure proper heartbeat interval

### Artio-Based Client Issues

**Aeron logs directory issues**
- Ensure write permissions for `artio-logs/`
- Check disk space availability
- Verify no other instances using same directory

**Out of memory**
- Adjust JVM heap size: `java -Xmx2g ...`
- Reduce message buffer sizes if possible
- Monitor long-running connections

**Connection timeouts**
- Increase timeout values in EngineConfiguration
- Check network latency
- Verify FIX gateway connectivity

## Migrating from Socket-Based to Artio

To migrate an existing application:

1. **Identify current client usage**
   ```java
   FIXTradingClient client = new FIXTradingClient(...);
   ```

2. **Update to use interface**
   ```java
   IFIXTradingClient client = new FIXTradingClient(...);
   ```

3. **Switch to Artio implementation**
   ```java
   IFIXTradingClient client = new ArtioFIXTradingClient(...);
   ```

4. **Test thoroughly** in test environment

5. **Deploy to production**

## Example: Complete Trading Workflow

```java
import java.util.Scanner;

public class TradingExample {
    public static void main(String[] args) throws Exception {
        // Create Artio client for high-performance trading
        IFIXTradingClient client = new ArtioFIXTradingClient("TRADER1", "NYSE");
        
        // Connect to NYSE FIX gateway
        client.connect("fix-gateway.nyse.com", 9876);
        
        // Start receiving messages
        client.receiveMessages();
        
        // Wait for logon
        Thread.sleep(2000);
        
        if (client.isLoggedOn()) {
            System.out.println("Successfully logged in!");
            
            // Place a buy order
            client.sendNewOrder(
                "AAPL",      // Symbol
                "1",         // Side: 1=Buy
                100,         // Quantity
                150.50,      // Price (for limit orders)
                "2",         // OrderType: 2=Limit
                "0"          // TimeInForce: 0=Day
            );
            
            // Keep the connection alive
            for (int i = 0; i < 60; i++) {
                Thread.sleep(1000);
                if (!client.isConnected()) break;
            }
        }
        
        // Disconnect gracefully
        client.disconnect();
    }
}
```

## References

- [Artio on GitHub](https://github.com/real-logic/artio)
- [Aeron on GitHub](https://github.com/real-logic/aeron)
- [FIX Protocol Official Site](https://www.fixtrading.org/)
- [FIX 4.4 Specification](https://www.fixtrading.org/standards/fix-4-4/)
- NYSE FIX Specification (available from NYSE)

## License

This integration maintains the same license as the original FIX Trading Client project.

## Support and Issues

For issues or questions:
1. Check the troubleshooting section above
2. Review the FIX specification for protocol details
3. Check Artio and Aeron documentation
4. Verify configuration and connectivity
5. Review application logs for error messages

## Version History

- **v1.0.0** - Initial Artio integration
  - Added ArtioFIXTradingClient implementation
  - Created IFIXTradingClient interface
  - Updated FIXTradingDemo with implementation selection
  - Added Maven build configuration


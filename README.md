# FIX Protocol Trading Client for NYSE

A Java implementation of the FIX (Financial Information eXchange) protocol for trading on the New York Stock Exchange.

## Overview

This implementation provides a basic FIX client that can:
- Establish FIX sessions with NYSE
- Send and receive FIX messages
- Place orders (New Order Single)
- Cancel orders (Order Cancel Request)
- Handle execution reports
- Maintain heartbeats and session management

## Prerequisites

1. **NYSE FIX Certification**: You must be certified by NYSE to use their FIX gateway
2. **Credentials**: Obtain your SenderCompID, TargetCompID, username, and password from NYSE
3. **Network Access**: Whitelist your IP address with NYSE
4. **FIX Specification**: Familiarize yourself with FIX 4.4 specification (NYSE typically uses FIX 4.4)

## Project Structure

```
FIXTradingClient.java      - Main FIX client implementation
FIXSessionConfig.java      - Configuration management
FIXMessageParser.java      - Message parsing and utilities
fix.properties             - Configuration file (create this)
```

## Configuration

### 1. Create a properties file (fix.properties):

```properties
# Your company identifier
fix.senderCompID=YOUR_FIRM_ID

# NYSE target identifier
fix.targetCompID=NYSE

# NYSE FIX gateway connection details
fix.host=fix-gateway.nyse.com
fix.port=9876

# FIX version (NYSE typically uses FIX 4.4)
fix.version=FIX.4.4

# Heartbeat interval in seconds
fix.heartbeatInterval=30

# Reset sequence numbers on logon
fix.resetSeqNum=true

# Credentials (if required)
fix.username=your_username
fix.password=your_password

# Trading session (REGULAR, PRE_MARKET, POST_MARKET)
fix.tradingSessionID=REGULAR
```

### 2. NYSE Specific Requirements

- **FIX Version**: NYSE supports FIX 4.2 and FIX 4.4
- **Message Types**: 
  - Logon (A)
  - Heartbeat (0)
  - Test Request (1)
  - Logout (5)
  - New Order Single (D)
  - Order Cancel Request (F)
  - Order Cancel/Replace Request (G)
  - Execution Report (8)

## Usage Example

```java
// Load configuration
FIXSessionConfig config = FIXSessionConfig.loadFromFile("fix.properties");

// Create client
FIXTradingClient client = new FIXTradingClient(
    config.getSenderCompID(),
    config.getTargetCompID()
);

// Connect to NYSE
client.connect(config.getHost(), config.getPort());

// Start receiving messages
client.receiveMessages();

// Wait for logon confirmation
Thread.sleep(2000);

// Place a buy order
client.sendNewOrder(
    "AAPL",     // Symbol
    "1",        // Side (1=Buy, 2=Sell)
    100,        // Quantity
    150.25,     // Price (use 0 for market orders)
    "2",        // OrderType (1=Market, 2=Limit)
    "0"         // TimeInForce (0=Day, 1=GTC, 3=IOC)
);

// Disconnect when done
client.disconnect();
```

## FIX Message Format

FIX messages consist of three parts:

1. **Header**: Contains BeginString (8), BodyLength (9), and message-specific tags
2. **Body**: Message-specific fields
3. **Trailer**: CheckSum (10)

Fields are separated by SOH (Start of Header, ASCII 0x01) character.

### Example Order Message

```
8=FIX.4.4|9=156|35=D|49=SENDER|56=NYSE|34=1|52=20260213-14:30:00.000|
11=ORD123456|55=AAPL|54=1|60=20260213-14:30:00.000|38=100|40=2|44=150.25|59=0|10=123|
```

## Important FIX Fields

### Common Order Fields

| Tag | Field Name | Description | Values |
|-----|------------|-------------|--------|
| 11  | ClOrdID    | Client Order ID | Unique per order |
| 37  | OrderID    | Exchange Order ID | Assigned by exchange |
| 55  | Symbol     | Ticker symbol | e.g., "AAPL" |
| 54  | Side       | Buy/Sell | 1=Buy, 2=Sell |
| 38  | OrderQty   | Quantity | Number of shares |
| 40  | OrdType    | Order type | 1=Market, 2=Limit |
| 44  | Price      | Limit price | Required for limit orders |
| 59  | TimeInForce| Duration | 0=Day, 1=GTC, 3=IOC, 4=FOK |

### Execution Report Fields

| Tag | Field Name | Description | Values |
|-----|------------|-------------|--------|
| 150 | ExecType   | Execution type | 0=New, 1=Partial, 2=Fill, 4=Canceled |
| 39  | OrdStatus  | Order status | 0=New, 1=Partial, 2=Filled, 4=Canceled |
| 32  | LastQty    | Last filled qty | Number of shares filled |
| 31  | LastPx     | Last fill price | Price of last fill |
| 14  | CumQty     | Cumulative qty | Total shares filled |
| 6   | AvgPx      | Average price | Average fill price |

## Session Management

### Logon Sequence
1. Client connects to NYSE FIX gateway
2. Client sends Logon message (MsgType=A)
3. NYSE responds with Logon acknowledgment
4. Session is established

### Heartbeat
- Sent every N seconds (configured in HeartBtInt field)
- If no messages received within 2 * HeartBtInt, connection may be dropped
- TestRequest (MsgType=1) can be sent to verify connection

### Logout Sequence
1. Client sends Logout message (MsgType=5)
2. NYSE acknowledges with Logout
3. Connection is closed

## Error Handling

### Common Rejections

1. **Order Reject (MsgType=8, ExecType=8)**
   - Invalid symbol
   - Insufficient permissions
   - Market closed
   - Invalid price/quantity

2. **Order Cancel Reject (MsgType=9)**
   - Order already filled
   - Order not found
   - Too late to cancel

3. **Session Reject (MsgType=3)**
   - Invalid message format
   - Missing required fields
   - Invalid checksum

## Testing

### NYSE Test Environment

NYSE provides a test environment for FIX certification:
- Test hosts and ports (separate from production)
- Test credentials
- Test scenarios for certification

**Important**: Always test thoroughly in the test environment before going to production!

## Production Considerations

1. **Sequence Number Management**
   - Store sequence numbers persistently
   - Handle gaps and resend requests
   - Reset procedures during maintenance windows

2. **Connection Resilience**
   - Implement reconnection logic
   - Handle network failures gracefully
   - Monitor connection health

3. **Message Throttling**
   - Respect NYSE rate limits
   - Implement message queuing
   - Handle back-pressure

4. **Logging**
   - Log all sent/received messages
   - Store logs for regulatory compliance
   - Include timestamps and sequence numbers

5. **Security**
   - Encrypt credentials
   - Use secure connections (TLS/SSL)
   - Implement access controls
   - Follow NYSE security requirements

6. **Monitoring**
   - Track message latency
   - Monitor fill rates
   - Alert on connection issues
   - Dashboard for order status

## Regulatory Compliance

- **Market Access Rules**: Ensure proper risk controls
- **Order Tagging**: Include required regulatory tags (e.g., MPID)
- **Audit Trail**: Maintain complete message logs
- **Drop Copy**: Consider implementing drop copy for order tracking

## Advanced Features (Not Implemented in Basic Version)

- Market data subscriptions (MsgType=V)
- Mass quote (MsgType=i)
- Order mass cancel (MsgType=q)
- Trade capture reporting
- Position management
- Risk controls and pre-trade validation

## Troubleshooting

### Connection Issues
- Verify IP is whitelisted
- Check credentials
- Confirm correct host/port
- Validate network connectivity

### Message Rejections
- Verify message format
- Check required fields
- Validate checksum calculation
- Review FIX specification version

### Sequence Number Gaps
- Implement Resend Request (MsgType=2)
- Handle Sequence Reset (MsgType=4)
- Maintain persistent sequence store

## Resources

- [FIX Protocol Official Site](https://www.fixtrading.org/)
- [FIX 4.4 Specification](https://www.fixtrading.org/standards/fix-4-4/)
- NYSE FIX Specification (available from NYSE)
- NYSE Connectivity Guide (available from NYSE)

## License

This is a reference implementation for educational purposes. 
Production use requires proper certification and compliance with NYSE requirements.

## Disclaimer

This code is provided as-is for educational purposes. It is not production-ready and lacks many features required for live trading including:
- Proper error handling
- Persistent state management
- Comprehensive message validation
- Security controls
- Regulatory compliance features

Always use certified FIX engines for production trading and ensure full compliance with NYSE requirements and financial regulations.

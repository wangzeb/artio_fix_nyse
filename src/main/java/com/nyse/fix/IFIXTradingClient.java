package com.nyse.fix;

import java.io.IOException;

/**
 * Unified FIX Trading Client Interface
 * Supports both socket-based and Artio-based implementations
 */
public interface IFIXTradingClient {

    /**
     * Connect to FIX gateway
     */
    void connect(String host, int port) throws IOException;

    /**
     * Send New Order Single message
     */
    void sendNewOrder(String symbol, String side, double quantity, double price,
                     String orderType, String timeInForce);

    /**
     * Send Order Cancel Request
     */
    void sendCancelOrder(String origClOrdID, String symbol, String side, double quantity);

    /**
     * Send Heartbeat
     */
    void sendHeartbeat();

    /**
     * Receive messages (start background receiver)
     */
    void receiveMessages();

    /**
     * Disconnect and cleanup
     */
    void disconnect() throws IOException;

    /**
     * Check if connected
     */
    boolean isConnected();

    /**
     * Check if logged on
     */
    boolean isLoggedOn();
}


package com.nyse.fix.config;

import java.io.*;
import java.util.Properties;

/**
 * FIX Session Configuration Manager
 * Loads and manages FIX protocol session configuration
 */
public class FIXSessionConfig {

    private String senderCompID;
    private String targetCompID;
    private String host;
    private int port;
    private String fixVersion = "FIX.4.4";
    private int heartbeatInterval = 30;
    private String username;
    private String password;
    private String tradingSessionID = "REGULAR";
    private boolean resetSeqNum = true;

    /**
     * Create a new FIX session configuration
     */
    public FIXSessionConfig(String senderCompID, String targetCompID, String host, int port) {
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
        this.host = host;
        this.port = port;
    }

    /**
     * Load configuration from properties file
     */
    public static FIXSessionConfig loadFromFile(String filePath) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        }

        String senderCompID = props.getProperty("fix.senderCompID");
        String targetCompID = props.getProperty("fix.targetCompID", "NYSE");
        String host = props.getProperty("fix.host");
        int port = Integer.parseInt(props.getProperty("fix.port", "9876"));

        FIXSessionConfig config = new FIXSessionConfig(senderCompID, targetCompID, host, port);

        // Set optional properties
        if (props.containsKey("fix.version")) {
            config.setFixVersion(props.getProperty("fix.version"));
        }
        if (props.containsKey("fix.heartbeatInterval")) {
            config.setHeartbeatInterval(Integer.parseInt(props.getProperty("fix.heartbeatInterval")));
        }
        if (props.containsKey("fix.username")) {
            config.setUsername(props.getProperty("fix.username"));
        }
        if (props.containsKey("fix.password")) {
            config.setPassword(props.getProperty("fix.password"));
        }
        if (props.containsKey("fix.tradingSessionID")) {
            config.setTradingSessionID(props.getProperty("fix.tradingSessionID"));
        }

        return config;
    }

    /**
     * Save configuration to properties file
     */
    public void saveToFile(String filePath) throws IOException {
        Properties props = new Properties();
        props.setProperty("fix.senderCompID", senderCompID);
        props.setProperty("fix.targetCompID", targetCompID);
        props.setProperty("fix.host", host);
        props.setProperty("fix.port", String.valueOf(port));
        props.setProperty("fix.version", fixVersion);
        props.setProperty("fix.heartbeatInterval", String.valueOf(heartbeatInterval));
        props.setProperty("fix.tradingSessionID", tradingSessionID);
        props.setProperty("fix.resetSeqNum", String.valueOf(resetSeqNum));

        if (username != null) props.setProperty("fix.username", username);
        if (password != null) props.setProperty("fix.password", password);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            props.store(fos, "FIX Session Configuration");
        }
    }

    // Getters and Setters

    public String getSenderCompID() {
        return senderCompID;
    }

    public void setSenderCompID(String senderCompID) {
        this.senderCompID = senderCompID;
    }

    public String getTargetCompID() {
        return targetCompID;
    }

    public void setTargetCompID(String targetCompID) {
        this.targetCompID = targetCompID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTradingSessionID() {
        return tradingSessionID;
    }

    public void setTradingSessionID(String tradingSessionID) {
        this.tradingSessionID = tradingSessionID;
    }

    public boolean isResetSeqNum() {
        return resetSeqNum;
    }

    public void setResetSeqNum(boolean resetSeqNum) {
        this.resetSeqNum = resetSeqNum;
    }

    @Override
    public String toString() {
        return "FIXSessionConfig{" +
                "senderCompID='" + senderCompID + '\'' +
                ", targetCompID='" + targetCompID + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", fixVersion='" + fixVersion + '\'' +
                ", heartbeatInterval=" + heartbeatInterval +
                ", tradingSessionID='" + tradingSessionID + '\'' +
                '}';
    }
}


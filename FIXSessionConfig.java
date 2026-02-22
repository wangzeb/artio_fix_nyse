import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * FIX Session Configuration
 * Manages connection settings and session parameters
 */
public class FIXSessionConfig {
    
    private String senderCompID;
    private String targetCompID;
    private String host;
    private int port;
    private String fixVersion;
    private int heartbeatInterval;
    private boolean resetSeqNumOnLogon;
    private String username;
    private String password;
    
    // NYSE specific settings
    private String tradingSessionID;
    private String onBehalfOfCompID;
    
    public FIXSessionConfig() {
        // Default values
        this.fixVersion = "FIX.4.4";
        this.heartbeatInterval = 30;
        this.resetSeqNumOnLogon = true;
    }
    
    /**
     * Load configuration from properties file
     */
    public static FIXSessionConfig loadFromFile(String filepath) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(filepath));
        
        FIXSessionConfig config = new FIXSessionConfig();
        config.setSenderCompID(props.getProperty("fix.senderCompID"));
        config.setTargetCompID(props.getProperty("fix.targetCompID", "NYSE"));
        config.setHost(props.getProperty("fix.host"));
        config.setPort(Integer.parseInt(props.getProperty("fix.port")));
        config.setFixVersion(props.getProperty("fix.version", "FIX.4.4"));
        config.setHeartbeatInterval(Integer.parseInt(props.getProperty("fix.heartbeatInterval", "30")));
        config.setResetSeqNumOnLogon(Boolean.parseBoolean(props.getProperty("fix.resetSeqNum", "true")));
        config.setUsername(props.getProperty("fix.username"));
        config.setPassword(props.getProperty("fix.password"));
        config.setTradingSessionID(props.getProperty("fix.tradingSessionID"));
        config.setOnBehalfOfCompID(props.getProperty("fix.onBehalfOfCompID"));
        
        return config;
    }
    
    /**
     * Create a sample configuration file
     */
    public static void createSampleConfig(String filepath) throws IOException {
        Properties props = new Properties();
        
        props.setProperty("fix.senderCompID", "YOUR_FIRM_ID");
        props.setProperty("fix.targetCompID", "NYSE");
        props.setProperty("fix.host", "fix-gateway.nyse.com");
        props.setProperty("fix.port", "9876");
        props.setProperty("fix.version", "FIX.4.4");
        props.setProperty("fix.heartbeatInterval", "30");
        props.setProperty("fix.resetSeqNum", "true");
        props.setProperty("fix.username", "your_username");
        props.setProperty("fix.password", "your_password");
        props.setProperty("fix.tradingSessionID", "REGULAR");
        props.setProperty("fix.onBehalfOfCompID", "");
        
        props.store(new java.io.FileOutputStream(filepath), 
            "FIX Trading Configuration for NYSE");
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
    
    public boolean isResetSeqNumOnLogon() {
        return resetSeqNumOnLogon;
    }
    
    public void setResetSeqNumOnLogon(boolean resetSeqNumOnLogon) {
        this.resetSeqNumOnLogon = resetSeqNumOnLogon;
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
    
    public String getOnBehalfOfCompID() {
        return onBehalfOfCompID;
    }
    
    public void setOnBehalfOfCompID(String onBehalfOfCompID) {
        this.onBehalfOfCompID = onBehalfOfCompID;
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
                '}';
    }
}

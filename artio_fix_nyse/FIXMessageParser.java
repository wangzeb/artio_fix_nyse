import java.util.HashMap;
import java.util.Map;

/**
 * FIX Message Parser and Builder Utility
 */
public class FIXMessageParser {
    
    private static final String SOH = "\u0001";
    
    /**
     * Parse FIX message into a map of tag-value pairs
     */
    public static Map<String, String> parseMessage(String message) {
        Map<String, String> fields = new HashMap<>();
        
        String[] parts = message.split(SOH);
        for (String part : parts) {
            if (part.contains("=")) {
                String[] tagValue = part.split("=", 2);
                if (tagValue.length == 2) {
                    fields.put(tagValue[0], tagValue[1]);
                }
            }
        }
        
        return fields;
    }
    
    /**
     * Get human-readable message type
     */
    public static String getMessageTypeName(String msgType) {
        Map<String, String> msgTypes = new HashMap<>();
        msgTypes.put("0", "Heartbeat");
        msgTypes.put("1", "Test Request");
        msgTypes.put("2", "Resend Request");
        msgTypes.put("3", "Reject");
        msgTypes.put("4", "Sequence Reset");
        msgTypes.put("5", "Logout");
        msgTypes.put("8", "Execution Report");
        msgTypes.put("9", "Order Cancel Reject");
        msgTypes.put("A", "Logon");
        msgTypes.put("D", "New Order Single");
        msgTypes.put("F", "Order Cancel Request");
        msgTypes.put("G", "Order Cancel/Replace Request");
        msgTypes.put("H", "Order Status Request");
        msgTypes.put("V", "Market Data Request");
        msgTypes.put("W", "Market Data Snapshot");
        msgTypes.put("X", "Market Data Incremental Refresh");
        msgTypes.put("Y", "Market Data Request Reject");
        
        return msgTypes.getOrDefault(msgType, "Unknown (" + msgType + ")");
    }
    
    /**
     * Get field name from tag number
     */
    public static String getFieldName(String tag) {
        Map<String, String> fieldNames = new HashMap<>();
        
        // Standard header
        fieldNames.put("8", "BeginString");
        fieldNames.put("9", "BodyLength");
        fieldNames.put("35", "MsgType");
        fieldNames.put("49", "SenderCompID");
        fieldNames.put("56", "TargetCompID");
        fieldNames.put("34", "MsgSeqNum");
        fieldNames.put("52", "SendingTime");
        
        // Logon
        fieldNames.put("98", "EncryptMethod");
        fieldNames.put("108", "HeartBtInt");
        fieldNames.put("141", "ResetSeqNumFlag");
        fieldNames.put("553", "Username");
        fieldNames.put("554", "Password");
        
        // Order fields
        fieldNames.put("11", "ClOrdID");
        fieldNames.put("37", "OrderID");
        fieldNames.put("41", "OrigClOrdID");
        fieldNames.put("17", "ExecID");
        fieldNames.put("150", "ExecType");
        fieldNames.put("39", "OrdStatus");
        fieldNames.put("55", "Symbol");
        fieldNames.put("54", "Side");
        fieldNames.put("38", "OrderQty");
        fieldNames.put("40", "OrdType");
        fieldNames.put("44", "Price");
        fieldNames.put("59", "TimeInForce");
        fieldNames.put("60", "TransactTime");
        fieldNames.put("32", "LastQty");
        fieldNames.put("31", "LastPx");
        fieldNames.put("14", "CumQty");
        fieldNames.put("151", "LeavesQty");
        fieldNames.put("6", "AvgPx");
        
        // Cancel/Reject
        fieldNames.put("102", "CxlRejReason");
        fieldNames.put("103", "OrdRejReason");
        fieldNames.put("58", "Text");
        
        // Market Data
        fieldNames.put("262", "MDReqID");
        fieldNames.put("263", "SubscriptionRequestType");
        fieldNames.put("264", "MarketDepth");
        fieldNames.put("265", "MDUpdateType");
        fieldNames.put("267", "NoMDEntryTypes");
        fieldNames.put("268", "NoMDEntries");
        fieldNames.put("269", "MDEntryType");
        fieldNames.put("270", "MDEntryPx");
        fieldNames.put("271", "MDEntrySize");
        
        // Trailer
        fieldNames.put("10", "CheckSum");
        
        return fieldNames.getOrDefault(tag, "Tag" + tag);
    }
    
    /**
     * Validate FIX message checksum
     */
    public static boolean validateChecksum(String message) {
        String[] parts = message.split(SOH + "10=");
        if (parts.length != 2) {
            return false;
        }
        
        String bodyPart = parts[0];
        String checksumPart = parts[1].replace(SOH, "");
        
        int calculatedSum = 0;
        for (char c : (bodyPart + SOH + "10=").toCharArray()) {
            calculatedSum += c;
        }
        calculatedSum = calculatedSum % 256;
        
        String calculatedChecksum = String.format("%03d", calculatedSum);
        return calculatedChecksum.equals(checksumPart);
    }
    
    /**
     * Pretty print FIX message
     */
    public static String prettyPrint(String message) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> fields = parseMessage(message);
        
        String msgType = fields.get("35");
        sb.append("=== ").append(getMessageTypeName(msgType)).append(" ===\n");
        
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String tag = entry.getKey();
            String value = entry.getValue();
            sb.append(String.format("%-20s (%3s): %s\n", 
                getFieldName(tag), tag, value));
        }
        
        return sb.toString();
    }
    
    /**
     * Convert readable format to FIX message
     */
    public static String toFIXFormat(String readable) {
        return readable.replace("|", SOH);
    }
    
    /**
     * Convert FIX message to readable format
     */
    public static String toReadableFormat(String fixMessage) {
        return fixMessage.replace(SOH, "|");
    }
    
    /**
     * Extract specific field value from message
     */
    public static String extractField(String message, String tag) {
        Map<String, String> fields = parseMessage(message);
        return fields.get(tag);
    }
    
    /**
     * Check if message is valid FIX format
     */
    public static boolean isValidFIXMessage(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        Map<String, String> fields = parseMessage(message);
        
        // Check required header fields
        if (!fields.containsKey("8") || // BeginString
            !fields.containsKey("9") || // BodyLength
            !fields.containsKey("35")) { // MsgType
            return false;
        }
        
        // Check trailer
        if (!fields.containsKey("10")) { // CheckSum
            return false;
        }
        
        return true;
    }
    
    /**
     * Get enum value descriptions
     */
    public static String getEnumDescription(String tag, String value) {
        // Side (54)
        if ("54".equals(tag)) {
            switch (value) {
                case "1": return "Buy";
                case "2": return "Sell";
                case "3": return "Buy minus";
                case "4": return "Sell plus";
                case "5": return "Sell short";
                case "6": return "Sell short exempt";
                default: return value;
            }
        }
        
        // OrdType (40)
        if ("40".equals(tag)) {
            switch (value) {
                case "1": return "Market";
                case "2": return "Limit";
                case "3": return "Stop";
                case "4": return "Stop Limit";
                case "P": return "Pegged";
                default: return value;
            }
        }
        
        // TimeInForce (59)
        if ("59".equals(tag)) {
            switch (value) {
                case "0": return "Day";
                case "1": return "GTC (Good Till Cancel)";
                case "2": return "At the Opening";
                case "3": return "IOC (Immediate or Cancel)";
                case "4": return "FOK (Fill or Kill)";
                case "6": return "GTD (Good Till Date)";
                default: return value;
            }
        }
        
        // ExecType (150)
        if ("150".equals(tag)) {
            switch (value) {
                case "0": return "New";
                case "1": return "Partial Fill";
                case "2": return "Fill";
                case "4": return "Canceled";
                case "5": return "Replace";
                case "8": return "Rejected";
                case "C": return "Expired";
                default: return value;
            }
        }
        
        // OrdStatus (39)
        if ("39".equals(tag)) {
            switch (value) {
                case "0": return "New";
                case "1": return "Partially Filled";
                case "2": return "Filled";
                case "4": return "Canceled";
                case "6": return "Pending Cancel";
                case "8": return "Rejected";
                case "C": return "Expired";
                default: return value;
            }
        }
        
        return value;
    }
}

package com.nyse.fix.util;

import java.util.HashMap;
import java.util.Map;

/**
 * FIX Message Parser and Utilities
 * Provides utilities for parsing and constructing FIX messages
 */
public class FIXMessageParser {

    private static final String SOH = "\u0001";

    /**
     * Parse a FIX message into a map of tag-value pairs
     */
    public static Map<String, String> parseMessage(String fixMessage) {
        Map<String, String> fields = new HashMap<>();
        String[] pairs = fixMessage.split(SOH);

        for (String pair : pairs) {
            if (pair.isEmpty()) continue;

            int eqIndex = pair.indexOf('=');
            if (eqIndex > 0) {
                String tag = pair.substring(0, eqIndex);
                String value = pair.substring(eqIndex + 1);
                fields.put(tag, value);
            }
        }

        return fields;
    }

    /**
     * Extract a specific field value from a FIX message
     */
    public static String extractField(String fixMessage, String tag) {
        Map<String, String> fields = parseMessage(fixMessage);
        return fields.get(tag);
    }

    /**
     * Format FIX message for display by replacing SOH with |
     */
    public static String formatForDisplay(String fixMessage) {
        return fixMessage.replace(SOH, "|");
    }

    /**
     * Get FIX message type description
     */
    public static String getMessageTypeDescription(String msgType) {
        switch (msgType) {
            case "A": return "Logon";
            case "0": return "Heartbeat";
            case "1": return "TestRequest";
            case "2": return "ResendRequest";
            case "3": return "Reject";
            case "4": return "SequenceReset";
            case "5": return "Logout";
            case "D": return "NewOrderSingle";
            case "G": return "OrderCancelReplaceRequest";
            case "F": return "OrderCancelRequest";
            case "8": return "ExecutionReport";
            case "9": return "OrderCancelReject";
            case "V": return "MarketDataRequest";
            case "W": return "MarketDataSnapshot";
            default: return "Unknown (" + msgType + ")";
        }
    }

    /**
     * Get side description
     */
    public static String getSideDescription(String side) {
        switch (side) {
            case "1": return "Buy";
            case "2": return "Sell";
            default: return "Unknown";
        }
    }

    /**
     * Get order type description
     */
    public static String getOrderTypeDescription(String ordType) {
        switch (ordType) {
            case "1": return "Market";
            case "2": return "Limit";
            case "3": return "Stop";
            case "4": return "StopLimit";
            default: return "Unknown";
        }
    }

    /**
     * Get time in force description
     */
    public static String getTimeInForceDescription(String tif) {
        switch (tif) {
            case "0": return "Day";
            case "1": return "GoodTillCancel";
            case "3": return "ImmediateOrCancel";
            case "4": return "FillOrKill";
            default: return "Unknown";
        }
    }

    /**
     * Get execution type description
     */
    public static String getExecTypeDescription(String execType) {
        switch (execType) {
            case "0": return "New";
            case "1": return "PartialFill";
            case "2": return "Fill";
            case "3": return "DoneForDay";
            case "4": return "Canceled";
            case "5": return "Replaced";
            case "6": return "PendingCancel";
            case "7": return "Stopped";
            case "8": return "Rejected";
            case "9": return "Suspended";
            case "A": return "PendingNew";
            case "B": return "Calculated";
            case "C": return "Expired";
            case "D": return "Restated";
            case "E": return "PendingReplace";
            case "F": return "Trade";
            case "G": return "TradeCorrect";
            case "H": return "TradeCancel";
            case "I": return "OrderStatus";
            default: return "Unknown";
        }
    }

    /**
     * Get order status description
     */
    public static String getOrderStatusDescription(String ordStatus) {
        switch (ordStatus) {
            case "0": return "New";
            case "1": return "PartiallyFilled";
            case "2": return "Filled";
            case "3": return "DoneForDay";
            case "4": return "Canceled";
            case "5": return "Replaced";
            case "6": return "PendingCancel";
            case "7": return "Stopped";
            case "8": return "Rejected";
            case "9": return "Suspended";
            case "A": return "PendingNew";
            case "B": return "Calculated";
            case "C": return "Expired";
            case "D": return "Accepted";
            case "E": return "PendingReplace";
            default: return "Unknown";
        }
    }

    /**
     * Validate FIX message format
     */
    public static boolean isValidFIXMessage(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // Check for BeginString (tag 8)
        if (!message.startsWith("8=")) {
            return false;
        }

        // Check for CheckSum (tag 10) at the end
        if (!message.contains("10=")) {
            return false;
        }

        return true;
    }

    /**
     * Get FIX message checksum
     */
    public static String getChecksum(String message) {
        // Extract checksum value (tag 10)
        String[] parts = message.split(SOH);
        for (String part : parts) {
            if (part.startsWith("10=")) {
                return part.substring(3);
            }
        }
        return null;
    }

    /**
     * Calculate FIX message checksum
     */
    public static String calculateChecksum(String message) {
        int sum = 0;
        for (char c : message.toCharArray()) {
            sum += c;
        }
        sum = sum % 256;
        return String.format("%03d", sum);
    }

    /**
     * Get FIX message body (excluding BeginString, BodyLength, and CheckSum)
     */
    public static String getMessageBody(String message) {
        // Find the end of BodyLength (tag 9)
        int bodyLengthEnd = message.indexOf(SOH, message.indexOf("9="));

        // Find the start of CheckSum (tag 10)
        int checksumStart = message.lastIndexOf(SOH + "10=");

        if (bodyLengthEnd > 0 && checksumStart > bodyLengthEnd) {
            return message.substring(bodyLengthEnd + 1, checksumStart + 1);
        }

        return "";
    }
}


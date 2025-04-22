package utils;

public class MessageUtils {
    public static String extract(String message, String protocol) {
        if (protocol.toUpperCase().equals("HTTP")) {
            String[] parts = message.split("\\r?\\n\\r?\\n", 2);
            if (parts.length == 2) {
                return parts[1].trim();
            } else {
                return "";
            }
        } else {
            return message;
        }
    }
}

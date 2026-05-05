package com.slt.iau_portal.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9]{10,15}$");
    
    private static final Pattern HTML_PATTERN = 
        Pattern.compile("<[^>]*>");

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.replaceAll("\\s+", "")).matches();
    }

    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        // Check for path traversal attempts
        return !fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\");
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        // Remove HTML tags
        String sanitized = HTML_PATTERN.matcher(input).replaceAll("");
        // Trim whitespace
        return sanitized.trim();
    }

    public static boolean containsHtmlTags(String input) {
        if (input == null) {
            return false;
        }
        return HTML_PATTERN.matcher(input).find();
    }
}

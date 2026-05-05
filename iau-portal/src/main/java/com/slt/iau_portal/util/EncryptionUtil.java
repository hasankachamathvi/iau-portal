package com.slt.iau_portal.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptionUtil {

    /**
     * Hash sensitive data for audit logging
     */
    public static String hashForLogging(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }
    }

    /**
     * Mask email address for display
     */
    public static String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 0) {
            return "***";
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (local.length() <= 2) {
            return "*" + local.charAt(local.length() - 1) + domain;
        }
        
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    /**
     * Mask phone number for display
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 1);
    }
}

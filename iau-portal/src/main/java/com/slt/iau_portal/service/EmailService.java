package com.slt.iau_portal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendConfirmationEmail(String toEmail, String crn, String category) {
        if (mailSender == null) {
            System.out.println("[EMAIL] Skipped (mail not configured). CRN: " + crn);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("IAU Complaint Portal - Confirmation (CRN: " + crn + ")");
            message.setText("Dear User,\n\n" +
                    "Your complaint has been successfully submitted.\n\n" +
                    "Complaint Reference Number (CRN): " + crn + "\n" +
                    "Category: " + category + "\n\n" +
                    "You can use this CRN to track your complaint status.\n\n" +
                    "Thank you for reporting.\n\n" +
                    "Best regards,\n" +
                    "Internal Affairs Unit (IAU)");
            message.setFrom("noreply@iau-portal.local");
            mailSender.send(message);
            System.out.println("[EMAIL] Sent to: " + toEmail + " | CRN: " + crn);
        } catch (Exception e) {
            System.out.println("[EMAIL ERROR] Failed to send: " + e.getMessage());
        }
    }
}

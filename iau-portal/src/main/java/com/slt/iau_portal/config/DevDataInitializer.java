package com.slt.iau_portal.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.repository.ComplaintRepository;

@Component
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    @Autowired
    private ComplaintRepository complaintRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String crn = "DEV-0001";
        int attempts = 0;
        final int maxAttempts = 6;
        while (attempts < maxAttempts) {
            try {
                Complaint c = new Complaint();
                c.setCrn(crn);
                c.setCategory("DEVELOPMENT");
                c.setDescription("Seed complaint for development environment.");
                c.setComplaintDate(LocalDate.now());
                c.setLocation("Localhost");
                c.setStatus("PENDING");
                c.setCreatedAt(LocalDateTime.now());
                c.setUpdatedAt(LocalDateTime.now());
                complaintRepository.save(c);
                log.info("DevDataInitializer: seeded complaint with CRN={}", crn);
                return;
            } catch (Exception e) {
                attempts++;
                log.warn("DevDataInitializer: attempt {}/{} failed to seed complaint: {}", attempts, maxAttempts, e.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.error("DevDataInitializer: failed to seed complaint after {} attempts", maxAttempts);
    }
}

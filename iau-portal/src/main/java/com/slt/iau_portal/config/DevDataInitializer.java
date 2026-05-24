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
        // Idempotent seeding: only insert if the CRN does not already exist.
        try {
            if (complaintRepository.findByCrn(crn).isPresent()) {
                log.debug("DevDataInitializer: complaint with CRN={} already exists, skipping seed", crn);
                return;
            }

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
        } catch (Exception e) {
            log.warn("DevDataInitializer: failed to seed complaint {}: {}", crn, e.getMessage());
        }
    }
}

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
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.ReporterRepository;

@Component
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReporterRepository reporterRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // Seed multiple dev complaints (idempotent)
        seedComplaint("DEV-0001", "DEVELOPMENT", "Seed complaint for development environment.", false, null, null);
        seedComplaint("DEV-ANON-0001", "HARASSMENT", "This is a seeded anonymous complaint used for testing the Tracking dashboard. The description intentionally exceeds the minimum length.", true, null, null);
        seedComplaint("DEV-NAMED-0001", "HARASSMENT", "This is a seeded named complaint used for testing the Admin dashboard and email flows. The description intentionally exceeds the minimum length.", false, "Test Reporter", "test.reporter@example.com");
    }

    private void seedComplaint(String crn, String category, String description, boolean anonymous, String reporterName, String reporterEmail) {
        try {
            if (complaintRepository.findByCrn(crn).isPresent()) {
                log.debug("DevDataInitializer: complaint with CRN={} already exists, skipping seed", crn);
                return;
            }

            Complaint c = new Complaint();
            c.setCrn(crn);
            c.setCategory(category);
            c.setDescription(description);
            c.setComplaintDate(LocalDate.now());
            c.setLocation("Dev Environment");
            c.setStatus("PENDING");
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(c);

            // Add reporter record
            Reporter r = new Reporter();
            r.setComplaint(c);
            r.setAnonymousFlag(anonymous);
            if (!anonymous) {
                r.setFullName(reporterName);
                r.setEmail(reporterEmail);
            }
            reporterRepository.save(r);

            log.info("DevDataInitializer: seeded complaint with CRN={}", crn);
        } catch (Exception e) {
            log.warn("DevDataInitializer: failed to seed complaint {}: {}", crn, e.getMessage());
        }
    }
}

package com.slt.iau_portal.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.repository.ComplaintRepository;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Override
    public void run(String... args) throws Exception {
        String crn = "DEV-0001";
        if (!complaintRepository.findByCrn(crn).isPresent()) {
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
        }
    }
}

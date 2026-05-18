package com.slt.iau_portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.service.AuditLogService;

@Controller
public class TrackController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/track")
    public String trackRoot(@RequestParam(required = false) String crn, Model model) {
        if (crn != null && !crn.isBlank()) {
            Complaint complaint = complaintRepository.findByCrnIgnoreCase(crn.trim()).orElse(null);
            if (complaint == null) {
                model.addAttribute("error", "not_found");
            } else {
                model.addAttribute("complaint", complaint);
                try {
                    auditLogService.record("COMPLAINT_TRACKED", complaint.getCrn(), "PUBLIC", "Tracked via public tracker");
                } catch (Exception e) {
                    // don't fail lookup on audit logging errors
                }
            }
        }

        return "track";
    }
}

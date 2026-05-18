package com.slt.iau_portal.controller;

import java.util.Optional;

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
    public String track(@RequestParam(required = false) String crn, Model model) {
        if (crn == null || crn.isBlank()) {
            model.addAttribute("message", "Enter your complaint reference number (CRN) to track its status.");
            return "track";
        }

        String normalized = crn.trim();
        Optional<Complaint> complaintOpt = complaintRepository.findByCrnIgnoreCase(normalized);

        if (complaintOpt.isPresent()) {
            Complaint c = complaintOpt.get();
            model.addAttribute("found", true);
            model.addAttribute("complaint", c);
            auditLogService.record("PUBLIC_TRACK_VIEWED", c.getCrn(), "PUBLIC", "Complaint tracked by CRN");
        } else {
            model.addAttribute("found", false);
            model.addAttribute("message", "No complaint found for CRN: " + normalized);
            auditLogService.record("PUBLIC_TRACK_NOT_FOUND", normalized, "PUBLIC", "No complaint found for CRN");
        }

        model.addAttribute("crn", normalized);
        return "track";
    }
}

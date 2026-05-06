package com.slt.iau_portal.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.model.Evidence;
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.model.Subject;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.EvidenceRepository;
import com.slt.iau_portal.repository.ReporterRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminControllerEnhanced {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final ComplaintRepository complaintRepository;
    private final ReporterRepository reporterRepository;
    private final EvidenceRepository evidenceRepository;

    public AdminControllerEnhanced(
            ComplaintRepository complaintRepository,
            ReporterRepository reporterRepository,
            EvidenceRepository evidenceRepository) {
        this.complaintRepository = complaintRepository;
        this.reporterRepository = reporterRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        logger.info("Admin dashboard accessed");
        
        long totalComplaints = complaintRepository.count();
        long pendingComplaints = complaintRepository.countByStatus("PENDING");
        
        logger.info("Dashboard stats - Total: {}, Pending: {}", totalComplaints, pendingComplaints);
        
        model.addAttribute("totalComplaints", totalComplaints);
        model.addAttribute("pendingComplaints", pendingComplaints);
        model.addAttribute("resolvedComplaints", totalComplaints - pendingComplaints);
        
        return "admin/dashboard";
    }

    @GetMapping("/complaints")
    public String viewComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String category,
            Model model) {
        
        logger.info("Fetching complaints - page: {}, size: {}, status: {}, category: {}", 
            page, size, status, category);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Complaint> complaints;

        if (!status.isEmpty() && !category.isEmpty()) {
            complaints = complaintRepository.findByStatusAndCategory(status, category, pageable);
        } else if (!status.isEmpty()) {
            complaints = complaintRepository.findByStatus(status, pageable);
        } else if (!category.isEmpty()) {
            complaints = complaintRepository.findByCategory(category, pageable);
        } else {
            complaints = complaintRepository.findAll(pageable);
        }

        model.addAttribute("complaints", complaints);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", complaints.getTotalPages());
        model.addAttribute("status", status);
        model.addAttribute("category", category);
        
        return "admin/complaint-list";
    }

    @GetMapping("/complaint/{id}")
    public String viewComplaintDetail(@PathVariable Long id, Model model) {
        logger.info("Fetching complaint details for ID: {}", id);
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        
        if (complaintOpt.isEmpty()) {
            logger.warn("Complaint not found with ID: {}", id);
            model.addAttribute("error", "Complaint not found");
            return "error";
        }

        Complaint complaint = complaintOpt.get();
        Reporter reporter = reporterRepository.findByComplaintId(complaint.getId());
        List<Evidence> evidences = evidenceRepository.findByComplaintId(complaint.getId());

        model.addAttribute("complaint", complaint);
        model.addAttribute("reporter", reporter);
        model.addAttribute("evidences", evidences);

        logger.info("Complaint details loaded - CRN: {}", complaint.getCrn());
        return "admin/complaint-detail";
    }

    @PostMapping("/complaint/{id}/status")
    public String updateComplaintStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String notes,
            Model model) {
        
        logger.info("Updating complaint status - ID: {}, New Status: {}", id, status);
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        
        if (complaintOpt.isPresent()) {
            Complaint complaint = complaintOpt.get();
            complaint.setStatus(status);
            complaintRepository.save(complaint);
            
            logger.info("Complaint status updated successfully - CRN: {}, Status: {}", 
                complaint.getCrn(), status);
            
            model.addAttribute("success", "Complaint status updated successfully");
        }

        return "redirect:/admin/complaint/" + id;
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        logger.info("Generating statistics");
        
        Map<String, Long> categoryStats = new HashMap<>();
        List<Complaint> allComplaints = complaintRepository.findAll();
        
        allComplaints.forEach(complaint -> 
            categoryStats.put(complaint.getCategory(), 
                categoryStats.getOrDefault(complaint.getCategory(), 0L) + 1)
        );

        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("PENDING", complaintRepository.countByStatus("PENDING"));
        statusStats.put("UNDER_INVESTIGATION", complaintRepository.countByStatus("UNDER_INVESTIGATION"));
        statusStats.put("RESOLVED", complaintRepository.countByStatus("RESOLVED"));

        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("statusStats", statusStats);
        model.addAttribute("totalComplaints", allComplaints.size());

        logger.info("Statistics generated - Total complaints: {}", allComplaints.size());
        return "admin/statistics";
    }
}

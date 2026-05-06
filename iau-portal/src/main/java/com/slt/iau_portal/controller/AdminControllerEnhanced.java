package com.slt.iau_portal.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.model.Evidence;
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.model.Subject;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.EvidenceRepository;
import com.slt.iau_portal.repository.ReporterRepository;
import com.slt.iau_portal.repository.SubjectRepository;

@Controller
@RequestMapping("/admin")
public class AdminControllerEnhanced {

    private static final Logger logger = LoggerFactory.getLogger(AdminControllerEnhanced.class);

    private final ComplaintRepository complaintRepository;
    private final ReporterRepository reporterRepository;
    private final SubjectRepository subjectRepository;
    private final EvidenceRepository evidenceRepository;

    public AdminControllerEnhanced(
            ComplaintRepository complaintRepository,
            ReporterRepository reporterRepository,
            SubjectRepository subjectRepository,
            EvidenceRepository evidenceRepository) {
        this.complaintRepository = complaintRepository;
        this.reporterRepository = reporterRepository;
        this.subjectRepository = subjectRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page) {

        logger.info("Admin dashboard accessed");
        
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Complaint> complaints = getComplaintsByFilter(filter, pageable);
        Map<Long, Reporter> reportersByComplaintId = getReportersByComplaintId(complaints.getContent());
        java.time.LocalDateTime monthStart = java.time.YearMonth.now().atDay(1).atStartOfDay();
        java.time.LocalDateTime monthEnd = java.time.YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
        long totalComplaints = complaintRepository.count();
        long escalatedCount = complaintRepository.countByEscalatedTrue();
        long pendingCount = complaintRepository.countByStatus("PENDING");
        long monthlyCount = complaintRepository.countByCreatedAtBetween(monthStart, monthEnd);
        
        logger.info("Dashboard stats - Total: {}, Pending: {}", totalComplaints, pendingCount);
        
        model.addAttribute("complaints", complaints.getContent());
        model.addAttribute("reportersByComplaintId", reportersByComplaintId);
        model.addAttribute("totalComplaints", totalComplaints);
        model.addAttribute("escalatedCount", escalatedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("monthlyCount", monthlyCount);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentFilterLabel", getFilterLabel(filter));
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", complaints.getTotalPages());
        
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

    @GetMapping("/complaint/{crn}")
    public String viewComplaintDetail(@PathVariable String crn, Model model) {
        logger.info("Fetching complaint details for CRN: {}", crn);
        
        Optional<Complaint> complaintOpt = complaintRepository.findByCrn(crn);
        
        if (complaintOpt.isEmpty()) {
            logger.warn("Complaint not found with CRN: {}", crn);
            model.addAttribute("error", "Complaint not found");
            return "error";
        }

        Complaint complaint = complaintOpt.get();
        Reporter reporter = reporterRepository.findByComplaintId(complaint.getId());
        List<Subject> subjects = subjectRepository.findByComplaintId(complaint.getId());
        List<Evidence> evidences = evidenceRepository.findByComplaintId(complaint.getId());

        model.addAttribute("complaint", complaint);
        model.addAttribute("reporter", reporter);
        model.addAttribute("subjects", subjects);
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

    private Page<Complaint> getComplaintsByFilter(String filter, Pageable pageable) {
        java.time.LocalDateTime monthStart = java.time.YearMonth.now().atDay(1).atStartOfDay();
        java.time.LocalDateTime monthEnd = java.time.YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();

        if ("pending".equalsIgnoreCase(filter)) {
            return complaintRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable);
        }

        if ("escalated".equalsIgnoreCase(filter)) {
            return complaintRepository.findByEscalatedTrueOrderByCreatedAtDesc(pageable);
        }

        if ("month".equalsIgnoreCase(filter)) {
            return complaintRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(monthStart, monthEnd, pageable);
        }

        return complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private Map<Long, Reporter> getReportersByComplaintId(List<Complaint> complaints) {
        Map<Long, Reporter> reportersByComplaintId = new HashMap<>();

        for (Complaint complaint : complaints) {
            Reporter reporter = reporterRepository.findByComplaintId(complaint.getId());
            if (reporter != null) {
                reportersByComplaintId.put(complaint.getId(), reporter);
            }
        }

        return reportersByComplaintId;
    }

    private String getFilterLabel(String filter) {
        if ("pending".equalsIgnoreCase(filter)) {
            return "Pending Complaints";
        }

        if ("escalated".equalsIgnoreCase(filter)) {
            return "Escalated Complaints";
        }

        if ("month".equalsIgnoreCase(filter)) {
            return "This Month's Complaints";
        }

        if ("search".equalsIgnoreCase(filter)) {
            return "Search Results";
        }

        return "All Complaints";
    }
}

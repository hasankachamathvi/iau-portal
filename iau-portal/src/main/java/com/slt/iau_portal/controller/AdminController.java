package com.slt.iau_portal.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
public class AdminController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReporterRepository reporterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Complaint> complaints = getComplaintsByFilter(filter, pageable);
        Map<Long, Reporter> reportersByComplaintId = getReportersByComplaintId(complaints.getContent());
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
        
        model.addAttribute("complaints", complaints.getContent());
        model.addAttribute("reportersByComplaintId", reportersByComplaintId);
        model.addAttribute("totalComplaints", complaintRepository.count());
        model.addAttribute("escalatedCount", complaintRepository.countByEscalatedTrue());
        model.addAttribute("pendingCount", complaintRepository.countByStatus("PENDING"));
        model.addAttribute("monthlyCount", complaintRepository.countByCreatedAtBetween(monthStart, monthEnd));
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentFilterLabel", getFilterLabel(filter));
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", complaints.getTotalPages());
        
        return "admin/dashboard";
    }

    @GetMapping("/complaint/{crn}")
    public String viewComplaint(@PathVariable String crn, Model model) {
        Complaint complaint = complaintRepository.findByCrn(crn).orElse(null);
        
        if (complaint == null) {
            model.addAttribute("error", "Complaint not found");
            return "redirect:/admin/dashboard";
        }
        
        Reporter reporter = reporterRepository.findByComplaintId(complaint.getId());
        List<Subject> subjects = subjectRepository.findByComplaintId(complaint.getId());
        List<Evidence> evidence = evidenceRepository.findByComplaintId(complaint.getId());
        
        model.addAttribute("complaint", complaint);
        model.addAttribute("reporter", reporter);
        model.addAttribute("subjects", subjects);
        model.addAttribute("evidence", evidence);
        
        return "admin/complaint-detail";
    }

    @PostMapping("/complaint/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        Complaint complaint = complaintRepository.findById(id).orElse(null);
        
        if (complaint != null) {
            complaint.setStatus(status);
            complaintRepository.save(complaint);
        }
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/search")
    public String searchComplaints(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String crn,
            Model model) {

        String searchValue = query != null && !query.isBlank() ? query : crn;
        List<Complaint> complaints = searchValue == null || searchValue.isBlank()
                ? List.of()
                : complaintRepository.findByCrn(searchValue).stream().toList();

        Map<Long, Reporter> reportersByComplaintId = getReportersByComplaintId(complaints);
        
        model.addAttribute("complaints", complaints);
        model.addAttribute("searchQuery", query);
        model.addAttribute("reportersByComplaintId", reportersByComplaintId);
        model.addAttribute("totalComplaints", complaintRepository.count());
        model.addAttribute("pendingCount", complaintRepository.countByStatus("PENDING"));
        model.addAttribute("escalatedCount", complaintRepository.countByEscalatedTrue());
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
        model.addAttribute("monthlyCount", complaintRepository.countByCreatedAtBetween(monthStart, monthEnd));
        model.addAttribute("currentFilter", "search");
        model.addAttribute("currentFilterLabel", "Search Results");
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", 1);
        
        return "admin/dashboard";
    }

    private Page<Complaint> getComplaintsByFilter(String filter, Pageable pageable) {
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();

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

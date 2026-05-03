package com.slt.iau_portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.model.Subject;
import com.slt.iau_portal.model.Evidence;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.ReporterRepository;
import com.slt.iau_portal.repository.SubjectRepository;
import com.slt.iau_portal.repository.EvidenceRepository;

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
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Complaint> complaints = complaintRepository.findAll(pageable);
        
        model.addAttribute("complaints", complaints.getContent());
        model.addAttribute("totalComplaintsCount", complaintRepository.count());
        model.addAttribute("escalatedCount", complaintRepository.countByEscalatedTrue());
        model.addAttribute("pendingCount", complaintRepository.countByStatus("PENDING"));
        model.addAttribute("currentPage", page);
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
    public String searchComplaints(@RequestParam String query, Model model) {
        List<Complaint> complaints = complaintRepository.findByCrn(query).stream().toList();
        
        model.addAttribute("complaints", complaints);
        model.addAttribute("searchQuery", query);
        
        return "admin/search-results";
    }
}

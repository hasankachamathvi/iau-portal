package com.slt.iau_portal.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.slt.iau_portal.util.ExportUtil;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final List<String> VALID_STATUSES = List.of("PENDING", "UNDER_INVESTIGATION", "RESOLVED");

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "dateDesc") String sort) {

        logger.info("Admin dashboard accessed with filter={} sort={}", filter, sort);
        int maxPage = Math.max(0, page);

        Sort sortOrder = buildSort(sort);
        Pageable pageable = PageRequest.of(maxPage, 10, sortOrder);
        Page<Complaint> complaints = getComplaintsByFilter(filter, pageable);
        List<Complaint> allComplaints = complaintRepository.findAll();
        Map<Long, Reporter> reportersByComplaintId = getReportersByComplaintId(complaints.getContent());
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
        List<ReportItem> statusReport = buildStatusReport(allComplaints);
        List<ReportItem> categoryReport = buildCategoryReport(allComplaints);
        List<ReportItem> monthlyTrend = buildMonthlyTrend();

        model.addAttribute("complaints", complaints.getContent());
        model.addAttribute("reportersByComplaintId", reportersByComplaintId);
        model.addAttribute("totalComplaints", complaintRepository.count());
        model.addAttribute("escalatedCount", complaintRepository.countByEscalatedTrue());
        model.addAttribute("pendingCount", complaintRepository.countByStatus("PENDING"));
        model.addAttribute("underInvestigationCount", complaintRepository.countByStatus("UNDER_INVESTIGATION"));
        model.addAttribute("resolvedCount", complaintRepository.countByStatus("RESOLVED"));
        model.addAttribute("monthlyCount", complaintRepository.countByCreatedAtBetween(monthStart, monthEnd));
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentFilterLabel", getFilterLabel(filter));
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", complaints.getTotalPages());
        model.addAttribute("statusReport", statusReport);
        model.addAttribute("categoryReport", categoryReport);
        model.addAttribute("monthlyTrend", monthlyTrend);

        return "admin/dashboard";
    }

    @GetMapping("/complaint/{crn}")
    public String viewComplaint(@PathVariable String crn, Model model) {
        logger.info("Fetching complaint details for CRN: {}", crn);

        Complaint complaint = complaintRepository.findByCrnIgnoreCase(crn.trim()).orElse(null);

        if (complaint == null) {
            model.addAttribute("error", "Complaint not found");
            return "redirect:/admin/dashboard";
        }

        Reporter reporter = reporterRepository.findByComplaintId(complaint.getId());
        List<Subject> subjects = subjectRepository.findByComplaintId(complaint.getId());
        List<Evidence> evidence = evidenceRepository.findByComplaintId(complaint.getId());

        model.addAttribute("complaint", complaint);
        model.addAttribute("reporter", reporter);
        model.addAttribute("subjects", subjects == null ? List.of() : subjects);
        model.addAttribute("evidence", evidence == null ? List.of() : evidence);

        return "admin/complaint-detail";
    }

    @PostMapping("/complaint/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        Complaint complaint = complaintRepository.findById(id).orElse(null);
        String normalizedStatus = Optional.ofNullable(status).map(String::trim).orElse("").toUpperCase();

        if (complaint != null && VALID_STATUSES.contains(normalizedStatus)) {
            complaint.setStatus(normalizedStatus);
            complaint.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);
            logger.info("Complaint {} status updated to {}", id, normalizedStatus);
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/search")
    public String searchComplaints(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String crn,
            Model model) {

        String searchValue = query != null && !query.isBlank() ? query : crn;
        String normalizedSearchValue = searchValue == null ? "" : searchValue.trim();
        List<Complaint> complaints = normalizedSearchValue.isBlank()
            ? complaintRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent()
            : complaintRepository.findByCrnIgnoreCase(normalizedSearchValue).stream().toList();

        Map<Long, Reporter> reportersByComplaintId = getReportersByComplaintId(complaints);

        model.addAttribute("complaints", complaints);
        model.addAttribute("searchQuery", normalizedSearchValue);
        model.addAttribute("reportersByComplaintId", reportersByComplaintId);
        model.addAttribute("totalComplaints", complaintRepository.count());
        model.addAttribute("pendingCount", complaintRepository.countByStatus("PENDING"));
        model.addAttribute("escalatedCount", complaintRepository.countByEscalatedTrue());
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
        model.addAttribute("monthlyCount", complaintRepository.countByCreatedAtBetween(monthStart, monthEnd));
        model.addAttribute("currentFilter", normalizedSearchValue.isBlank() ? "all" : "search");
        model.addAttribute("currentFilterLabel", normalizedSearchValue.isBlank() ? "All Complaints" : "Search Results");
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", 1);
        model.addAttribute("underInvestigationCount", complaintRepository.countByStatus("UNDER_INVESTIGATION"));
        model.addAttribute("resolvedCount", complaintRepository.countByStatus("RESOLVED"));
        model.addAttribute("statusReport", buildStatusReport(complaints));
        model.addAttribute("categoryReport", buildCategoryReport(complaints));
        model.addAttribute("monthlyTrend", buildMonthlyTrend());

        return "admin/dashboard";
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportComplaints(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String query) {
        logger.info("Admin export requested with filter: {} query={}", filter, query);

        List<Complaint> complaints;
        String normalizedQuery = query == null ? "" : query.trim();

        if ("search".equalsIgnoreCase(filter) && !normalizedQuery.isBlank()) {
            complaints = complaintRepository.findByCrnIgnoreCase(normalizedQuery).stream().toList();
        } else {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));
            complaints = getComplaintsByFilter(filter, pageable).getContent();
        }
        
        String csvContent = ExportUtil.exportComplaintsToCSV(complaints);
        String filename = "complaints_" + System.currentTimeMillis() + ".csv";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent);
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

    private Sort buildSort(String sortParam) {
        return switch (sortParam) {
            case "dateAsc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "categoryAsc" -> Sort.by(Sort.Direction.ASC, "category");
            case "categoryDesc" -> Sort.by(Sort.Direction.DESC, "category");
            case "statusAsc" -> Sort.by(Sort.Direction.ASC, "status");
            case "statusDesc" -> Sort.by(Sort.Direction.DESC, "status");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // dateDesc
        };
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

    private List<ReportItem> buildStatusReport(List<Complaint> complaints) {
        long total = Math.max(1, complaints.size());
        return List.of(
            buildReportItem("Pending", complaints.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getStatus())).count(), total),
            buildReportItem("Under Investigation", complaints.stream().filter(c -> "UNDER_INVESTIGATION".equalsIgnoreCase(c.getStatus())).count(), total),
            buildReportItem("Resolved", complaints.stream().filter(c -> "RESOLVED".equalsIgnoreCase(c.getStatus())).count(), total),
            buildReportItem("Escalated", complaints.stream().filter(c -> Boolean.TRUE.equals(c.getEscalated())).count(), total)
        );
    }

    private List<ReportItem> buildCategoryReport(List<Complaint> complaints) {
        long total = Math.max(1, complaints.size());
        Map<String, Long> counts = complaints.stream()
            .collect(Collectors.groupingBy(
                complaint -> {
                    String category = complaint.getCategory();
                    return (category == null || category.isBlank()) ? "Unspecified" : category;
                },
                Collectors.counting()
            ));

        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(entry -> buildReportItem(entry.getKey(), entry.getValue(), total))
            .toList();
    }

    private List<ReportItem> buildMonthlyTrend() {
        List<ReportItem> trend = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = current.minusMonths(offset);
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
            long count = complaintRepository.countByCreatedAtBetween(start, end);
            trend.add(new ReportItem(month.toString(), count, 0));
        }

        long peak = Math.max(1, trend.stream().mapToLong(ReportItem::getCount).max().orElse(1));
        for (ReportItem item : trend) {
            item.setPercent(Math.round((item.getCount() * 100.0) / peak));
        }

        return trend;
    }

    private ReportItem buildReportItem(String label, long count, long total) {
        long percent = Math.round((count * 100.0) / total);
        return new ReportItem(label, count, percent);
    }

    public static class ReportItem {
        private final String label;
        private final long count;
        private long percent;

        public ReportItem(String label, long count, long percent) {
            this.label = label;
            this.count = count;
            this.percent = percent;
        }

        public String getLabel() { return label; }
        public long getCount() { return count; }
        public long getPercent() { return percent; }
        public void setPercent(long percent) { this.percent = percent; }
    }
}

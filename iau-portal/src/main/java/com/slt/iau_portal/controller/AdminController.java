package com.slt.iau_portal.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.slt.iau_portal.model.AuditLog;
import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.model.Evidence;
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.model.Subject;
import com.slt.iau_portal.repository.AuditLogRepository;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.EvidenceRepository;
import com.slt.iau_portal.repository.ReporterRepository;
import com.slt.iau_portal.repository.SubjectRepository;
import com.slt.iau_portal.service.AuditLogService;
import com.slt.iau_portal.service.EmailService;
import com.slt.iau_portal.util.ExportUtil;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final List<String> VALID_STATUSES = List.of("PENDING", "UNDER_INVESTIGATION", "RESOLVED");
    private static final int PAGE_SIZE = 10;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReporterRepository reporterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    @Value("${evidence.encryption.key:}")
    private String evidenceEncryptionKeyBase64;

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "dateDesc") String sort) {

        logger.info("Admin dashboard accessed with filter={} sort={}", filter, sort);
        int requestedPage = Math.max(0, page);

        Sort sortOrder = buildSort(sort);
        Pageable pageable = PageRequest.of(requestedPage, PAGE_SIZE, sortOrder);
        Page<Complaint> complaints = getComplaintsByFilter(filter, pageable);
        int totalPages = Math.max(1, complaints.getTotalPages());

        if (requestedPage >= totalPages) {
            requestedPage = totalPages - 1;
            pageable = PageRequest.of(requestedPage, PAGE_SIZE, sortOrder);
            complaints = getComplaintsByFilter(filter, pageable);
        }

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
        model.addAttribute("currentPage", requestedPage + 1);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageResultCount", complaints.getTotalElements());
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

        auditLogService.record("COMPLAINT_VIEWED", complaint.getCrn(), "ADMIN", "Viewed complaint detail page");

        return "admin/complaint-detail";
    }

    @PostMapping("/complaint/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        Complaint complaint = complaintRepository.findById(id).orElse(null);
        String normalizedStatus = Optional.ofNullable(status).map(String::trim).orElse("").toUpperCase();

        if (complaint != null && VALID_STATUSES.contains(normalizedStatus)) {
            String previousStatus = complaint.getStatus();
            complaint.setStatus(normalizedStatus);
            complaintRepository.save(complaint);
            logger.info("Complaint {} status updated to {}", id, normalizedStatus);

            // Record audit with previous and new status
            auditLogService.record("STATUS_UPDATED", complaint.getCrn(), "ADMIN", "from=" + previousStatus + " to=" + normalizedStatus);

            // Notify reporter by email if not anonymous
            try {
                com.slt.iau_portal.model.Reporter reporter = reporterRepository.findByComplaintId(id);
                if (reporter != null && Boolean.FALSE.equals(reporter.getAnonymousFlag()) && reporter.getEmail() != null && !reporter.getEmail().isBlank()) {
                    emailService.sendStatusUpdateEmail(reporter.getEmail(), complaint.getCrn(), normalizedStatus);
                }
            } catch (Exception e) {
                logger.warn("Failed to send status update notification for complaint {}", id, e);
            }
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
        model.addAttribute("pageResultCount", complaints.size());
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

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportComplaintsPdf(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String query) {
        logger.info("Admin PDF export requested with filter: {} query={}", filter, query);

        List<Complaint> complaints;
        String normalizedQuery = query == null ? "" : query.trim();

        if ("search".equalsIgnoreCase(filter) && !normalizedQuery.isBlank()) {
            complaints = complaintRepository.findByCrnIgnoreCase(normalizedQuery).stream().toList();
        } else {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));
            complaints = getComplaintsByFilter(filter, pageable).getContent();
        }

        byte[] pdfBytes = buildComplaintsPdf(complaints, filter, normalizedQuery);
        String filename = "complaints_" + System.currentTimeMillis() + ".pdf";

        auditLogService.record("EXPORT_PDF", null, "ADMIN", "filter=" + filter + ", query=" + normalizedQuery);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/audit-logs")
        public String viewAuditLogs(
            Model model,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "0") int page) {

        int requestedPage = Math.max(0, page);
        Pageable pageable = PageRequest.of(requestedPage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> logPage = auditLogService.findLogs(query, eventType, pageable);
        int totalPages = Math.max(1, logPage.getTotalPages());

        if (requestedPage >= totalPages) {
            requestedPage = totalPages - 1;
            pageable = PageRequest.of(requestedPage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
            logPage = auditLogService.findLogs(query, pageable);
        }

        model.addAttribute("auditLogs", logPage.getContent());
        model.addAttribute("searchQuery", query == null ? "" : query.trim());
        java.util.List<String> eventTypes = auditLogService.getDistinctEventTypes();
        model.addAttribute("eventTypes", eventTypes);
        model.addAttribute("selectedEventType", eventType == null ? "all" : eventType);
        model.addAttribute("currentPage", requestedPage + 1);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalAuditLogs", logPage.getTotalElements());
        model.addAttribute("auditEventCount", auditLogRepository.count());

        auditLogService.record("AUDIT_LOG_VIEWED", null, "ADMIN", "Viewed audit log page");

        return "admin/audit-logs";
    }

    @GetMapping("/evidence/{id}/download")
    public ResponseEntity<byte[]> downloadEvidence(@PathVariable Long id) {
        Evidence evidence = evidenceRepository.findById(id).orElse(null);

        if (evidence == null || evidence.getFilePath() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Path filePath = Paths.get(evidence.getFilePath());
            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = evidence.getFileType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : evidence.getFileType();

            // Attempt to decrypt if encryption key is configured
            String encKey = System.getProperty("evidence.encryption.key");
            if ((encKey == null || encKey.isBlank()) && evidence != null) {
                // try reading from Spring property fallback via env var
                encKey = System.getenv("EVIDENCE_ENCRYPTION_KEY_BASE64");
            }

            byte[] payload = fileBytes;
            if (encKey != null && !encKey.isBlank()) {
                try {
                    byte[] key = com.slt.iau_portal.util.EncryptionUtil.decodeKeyFromBase64(encKey);
                    payload = com.slt.iau_portal.util.EncryptionUtil.decrypt(fileBytes, key);
                } catch (Exception ex) {
                    logger.warn("Failed to decrypt evidence {}, returning raw bytes", id, ex);
                }
            } else if (evidenceEncryptionKeyBase64 != null && !evidenceEncryptionKeyBase64.isBlank()) {
                try {
                    byte[] key = com.slt.iau_portal.util.EncryptionUtil.decodeKeyFromBase64(evidenceEncryptionKeyBase64);
                    payload = com.slt.iau_portal.util.EncryptionUtil.decrypt(fileBytes, key);
                } catch (Exception ex) {
                    logger.warn("Failed to decrypt evidence {} with application key, returning raw bytes", id, ex);
                }
            }

            auditLogService.record("EVIDENCE_DOWNLOADED", evidence.getComplaint() != null ? evidence.getComplaint().getCrn() : null, "ADMIN", evidence.getFileName());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + evidence.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(payload);
        } catch (Exception e) {
            logger.error("Failed to download evidence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private byte[] buildComplaintsPdf(List<Complaint> complaints, String filter, String query) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 780);
                contentStream.showText("SLTMobitel IAU Complaint Ledger");
                contentStream.newLineAtOffset(0, -24);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Filter: " + filter + (query == null || query.isBlank() ? "" : " | Query: " + query));
                contentStream.newLineAtOffset(0, -18);

                int lineCount = 0;
                for (Complaint complaint : complaints) {
                    String line = String.format("%s | %s | %s | %s | %s",
                        safeText(complaint.getCrn()),
                        safeText(complaint.getCategory()),
                        safeText(complaint.getStatus()),
                        safeText(complaint.getLocation()),
                        complaint.getCreatedAt() == null ? "" : complaint.getCreatedAt().toString());

                    contentStream.showText(truncatePdfLine(line));
                    contentStream.newLineAtOffset(0, -14);
                    lineCount++;

                    if (lineCount >= 42) {
                        break;
                    }
                }

                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate PDF export", e);
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value.replace("\n", " ").replace("\r", " ");
    }

    private String truncatePdfLine(String value) {
        if (value.length() <= 120) {
            return value;
        }

        return value.substring(0, 117) + "...";
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

    @GetMapping("/users")
    public String users(Model model) {
        List<Reporter> reporters = reporterRepository.findAll();
        model.addAttribute("reporters", reporters == null ? List.of() : reporters);
        auditLogService.record("USERS_VIEWED", null, "ADMIN", "Viewed users page");
        return "admin/users";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        List<Complaint> allComplaints = complaintRepository.findAll();

        Map<String, Long> counts = allComplaints.stream()
            .collect(Collectors.groupingBy(c -> {
                String cat = c.getCategory();
                return (cat == null || cat.isBlank()) ? "Unspecified" : cat;
            }, Collectors.counting()));

        List<Map.Entry<String, Long>> topCategories = counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());

        List<ReportItem> categoryReport = buildCategoryReport(allComplaints);
        model.addAttribute("categoryReport", categoryReport);
        List<String> catLabels = categoryReport.stream().map(ReportItem::getLabel).toList();
        List<Long> catCounts = categoryReport.stream().map(ReportItem::getCount).toList();
        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catCounts", catCounts);
        model.addAttribute("statusReport", buildStatusReport(allComplaints));
        model.addAttribute("topCategories", topCategories);
        model.addAttribute("topTenComplaints", allComplaints.stream().sorted(Comparator.comparing(Complaint::getCreatedAt).reversed()).limit(10).collect(Collectors.toList()));

        auditLogService.record("REPORTS_VIEWED", null, "ADMIN", "Viewed reports page");
        return "admin/reports";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        // simple placeholders for UI
        model.addAttribute("emailNotifications", true);
        model.addAttribute("evidenceEncryptionKey", System.getProperty("EVIDENCE_ENCRYPTION_KEY_BASE64", ""));
        auditLogService.record("SETTINGS_VIEWED", null, "ADMIN", "Viewed settings page");
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam(required = false, defaultValue = "") String evidenceEncryptionKeyBase64,
                               @RequestParam(required = false, defaultValue = "false") boolean emailNotifications,
                               Model model) {
        if (evidenceEncryptionKeyBase64 != null && !evidenceEncryptionKeyBase64.isBlank()) {
            System.setProperty("EVIDENCE_ENCRYPTION_KEY_BASE64", evidenceEncryptionKeyBase64);
        }
        // we only record an audit entry and redirect back to settings
        auditLogService.record("SETTINGS_SAVED", null, "ADMIN", "emailNotifications=" + emailNotifications);
        return "redirect:/admin/settings";
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

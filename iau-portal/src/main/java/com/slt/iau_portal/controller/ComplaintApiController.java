package com.slt.iau_portal.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.slt.iau_portal.dto.ApiResponse;
import com.slt.iau_portal.dto.ComplaintFormDto;
import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.service.AuditLogService;
import com.slt.iau_portal.service.ComplaintService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintApiController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintApiController.class);

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Submit a new complaint via API
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<String>> submitComplaintApi(@Valid @RequestBody ComplaintFormDto form) {
        logger.info("API complaint submission received - Category: {}", form.getCategory());
        try {
            String crn = complaintService.processComplaint(form);
            auditLogService.record("API_COMPLAINT_SUBMITTED", crn, "API", "category=" + form.getCategory());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully", crn));
        } catch (Exception e) {
            logger.error("API complaint submission failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to submit complaint", e.getMessage()));
        }
    }

    /**
     * Get complaint by CRN
     */
    @GetMapping("/crn/{crn}")
    public ResponseEntity<ApiResponse<Complaint>> getComplaintByCrn(@PathVariable String crn) {
        logger.info("Fetching complaint via API - CRN: {}", crn);
        try {
            Optional<Complaint> complaint = complaintRepository.findByCrn(crn);

            if (complaint.isPresent()) {
                auditLogService.record("API_COMPLAINT_VIEWED", complaint.get().getCrn(), "API", "Complaint fetched by CRN");
                return ResponseEntity.ok(ApiResponse.success("Complaint found", complaint.get()));
            } else {
                logger.warn("Complaint not found - CRN: {}", crn);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Complaint not found", "No complaint with CRN: " + crn));
            }
        } catch (DataAccessException dae) {
            logger.error("Database error while fetching CRN {}", crn, dae);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Service temporarily unavailable", "Database is unreachable"));
        } catch (Exception e) {
            logger.error("Unexpected error while fetching CRN {}", crn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error", e.getMessage()));
        }
    }

    /**
     * Get complaints by status
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Complaint>>> getComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        
        logger.info("Fetching complaints via API - page: {}, size: {}, status: {}, category: {}", 
            page, size, status, category);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Complaint> complaints;

        if (status != null && category != null) {
            complaints = complaintRepository.findByStatusAndCategory(status, category, pageable);
        } else if (status != null) {
            complaints = complaintRepository.findByStatus(status, pageable);
        } else if (category != null) {
            complaints = complaintRepository.findByCategory(category, pageable);
        } else {
            complaints = complaintRepository.findAll(pageable);
        }

        auditLogService.record("API_COMPLAINT_LISTED", null, "API", "page=" + page + ", size=" + size);

        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved", complaints));
    }

    /**
     * Temporary test endpoint to return a synthetic complaint for UI testing.
     * Not intended for production use.
     */
    @GetMapping("/test/{crn}")
    public ResponseEntity<ApiResponse<Complaint>> testComplaint(@PathVariable String crn) {
        Complaint c = new Complaint();
        c.setCrn(crn);
        c.setCategory("TEST");
        c.setDescription("This is a synthetic test complaint generated for UI testing.");
        c.setComplaintDate(LocalDate.now());
        c.setLocation("Test Location");
        c.setStatus("PENDING");
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Test complaint", c));
    }

    /**
     * Get complaint statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics() {
        logger.info("Fetching complaint statistics via API");
        
        Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", complaintRepository.count());
        stats.put("pending", complaintRepository.countByStatus("PENDING"));
        stats.put("under_investigation", complaintRepository.countByStatus("UNDER_INVESTIGATION"));
        stats.put("resolved", complaintRepository.countByStatus("RESOLVED"));

        auditLogService.record("API_STATISTICS_VIEWED", null, "API", "Complaint statistics retrieved");

        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));
    }
}

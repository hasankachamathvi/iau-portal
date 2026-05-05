package com.slt.iau_portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;

import com.slt.iau_portal.dto.ApiResponse;
import com.slt.iau_portal.dto.ComplaintFormDto;
import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.service.ComplaintService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/complaints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ComplaintApiController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintApiController.class);

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    /**
     * Submit a new complaint via API
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<String>> submitComplaintApi(@Valid @RequestBody ComplaintFormDto form) {
        logger.info("API complaint submission received - Category: {}", form.getCategory());
        try {
            String crn = complaintService.processComplaint(form);
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
        Optional<Complaint> complaint = complaintRepository.findByCrn(crn);
        
        if (complaint.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Complaint found", complaint.get()));
        } else {
            logger.warn("Complaint not found - CRN: {}", crn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Complaint not found", "No complaint with CRN: " + crn));
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

        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved", complaints));
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

        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));
    }
}

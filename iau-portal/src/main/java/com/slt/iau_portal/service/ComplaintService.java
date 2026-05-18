package com.slt.iau_portal.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slt.iau_portal.dto.ComplaintFormDto;
import com.slt.iau_portal.exception.ComplaintProcessingException;
import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.model.Evidence;
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.model.Subject;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.EvidenceRepository;
import com.slt.iau_portal.repository.ReporterRepository;
import com.slt.iau_portal.repository.SubjectRepository;
import com.slt.iau_portal.service.AuditLogService;
import com.slt.iau_portal.util.CrnGenerator;
import com.slt.iau_portal.util.ValidationUtil;

@Service
public class ComplaintService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReporterRepository reporterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private CrnGenerator crnGenerator;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${evidence.encryption.key:}")
    private String evidenceEncryptionKeyBase64;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES = 5;

    public String processComplaint(ComplaintFormDto form) {
        try {
            logger.info("Starting complaint processing");
            String crn = crnGenerator.generate();
            logger.info("Generated CRN: {}", crn);

            // Create complaint
            Complaint complaint = new Complaint();
            complaint.setCrn(crn);
            complaint.setCategory(form.getCategory());
            complaint.setDescription(form.getDescription());
            complaint.setComplaintDate(form.getComplaintDate());
            complaint.setLocation(form.getLocation());
            complaint.setReportedBefore(form.isReportedBefore());
            complaint.setEscalated(form.isSeniorManagement());
            complaint.setCiabocEscalated(shouldEscalateToCiaboc(form));
            complaint.setStatus("PENDING");
            complaintRepository.save(complaint);
            logger.info("Complaint saved with ID: {}", complaint.getId());

            // Save reporter information
            Reporter reporter = new Reporter();
            reporter.setComplaint(complaint);
            reporter.setAnonymousFlag(form.isAnonymous());
            if (!form.isAnonymous()) {
                reporter.setFullName(form.getFullName());
                reporter.setEmail(form.getEmail());
                reporter.setPhone(form.getPhone());
                reporter.setEmployeeId(form.getEmployeeId());
                logger.info("Non-anonymous complaint from: {}", form.getFullName());
            } else {
                logger.info("Anonymous complaint submitted");
            }
            reporterRepository.save(reporter);

            // Save subject information
            if (form.getSubjectName() != null && !form.getSubjectName().isEmpty()) {
                Subject subject = new Subject();
                subject.setComplaint(complaint);
                subject.setFullName(form.getSubjectName());
                subject.setRole(form.getSubjectRole());
                subject.setOrganization(form.getSubjectOrganization());
                subject.setRelationship(form.getSubjectRelationship());
                subjectRepository.save(subject);
                logger.info("Subject information saved for: {}", form.getSubjectName());
            }

            // Handle file uploads
            if (form.getEvidenceFiles() != null && !form.getEvidenceFiles().isEmpty()) {
                processEvidenceFiles(form.getEvidenceFiles(), complaint);
            }

            // Send email notification if not anonymous
            if (!form.isAnonymous() && form.getEmail() != null && !form.getEmail().isEmpty()) {
                try {
                    emailService.sendConfirmationEmail(form.getEmail(), crn, form.getCategory());
                    logger.info("Confirmation email sent to: {}", form.getEmail());
                } catch (Exception e) {
                    logger.warn("Failed to send confirmation email to: {} - {}", form.getEmail(), e.getMessage());
                }
            }

            auditLogService.record(
                "COMPLAINT_SUBMITTED",
                crn,
                form.isAnonymous() ? "ANONYMOUS" : form.getEmail(),
                "category=" + form.getCategory() + ", escalated=" + complaint.getEscalated() + ", ciabocEscalated=" + complaint.getCiabocEscalated()
            );

            logger.info("Complaint processing completed successfully. CRN: {}", crn);
            return crn;
            
        } catch (Exception e) {
            logger.error("Error processing complaint", e);
            throw new ComplaintProcessingException("Failed to process complaint: " + e.getMessage(), e);
        }
    }

    private void processEvidenceFiles(List<MultipartFile> files, Complaint complaint) throws IOException {
        int fileCount = 0;
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Validate file count
                if (fileCount >= MAX_FILES) {
                    throw new ComplaintProcessingException("Maximum " + MAX_FILES + " files allowed");
                }
                
                // Validate file size
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new ComplaintProcessingException(
                        "File " + file.getOriginalFilename() + " exceeds 10MB limit"
                    );
                }
                
                // Validate filename
                if (!ValidationUtil.isValidFileName(file.getOriginalFilename())) {
                    throw new ComplaintProcessingException(
                        "Invalid filename: " + file.getOriginalFilename()
                    );
                }
                
                // Validate file type
                String contentType = file.getContentType();
                if (!isAllowedFileType(contentType)) {
                    throw new ComplaintProcessingException(
                        "File type not allowed for " + file.getOriginalFilename()
                    );
                }
                
                try {
                    // Create upload directory
                    File uploadFolder = new File(uploadDir);
                    if (!uploadFolder.exists()) {
                        if (!uploadFolder.mkdirs()) {
                            throw new IOException("Failed to create upload directory");
                        }
                    }

                    // Generate unique filename to prevent overwrite
                    String uniqueFilename = System.currentTimeMillis() + "_" + sanitizeFilename(file.getOriginalFilename());
                    String filePath = uploadDir + File.separator + uniqueFilename;

                    byte[] bytes = file.getBytes();
                    try {
                        if (evidenceEncryptionKeyBase64 != null && !evidenceEncryptionKeyBase64.isBlank()) {
                            byte[] key = com.slt.iau_portal.util.EncryptionUtil.decodeKeyFromBase64(evidenceEncryptionKeyBase64);
                            byte[] encrypted = com.slt.iau_portal.util.EncryptionUtil.encrypt(bytes, key);
                            java.nio.file.Files.write(java.nio.file.Paths.get(filePath), encrypted);
                            logger.info("File uploaded and encrypted successfully: {}", uniqueFilename);
                        } else {
                            // fallback: write plaintext and log warning
                            file.transferTo(new File(filePath));
                            logger.warn("Evidence encryption key not configured; storing file in plaintext: {}", uniqueFilename);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to encrypt and save file: {}", uniqueFilename, e);
                        throw new ComplaintProcessingException("Failed to upload/encrypt file: " + file.getOriginalFilename(), e);
                    }

                    // Save evidence record
                    Evidence evidence = new Evidence();
                    evidence.setComplaint(complaint);
                    evidence.setFileName(file.getOriginalFilename());
                    evidence.setFilePath(filePath);
                    evidence.setFileType(contentType);
                    evidenceRepository.save(evidence);
                    
                    fileCount++;
                    
                } catch (IOException e) {
                    logger.error("Error uploading file: {}", file.getOriginalFilename(), e);
                    throw new ComplaintProcessingException("Failed to upload file: " + file.getOriginalFilename(), e);
                }
            }
        }
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) return false;
        
        String[] allowedTypes = {
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
        
        for (String allowed : allowedTypes) {
            if (contentType.equals(allowed)) {
                return true;
            }
        }
        
        logger.warn("Unsupported file type attempted: {}", contentType);
        return false;
    }

    private boolean shouldEscalateToCiaboc(ComplaintFormDto form) {
        if (form.isSeniorManagement()) {
            return true;
        }

        String subjectRole = form.getSubjectRole() == null ? "" : form.getSubjectRole().toLowerCase();
        String subjectOrganization = form.getSubjectOrganization() == null ? "" : form.getSubjectOrganization().toLowerCase();
        String subjectRelationship = form.getSubjectRelationship() == null ? "" : form.getSubjectRelationship().toLowerCase();
        String subjectName = form.getSubjectName() == null ? "" : form.getSubjectName().toLowerCase();

        return subjectRole.contains("iau")
            || subjectOrganization.contains("iau")
            || subjectRelationship.contains("iau")
            || subjectName.contains("iau")
            || subjectRole.contains("internal affairs")
            || subjectOrganization.contains("internal affairs");
    }
}

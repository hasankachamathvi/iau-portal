package com.slt.iau_portal.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.slt.iau_portal.dto.ComplaintFormDto;
import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.model.Evidence;
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.model.Subject;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.EvidenceRepository;
import com.slt.iau_portal.repository.ReporterRepository;
import com.slt.iau_portal.repository.SubjectRepository;
import com.slt.iau_portal.util.CrnGenerator;

@Service
public class ComplaintService {

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

    @Value("${upload.dir}")
    private String uploadDir;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES = 5;

    public String processComplaint(ComplaintFormDto form) throws IOException {
        String crn = crnGenerator.generate();

        Complaint complaint = new Complaint();
        complaint.setCrn(crn);
        complaint.setCategory(form.getCategory());
        complaint.setDescription(form.getDescription());
        complaint.setComplaintDate(form.getComplaintDate());
        complaint.setLocation(form.getLocation());
        complaint.setReportedBefore(form.isReportedBefore());
        complaint.setEscalated(form.isSeniorManagement());
        complaint.setStatus("PENDING");
        complaintRepository.save(complaint);

        Reporter reporter = new Reporter();
        reporter.setComplaint(complaint);
        reporter.setAnonymousFlag(form.isAnonymous());
        if (!form.isAnonymous()) {
            reporter.setFullName(form.getFullName());
            reporter.setEmail(form.getEmail());
            reporter.setPhone(form.getPhone());
            reporter.setEmployeeId(form.getEmployeeId());
        }
        reporterRepository.save(reporter);

        if (form.getSubjectName() != null && !form.getSubjectName().isEmpty()) {
            Subject subject = new Subject();
            subject.setComplaint(complaint);
            subject.setFullName(form.getSubjectName());
            subject.setRole(form.getSubjectRole());
            subject.setOrganization(form.getSubjectOrganization());
            subject.setRelationship(form.getSubjectRelationship());
            subjectRepository.save(subject);
        }

        // Handle file uploads with validation
        if (form.getEvidenceFiles() != null) {
            List<MultipartFile> files = form.getEvidenceFiles();
            int fileCount = 0;
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Validate file count
                    if (fileCount >= MAX_FILES) {
                        throw new IOException("Maximum " + MAX_FILES + " files allowed");
                    }
                    
                    // Validate file size
                    if (file.getSize() > MAX_FILE_SIZE) {
                        throw new IOException("File " + file.getOriginalFilename() + " exceeds 10MB limit");
                    }
                    
                    // Validate file type
                    String contentType = file.getContentType();
                    if (!isAllowedFileType(contentType)) {
                        throw new IOException("File type not allowed for " + file.getOriginalFilename());
                    }
                    
                    // Create upload directory
                    File uploadFolder = new File(uploadDir);
                    if (!uploadFolder.exists()) {
                        uploadFolder.mkdirs();
                    }

                    // Generate unique filename to prevent overwrite
                    String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    String filePath = uploadDir + File.separator + uniqueFilename;
                    file.transferTo(new File(filePath));

                    Evidence evidence = new Evidence();
                    evidence.setComplaint(complaint);
                    evidence.setFileName(file.getOriginalFilename());
                    evidence.setFilePath(filePath);
                    evidence.setFileType(contentType);
                    evidenceRepository.save(evidence);
                    
                    fileCount++;
                }
            }
        }

        // Send email notification if not anonymous
        if (!form.isAnonymous() && form.getEmail() != null && !form.getEmail().isEmpty()) {
            emailService.sendConfirmationEmail(form.getEmail(), crn, form.getCategory());
        }

        return crn;
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
        return false;
    }
}

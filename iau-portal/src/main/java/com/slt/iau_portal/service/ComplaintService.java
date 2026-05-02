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

    @Value("${upload.dir}")
    private String uploadDir;

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

        if (form.getEvidenceFiles() != null) {
            List<MultipartFile> files = form.getEvidenceFiles();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    File uploadFolder = new File(uploadDir);
                    if (!uploadFolder.exists()) {
                        uploadFolder.mkdirs();
                    }

                    String filePath = uploadDir + File.separator + file.getOriginalFilename();
                    file.transferTo(new File(filePath));

                    Evidence evidence = new Evidence();
                    evidence.setComplaint(complaint);
                    evidence.setFileName(file.getOriginalFilename());
                    evidence.setFilePath(filePath);
                    evidence.setFileType(file.getContentType());
                    evidenceRepository.save(evidence);
                }
            }
        }

        return crn;
    }
}

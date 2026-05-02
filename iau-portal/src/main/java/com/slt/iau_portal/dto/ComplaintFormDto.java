package com.slt.iau_portal.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class ComplaintFormDto {

    private boolean anonymous;
    private String fullName;
    private String email;
    private String phone;
    private String employeeId;

    private String category;
    private String description;
    private LocalDate complaintDate;
    private String location;
    private boolean reportedBefore;

    private String subjectName;
    private String subjectRole;
    private String subjectOrganization;
    private String subjectRelationship;
    private boolean seniorManagement;

    private List<MultipartFile> evidenceFiles;
    private String witnessNames;
    private String additionalNotes;

    private boolean declarationChecked;
    private boolean consentChecked;

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getComplaintDate() { return complaintDate; }
    public void setComplaintDate(LocalDate complaintDate) { this.complaintDate = complaintDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isReportedBefore() { return reportedBefore; }
    public void setReportedBefore(boolean reportedBefore) { this.reportedBefore = reportedBefore; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectRole() { return subjectRole; }
    public void setSubjectRole(String subjectRole) { this.subjectRole = subjectRole; }

    public String getSubjectOrganization() { return subjectOrganization; }
    public void setSubjectOrganization(String subjectOrganization) { this.subjectOrganization = subjectOrganization; }

    public String getSubjectRelationship() { return subjectRelationship; }
    public void setSubjectRelationship(String subjectRelationship) { this.subjectRelationship = subjectRelationship; }

    public boolean isSeniorManagement() { return seniorManagement; }
    public void setSeniorManagement(boolean seniorManagement) { this.seniorManagement = seniorManagement; }

    public List<MultipartFile> getEvidenceFiles() { return evidenceFiles; }
    public void setEvidenceFiles(List<MultipartFile> evidenceFiles) { this.evidenceFiles = evidenceFiles; }

    public String getWitnessNames() { return witnessNames; }
    public void setWitnessNames(String witnessNames) { this.witnessNames = witnessNames; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public boolean isDeclarationChecked() { return declarationChecked; }
    public void setDeclarationChecked(boolean declarationChecked) { this.declarationChecked = declarationChecked; }

    public boolean isConsentChecked() { return consentChecked; }
    public void setConsentChecked(boolean consentChecked) { this.consentChecked = consentChecked; }
}

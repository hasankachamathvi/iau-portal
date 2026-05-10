package com.slt.iau_portal.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

public class ComplaintFormDto {

    private boolean anonymous;
    
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;
    
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$|^$", message = "Please provide a valid phone number")
    private String phone;
    
    private String employeeId;

    @NotBlank(message = "Please select a complaint category")
    private String category;
    
    @NotBlank(message = "Complaint description is required")
    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;
    
    @NotNull(message = "Date of incident is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate complaintDate;
    
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    private boolean reportedBefore;

    @NotBlank(message = "Name of person involved is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String subjectName;
    
    @Size(max = 100, message = "Role must not exceed 100 characters")
    private String subjectRole;
    
    @Size(max = 100, message = "Organization must not exceed 100 characters")
    private String subjectOrganization;
    
    @Size(max = 200, message = "Relationship must not exceed 200 characters")
    private String subjectRelationship;
    
    private boolean seniorManagement;

    private List<MultipartFile> evidenceFiles;
    
    @Size(max = 500, message = "Witness names must not exceed 500 characters")
    private String witnessNames;
    
    @Size(max = 500, message = "Additional notes must not exceed 500 characters")
    private String additionalNotes;

    @AssertTrue(message = "You must declare that the information is truthful")
    private boolean declarationChecked;
    
    @AssertTrue(message = "You must consent to the data processing")
    private boolean consentChecked;

    @NotBlank(message = "Please solve the verification challenge")
    private String captchaAnswer;

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

    public String getCaptchaAnswer() { return captchaAnswer; }
    public void setCaptchaAnswer(String captchaAnswer) { this.captchaAnswer = captchaAnswer; }
}

package com.slt.iau_portal.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ComplaintFormDto {

    // Section 1: Reporter Information
    @NotBlank(message = "Submission type is required")
    private String submissionType; // named or anonymous
    
    @NotBlank(message = "Reporter category is required")
    private String reporterCategory;
    
    @NotBlank(message = "Full name is required for named submissions")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;
    
    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    private String employeeId;
    
    @Size(max = 100, message = "Division must not exceed 100 characters")
    private String division;
    
    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required for named submissions")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$|^$", message = "Phone number must be in valid format")
    private String phone;
    
    private String preferredContactMethod; // email or phone

    // Section 2: Complaint Details
    @NotBlank(message = "Complaint category is required")
    private String category;
    
    @NotNull(message = "Date of incident is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate incidentDate;
    
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    private String frequency; // one-time, repeated-periodic, ongoing
    
    @NotBlank(message = "Complaint description is required")
    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;
    
    private String becameAware; // direct-witness, informed-by-other, discovered-documents, other
    
    private String reportedPreviously; // yes, no
    
    @Size(max = 1000, message = "Previous outcome must not exceed 1000 characters")
    private String previousOutcome;

    // Section 3: Subject(s) of Complaint
    @Size(max = 500, message = "Subject names must not exceed 500 characters")
    private String subjectNames;
    
    @Size(max = 100, message = "Subject role must not exceed 100 characters")
    private String subjectRole;
    
    private String subjectOrganisation;
    
    private String subjectRelationship;
    
    private String involvesSeniorManagement; // yes, no, unsure
    
    @Size(max = 500, message = "Senior personnel names must not exceed 500 characters")
    private String seniorPersonnelNames;

    // Section 4: Supporting Evidence
    private String hasEvidence; // yes, no
    
    private List<String> evidenceTypes; // documents, email, photographs, videos, witness, financial, other
    
    private List<MultipartFile> evidenceFiles;
    
    @Size(max = 500, message = "Witness names must not exceed 500 characters")
    private String witnessNames;
    
    @Size(max = 2000, message = "Additional information must not exceed 2000 characters")
    private String additionalInfo;

    // Section 5: Declaration & Submission
    @AssertTrue(message = "You must confirm the declaration")
    private boolean declaration1;
    
    @AssertTrue(message = "You must acknowledge the monitoring notice")
    private boolean declaration2;

    // Getters and Setters
    public String getSubmissionType() { return submissionType; }
    public void setSubmissionType(String submissionType) { this.submissionType = submissionType; }

    public String getReporterCategory() { return reporterCategory; }
    public void setReporterCategory(String reporterCategory) { this.reporterCategory = reporterCategory; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBecameAware() { return becameAware; }
    public void setBecameAware(String becameAware) { this.becameAware = becameAware; }

    public String getReportedPreviously() { return reportedPreviously; }
    public void setReportedPreviously(String reportedPreviously) { this.reportedPreviously = reportedPreviously; }

    public String getPreviousOutcome() { return previousOutcome; }
    public void setPreviousOutcome(String previousOutcome) { this.previousOutcome = previousOutcome; }

    public String getSubjectNames() { return subjectNames; }
    public void setSubjectNames(String subjectNames) { this.subjectNames = subjectNames; }

    public String getSubjectRole() { return subjectRole; }
    public void setSubjectRole(String subjectRole) { this.subjectRole = subjectRole; }

    public String getSubjectOrganisation() { return subjectOrganisation; }
    public void setSubjectOrganisation(String subjectOrganisation) { this.subjectOrganisation = subjectOrganisation; }

    public String getSubjectRelationship() { return subjectRelationship; }
    public void setSubjectRelationship(String subjectRelationship) { this.subjectRelationship = subjectRelationship; }

    public String getInvolvesSeniorManagement() { return involvesSeniorManagement; }
    public void setInvolvesSeniorManagement(String involvesSeniorManagement) { this.involvesSeniorManagement = involvesSeniorManagement; }

    public String getSeniorPersonnelNames() { return seniorPersonnelNames; }
    public void setSeniorPersonnelNames(String seniorPersonnelNames) { this.seniorPersonnelNames = seniorPersonnelNames; }

    public String getHasEvidence() { return hasEvidence; }
    public void setHasEvidence(String hasEvidence) { this.hasEvidence = hasEvidence; }

    public List<String> getEvidenceTypes() { return evidenceTypes; }
    public void setEvidenceTypes(List<String> evidenceTypes) { this.evidenceTypes = evidenceTypes; }

    public List<MultipartFile> getEvidenceFiles() { return evidenceFiles; }
    public void setEvidenceFiles(List<MultipartFile> evidenceFiles) { this.evidenceFiles = evidenceFiles; }

    public String getWitnessNames() { return witnessNames; }
    public void setWitnessNames(String witnessNames) { this.witnessNames = witnessNames; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public boolean isDeclaration1() { return declaration1; }
    public void setDeclaration1(boolean declaration1) { this.declaration1 = declaration1; }

    public boolean isDeclaration2() { return declaration2; }
    public void setDeclaration2(boolean declaration2) { this.declaration2 = declaration2; }
}

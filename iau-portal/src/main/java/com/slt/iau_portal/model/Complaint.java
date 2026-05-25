package com.slt.iau_portal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String crn;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate complaintDate;
    private String location;
    @Convert(converter = com.slt.iau_portal.util.ListToJsonConverter.class)
    @Column(columnDefinition = "JSON")
    private java.util.List<String> evidenceTypes;
    private String witnessNames;
    @Column(columnDefinition = "LONGTEXT")
    private String additionalInfo;
    private Boolean reportedBefore = false;
    private Boolean escalated = false;
    private Boolean ciabocEscalated = false;
    private String status = "PENDING";

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private Boolean declarationConfirmed = false;
    private Boolean declarationAcknowledged = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCrn() { return crn; }
    public void setCrn(String crn) { this.crn = crn; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getComplaintDate() { return complaintDate; }
    public void setComplaintDate(LocalDate complaintDate) { this.complaintDate = complaintDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public java.util.List<String> getEvidenceTypes() { return evidenceTypes; }
    public void setEvidenceTypes(java.util.List<String> evidenceTypes) { this.evidenceTypes = evidenceTypes; }

    public String getWitnessNames() { return witnessNames; }
    public void setWitnessNames(String witnessNames) { this.witnessNames = witnessNames; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public Boolean getReportedBefore() { return reportedBefore; }
    public void setReportedBefore(Boolean reportedBefore) { this.reportedBefore = reportedBefore; }

    public Boolean getEscalated() { return escalated; }
    public void setEscalated(Boolean escalated) { this.escalated = escalated; }

    public Boolean getCiabocEscalated() { return ciabocEscalated; }
    public void setCiabocEscalated(Boolean ciabocEscalated) { this.ciabocEscalated = ciabocEscalated; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getDeclarationConfirmed() { return declarationConfirmed; }
    public void setDeclarationConfirmed(Boolean declarationConfirmed) { this.declarationConfirmed = declarationConfirmed; }

    public Boolean getDeclarationAcknowledged() { return declarationAcknowledged; }
    public void setDeclarationAcknowledged(Boolean declarationAcknowledged) { this.declarationAcknowledged = declarationAcknowledged; }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

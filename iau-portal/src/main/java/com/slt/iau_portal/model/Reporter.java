package com.slt.iau_portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reporter")
public class Reporter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    private Boolean anonymousFlag = false;
    private String fullName;
    private String email;
    private String phone;
    private String employeeId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public Boolean getAnonymousFlag() { return anonymousFlag; }
    public void setAnonymousFlag(Boolean anonymousFlag) { this.anonymousFlag = anonymousFlag; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}

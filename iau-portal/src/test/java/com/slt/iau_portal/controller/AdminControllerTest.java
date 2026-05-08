package com.slt.iau_portal.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.slt.iau_portal.model.Complaint;
import com.slt.iau_portal.model.Evidence;
import com.slt.iau_portal.model.Reporter;
import com.slt.iau_portal.model.Subject;
import com.slt.iau_portal.repository.ComplaintRepository;
import com.slt.iau_portal.repository.EvidenceRepository;
import com.slt.iau_portal.repository.ReporterRepository;
import com.slt.iau_portal.repository.SubjectRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComplaintRepository complaintRepository;

    @MockBean
    private ReporterRepository reporterRepository;

    @MockBean
    private SubjectRepository subjectRepository;

    @MockBean
    private EvidenceRepository evidenceRepository;

    @Test
    void dashboardFilterShowsPendingComplaints() throws Exception {
        Complaint complaint = buildComplaint(1L, "IAU-2026-000101", "PENDING");
        Page<Complaint> page = new PageImpl<>(List.of(complaint), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        when(complaintRepository.findByStatusOrderByCreatedAtDesc("PENDING", any(Pageable.class))).thenReturn(page);
        stubCounts();
        when(reporterRepository.findByComplaintId(1L)).thenReturn(buildReporter(complaint, false));

        mockMvc.perform(get("/admin/dashboard").param("filter", "pending"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("currentFilterLabel", "Pending Complaints"))
                .andExpect(content().string(containsString("IAU-2026-000101")));

        verify(complaintRepository).findByStatusOrderByCreatedAtDesc("PENDING", any(Pageable.class));
    }

    @Test
    void searchByCrnIsTrimmedAndCaseInsensitive() throws Exception {
        Complaint complaint = buildComplaint(2L, "IAU-2026-000202", "RESOLVED");
        when(complaintRepository.findByCrnIgnoreCase("iau-2026-000202")).thenReturn(Optional.of(complaint));
        stubCounts();
        when(reporterRepository.findByComplaintId(2L)).thenReturn(buildReporter(complaint, true));

        mockMvc.perform(get("/admin/search").param("query", "  iau-2026-000202  "))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("searchQuery", "iau-2026-000202"))
                .andExpect(content().string(containsString("IAU-2026-000202")))
                .andExpect(content().string(containsString("value=\"iau-2026-000202\"")));

        verify(complaintRepository).findByCrnIgnoreCase("iau-2026-000202");
    }

    @Test
    void complaintDetailShowsReporterSubjectAndEvidence() throws Exception {
        Complaint complaint = buildComplaint(3L, "IAU-2026-000303", "UNDER_INVESTIGATION");
        Reporter reporter = buildReporter(complaint, false);
        Subject subject = buildSubject(complaint);
        Evidence evidence = buildEvidence(complaint);

        when(complaintRepository.findByCrnIgnoreCase("IAU-2026-000303")).thenReturn(Optional.of(complaint));
        when(reporterRepository.findByComplaintId(3L)).thenReturn(reporter);
        when(subjectRepository.findByComplaintId(3L)).thenReturn(List.of(subject));
        when(evidenceRepository.findByComplaintId(3L)).thenReturn(List.of(evidence));

        mockMvc.perform(get("/admin/complaint/{crn}", "IAU-2026-000303"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/complaint-detail"))
                .andExpect(content().string(containsString("IAU-2026-000303")))
                .andExpect(content().string(containsString("Ada Reporter")))
                .andExpect(content().string(containsString("Witness A")))
                .andExpect(content().string(containsString("evidence.pdf")))
                .andExpect(content().string(containsString("UNDER_INVESTIGATION")));
    }

    @Test
    void updateStatusAcceptsNormalizedStatuses() throws Exception {
        Complaint complaint = buildComplaint(4L, "IAU-2026-000404", "PENDING");
        when(complaintRepository.findById(4L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/admin/complaint/{id}/status", 4L).param("status", "resolved"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(complaintRepository).save(any(Complaint.class));
    }

    private void stubCounts() {
        when(complaintRepository.count()).thenReturn(1L);
        when(complaintRepository.countByEscalatedTrue()).thenReturn(0L);
        when(complaintRepository.countByStatus("PENDING")).thenReturn(1L);
        when(complaintRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1L);
        when(complaintRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 0));
    }

    private Complaint buildComplaint(Long id, String crn, String status) {
        Complaint complaint = new Complaint();
        complaint.setId(id);
        complaint.setCrn(crn);
        complaint.setCategory("Fraud");
        complaint.setDescription("Complaint description");
        complaint.setComplaintDate(LocalDate.of(2026, 5, 1));
        complaint.setLocation("Colombo");
        complaint.setReportedBefore(false);
        complaint.setEscalated(false);
        complaint.setStatus(status);
        complaint.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 30));
        return complaint;
    }

    private Reporter buildReporter(Complaint complaint, boolean anonymous) {
        Reporter reporter = new Reporter();
        reporter.setComplaint(complaint);
        reporter.setAnonymousFlag(anonymous);
        reporter.setFullName("Ada Reporter");
        reporter.setEmail("ada@example.com");
        reporter.setPhone("0771234567");
        reporter.setEmployeeId("EMP-001");
        return reporter;
    }

    private Subject buildSubject(Complaint complaint) {
        Subject subject = new Subject();
        subject.setComplaint(complaint);
        subject.setFullName("Witness A");
        subject.setRole("Manager");
        subject.setOrganization("SLT Mobitel");
        subject.setRelationship("Colleague");
        return subject;
    }

    private Evidence buildEvidence(Complaint complaint) {
        Evidence evidence = new Evidence();
        evidence.setComplaint(complaint);
        evidence.setFileName("evidence.pdf");
        evidence.setFilePath("/uploads/evidence.pdf");
        evidence.setFileType("application/pdf");
        return evidence;
    }
}
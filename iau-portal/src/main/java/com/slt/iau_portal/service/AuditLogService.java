package com.slt.iau_portal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slt.iau_portal.model.AuditLog;
import com.slt.iau_portal.repository.AuditLogRepository;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void record(String eventType, String complaintCrn, String actor, String details) {
        AuditLog log = new AuditLog();
        log.setEventType(eventType);
        log.setComplaintCrn(complaintCrn);
        log.setActor(actor);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
    public Page<AuditLog> findLogs(String query, Pageable pageable) {
        return findLogs(query, null, pageable);
    }

    public Page<AuditLog> findLogs(String query, String eventType, Pageable pageable) {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedEvent = eventType == null ? "" : eventType.trim();

        boolean hasEventFilter = !normalizedEvent.isBlank() && !"all".equalsIgnoreCase(normalizedEvent);

        if (!hasEventFilter && normalizedQuery.isBlank()) {
            return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        if (hasEventFilter && normalizedQuery.isBlank()) {
            return auditLogRepository.findByEventTypeIgnoreCaseOrderByCreatedAtDesc(normalizedEvent, pageable);
        }

        // Fallback: perform general search across fields
        String effectiveQuery = normalizedQuery.isBlank() ? normalizedEvent : normalizedQuery;

        return auditLogRepository.findByEventTypeContainingIgnoreCaseOrComplaintCrnContainingIgnoreCaseOrActorContainingIgnoreCaseOrderByCreatedAtDesc(
            effectiveQuery,
            effectiveQuery,
            effectiveQuery,
            pageable
        );
    }

    public java.util.List<String> getDistinctEventTypes() {
        return auditLogRepository.findDistinctEventTypes();
    }
}
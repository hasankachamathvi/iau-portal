package com.slt.iau_portal.service;

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
}
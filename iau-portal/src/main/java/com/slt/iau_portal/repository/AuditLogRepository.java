package com.slt.iau_portal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slt.iau_portal.model.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Page<AuditLog> findByEventTypeContainingIgnoreCaseOrComplaintCrnContainingIgnoreCaseOrActorContainingIgnoreCaseOrderByCreatedAtDesc(
		String eventType,
		String complaintCrn,
		String actor,
		Pageable pageable
	);

	List<AuditLog> findTop200ByOrderByCreatedAtDesc();
}
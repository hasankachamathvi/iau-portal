package com.slt.iau_portal.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slt.iau_portal.model.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByCrn(String crn);

    Optional<Complaint> findByCrnIgnoreCase(String crn);

    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Complaint> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    Page<Complaint> findByStatus(String status, Pageable pageable);
    
    Page<Complaint> findByCategory(String category, Pageable pageable);
    
    Page<Complaint> findByStatusAndCategory(String status, String category, Pageable pageable);

    Page<Complaint> findByEscalatedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Complaint> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countByEscalatedTrue();

    long countByStatus(String status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}

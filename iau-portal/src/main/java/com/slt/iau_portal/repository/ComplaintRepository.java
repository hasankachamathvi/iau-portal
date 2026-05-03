package com.slt.iau_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slt.iau_portal.model.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByCrn(String crn);

    long countByEscalatedTrue();

    long countByStatus(String status);

}

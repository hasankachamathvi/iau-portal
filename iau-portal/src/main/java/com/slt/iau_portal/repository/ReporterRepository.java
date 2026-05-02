package com.slt.iau_portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slt.iau_portal.model.Reporter;

@Repository
public interface ReporterRepository extends JpaRepository<Reporter, Long> {

    Reporter findByComplaintId(Long complaintId);

}

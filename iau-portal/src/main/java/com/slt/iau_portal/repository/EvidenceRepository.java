package com.slt.iau_portal.repository;

import com.slt.iau_portal.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    java.util.List<Evidence> findByComplaintId(Long complaintId);

}

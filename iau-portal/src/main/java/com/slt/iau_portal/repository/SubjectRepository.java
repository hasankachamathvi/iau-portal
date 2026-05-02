package com.slt.iau_portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slt.iau_portal.model.Subject;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    java.util.List<Subject> findByComplaintId(Long complaintId);

}

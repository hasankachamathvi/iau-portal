package com.slt.iau_portal.util;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.slt.iau_portal.repository.ComplaintRepository;

@Component
public class CrnGenerator {

    @Autowired
    private ComplaintRepository complaintRepository;

    public String generate() {
        int year = LocalDate.now().getYear();
        long count = complaintRepository.count() + 1;
        return String.format("IAU-%d-%06d", year, count);
    }
}

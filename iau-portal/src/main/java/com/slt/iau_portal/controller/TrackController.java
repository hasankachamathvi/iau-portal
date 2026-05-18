package com.slt.iau_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TrackController {

    // Minimal skeleton for future tracking implementation
    @GetMapping("/track")
    public String trackPlaceholder(@RequestParam(required = false) String crn, Model model) {
        model.addAttribute("message", "Complaint tracking is temporarily unavailable. Please contact support.");
        if (crn != null && !crn.isBlank()) {
            model.addAttribute("crn", crn.trim());
        }
        return "track";
    }
}

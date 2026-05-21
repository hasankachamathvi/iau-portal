package com.slt.iau_portal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.slt.iau_portal.dto.ComplaintFormDto;
import com.slt.iau_portal.exception.ComplaintProcessingException;
import com.slt.iau_portal.service.ComplaintService;
import com.slt.iau_portal.util.ValidationUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/complaint")
public class ComplaintController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private com.slt.iau_portal.repository.ComplaintRepository complaintRepository;

    @GetMapping
    public String showForm(Model model, HttpSession session) {
        logger.info("Complaint form requested");
        model.addAttribute("form", new ComplaintFormDto());
        return "complaint-form";
    }

    @PostMapping("/submit")
    public String submitComplaint(
            @Valid @ModelAttribute ComplaintFormDto form,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {
        
        logger.info("Complaint submission received");

        if (!form.isAnonymous()) {
            if (form.getFullName() == null || form.getFullName().trim().isEmpty()) {
                bindingResult.rejectValue("fullName", "fullName.required", "Full name is required for named complaints");
            }

            if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
                bindingResult.rejectValue("email", "email.required", "Email address is required for named complaints");
            }
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            logger.warn("Complaint form validation failed with {} errors", bindingResult.getErrorCount());
            model.addAttribute("form", form);
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "complaint-form";
        }

        try {
            // Additional sanitization
            sanitizeFormData(form);
            
            logger.info("Processing complaint - Category: {}, Anonymous: {}", 
                form.getCategory(), form.isAnonymous());
            
            String crn = complaintService.processComplaint(form);
            
            logger.info("Complaint successfully processed with CRN: {}", crn);
            
            model.addAttribute("crn", crn);
            model.addAttribute("category", form.getCategory());
            model.addAttribute("anonymous", form.isAnonymous());
            model.addAttribute("success", true);
            return "confirmation";
            
        } catch (ComplaintProcessingException e) {
            logger.error("Complaint processing exception: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            return "complaint-form";
            
        } catch (Exception e) {
            logger.error("Unexpected error during complaint submission", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again later.");
            model.addAttribute("form", form);
            return "complaint-form";
        }
    }

    private void sanitizeFormData(ComplaintFormDto form) {
        form.setDescription(ValidationUtil.sanitizeInput(form.getDescription()));
        form.setFullName(ValidationUtil.sanitizeInput(form.getFullName()));
        form.setSubjectName(ValidationUtil.sanitizeInput(form.getSubjectName()));
        form.setLocation(ValidationUtil.sanitizeInput(form.getLocation()));
        form.setAdditionalNotes(ValidationUtil.sanitizeInput(form.getAdditionalNotes()));
        form.setWitnessNames(ValidationUtil.sanitizeInput(form.getWitnessNames()));
    }

    @GetMapping("/")
    public String home() { 
        return "redirect:/complaint"; 
    }

    // Public complaint tracking endpoint removed. Use admin interfaces for complaint details.
}

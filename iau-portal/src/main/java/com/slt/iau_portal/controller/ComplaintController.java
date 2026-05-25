package com.slt.iau_portal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/complaint")
public class ComplaintController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private com.slt.iau_portal.service.RecaptchaService recaptchaService;

    @Autowired
    private com.slt.iau_portal.repository.ComplaintRepository complaintRepository;

    @Value("${recaptcha.site:}")
    private String recaptchaSiteKey;

    @GetMapping
    public String showForm(Model model, HttpSession session) {
        logger.info("Complaint form requested");
        model.addAttribute("form", new ComplaintFormDto());
        addRecaptchaModel(model);
        return "complaint-form";
    }

    @PostMapping("/submit")
    public String submitComplaint(
            @Valid @ModelAttribute ComplaintFormDto form,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            HttpServletRequest request) {
        
        logger.info("Complaint submission received");

        // Validate submission type specific fields
        if ("named".equals(form.getSubmissionType())) {
            if (form.getFullName() == null || form.getFullName().trim().isEmpty()) {
                bindingResult.rejectValue("fullName", "fullName.required", "Full name is required for named complaints");
            }

            if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
                bindingResult.rejectValue("email", "email.required", "Email address is required for named complaints");
            }
        }

        // Validate senior management field if applicable
        if ("yes".equals(form.getInvolvesSeniorManagement())) {
            if (form.getSeniorPersonnelNames() == null || form.getSeniorPersonnelNames().trim().isEmpty()) {
                bindingResult.rejectValue("seniorPersonnelNames", "seniorPersonnelNames.required", 
                    "Senior personnel names are required when complaint involves senior management");
            }
        }

        // Validate evidence fields if applicable
        if ("yes".equals(form.getHasEvidence())) {
            if (form.getEvidenceTypes() == null || form.getEvidenceTypes().isEmpty()) {
                bindingResult.rejectValue("evidenceTypes", "evidenceTypes.required", 
                    "Please select at least one evidence type");
            }
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            logger.warn("Complaint form validation failed with {} errors", bindingResult.getErrorCount());
            model.addAttribute("form", form);
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            addRecaptchaModel(model);
            return "complaint-form";
        }

        // Verify reCAPTCHA (server-side)
        String captchaResponse = request.getParameter("g-recaptcha-response");
        boolean captchaOk = recaptchaService.verify(captchaResponse);
        if (!captchaOk) {
            logger.warn("reCAPTCHA verification failed");
            bindingResult.reject("captcha.failed", "reCAPTCHA verification failed. Please confirm you're not a robot.");
            model.addAttribute("form", form);
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("error", "Please complete the reCAPTCHA verification.");
            addRecaptchaModel(model);
            return "complaint-form";
        }

        try {
            // Additional sanitization
            sanitizeFormData(form);
            
            logger.info("Processing complaint - Category: {}, Type: {}", 
                form.getCategory(), form.getSubmissionType());
            
            String crn = complaintService.processComplaint(form);
            
            logger.info("Complaint successfully processed with CRN: {}", crn);
            
            model.addAttribute("crn", crn);
            model.addAttribute("category", form.getCategory());
            model.addAttribute("submissionType", form.getSubmissionType());
            model.addAttribute("success", true);
            return "confirmation";
            
        } catch (ComplaintProcessingException e) {
            logger.error("Complaint processing exception: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            addRecaptchaModel(model);
            return "complaint-form";
            
        } catch (Exception e) {
            logger.error("Unexpected error during complaint submission", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again later.");
            model.addAttribute("form", form);
            addRecaptchaModel(model);
            return "complaint-form";
        }
    }

    private void addRecaptchaModel(Model model) {
        model.addAttribute("recaptcha", java.util.Map.of("site", recaptchaSiteKey == null ? "" : recaptchaSiteKey));
    }

    private void sanitizeFormData(ComplaintFormDto form) {
        form.setDescription(ValidationUtil.sanitizeInput(form.getDescription()));
        form.setFullName(ValidationUtil.sanitizeInput(form.getFullName()));
        form.setSubjectNames(ValidationUtil.sanitizeInput(form.getSubjectNames()));
        form.setLocation(ValidationUtil.sanitizeInput(form.getLocation()));
        form.setAdditionalInfo(ValidationUtil.sanitizeInput(form.getAdditionalInfo()));
        form.setWitnessNames(ValidationUtil.sanitizeInput(form.getWitnessNames()));
        form.setPreviousOutcome(ValidationUtil.sanitizeInput(form.getPreviousOutcome()));
        form.setSeniorPersonnelNames(ValidationUtil.sanitizeInput(form.getSeniorPersonnelNames()));
        form.setSubjectRole(ValidationUtil.sanitizeInput(form.getSubjectRole()));
        form.setDivision(ValidationUtil.sanitizeInput(form.getDivision()));
        form.setDesignation(ValidationUtil.sanitizeInput(form.getDesignation()));
    }

    @GetMapping("/")
    public String home() { 
        return "redirect:/complaint"; 
    }

    // Public complaint tracking endpoint removed. Use admin interfaces for complaint details.
}

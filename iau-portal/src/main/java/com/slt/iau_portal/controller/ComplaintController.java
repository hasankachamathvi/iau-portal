package com.slt.iau_portal.controller;

import com.slt.iau_portal.dto.ComplaintFormDto;
import com.slt.iau_portal.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("form", new ComplaintFormDto());
        return "complaint-form";
    }

    @PostMapping("/submit")
    public String submitComplaint(
            @ModelAttribute ComplaintFormDto form,
            Model model) {
        try {
            String crn = complaintService.processComplaint(form);
            model.addAttribute("crn", crn);
            model.addAttribute("category", form.getCategory());
            model.addAttribute("anonymous", form.isAnonymous());
            return "confirmation";
        } catch (Exception e) {
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("form", form);
            return "complaint-form";
        }
    }

    @GetMapping("/")
    public String home() { return "redirect:/complaint"; }
}

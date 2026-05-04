package com.slt.iau_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/confirmation")
    public String confirmation() { return "confirmation"; }

}

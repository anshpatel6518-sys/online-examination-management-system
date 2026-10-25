package com.examportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for public/home navigation.
 */
@Controller
public class HomeController {

    /**
     * Temporary root endpoint confirming project setup.
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Online Examination System");
        model.addAttribute("message", "Online Examination System: setup complete");
        return "index";
    }
}

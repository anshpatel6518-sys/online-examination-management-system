package com.examportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle security exceptions and explicit error paths.
 */
@Controller
public class CustomErrorController {

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}

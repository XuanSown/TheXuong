package com.example.thexuong.controller;

import com.example.thexuong.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class EmailController {
    @Autowired
    private final EmailService emailService;

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam("email") String email,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            emailService.sendEmail(email);
            redirectAttributes.addFlashAttribute("message", "Email subscribed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error subscribing to email");
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}

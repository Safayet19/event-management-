package com.eventflow.controller;

import com.eventflow.dto.ForgotPasswordRequest;
import com.eventflow.dto.RegisterRequest;
import com.eventflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/login", "/register"})
    public String authPage(Authentication authentication,
                           @RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           @RequestParam(required = false) String redirect,
                           HttpServletRequest request,
                           Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("loginError", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", "You have been signed out successfully.");
        }
        if ("/register".equals(request.getRequestURI())) {
            model.addAttribute("activeTab", "register");
        }
        model.addAttribute("redirect", safeRedirect(redirect));
        return "auth";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request,
                           @RequestParam(required = false) String redirect,
                           RedirectAttributes redirectAttributes) {
        String cleanName = request.getFullName() == null
                ? ""
                : request.getFullName().trim().replaceAll("\\s+", " ");
        String cleanEmail = request.getEmail() == null
                ? ""
                : request.getEmail().trim().toLowerCase(Locale.ROOT);
        String cleanRole = request.getRole() == null
                ? ""
                : request.getRole().trim().toUpperCase(Locale.ROOT);

        redirectAttributes.addFlashAttribute("formName", cleanName);
        redirectAttributes.addFlashAttribute("formEmail", cleanEmail);
        redirectAttributes.addFlashAttribute("formRole", cleanRole);

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute(
                    "registerSuccess",
                    "Account created successfully. Sign in with your new account."
            );
            String safeRedirect = safeRedirect(redirect);
            if (safeRedirect != null) {
                redirectAttributes.addAttribute("redirect", safeRedirect);
            }
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("registerError", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "register");
            String safeRedirect = safeRedirect(redirect);
            if (safeRedirect != null) {
                redirectAttributes.addAttribute("redirect", safeRedirect);
            }
            return "redirect:/register";
        }
    }

    @PostMapping("/forgot-password/check")
    public String checkForgotPasswordEmail(@ModelAttribute ForgotPasswordRequest request,
                                           RedirectAttributes redirectAttributes) {
        String cleanEmail = request.getEmail() == null
                ? ""
                : request.getEmail().trim().toLowerCase(Locale.ROOT);

        redirectAttributes.addFlashAttribute("forgotEmail", cleanEmail);

        try {
            userService.validatePasswordResetEmail(cleanEmail);
            redirectAttributes.addFlashAttribute("forgotStep", "reset");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("forgotStep", "email");
            redirectAttributes.addFlashAttribute("forgotError", ex.getMessage());
        }
        return "redirect:/login";
    }

    @PostMapping("/forgot-password/reset")
    public String resetForgottenPassword(@ModelAttribute ForgotPasswordRequest request,
                                         RedirectAttributes redirectAttributes) {
        String cleanEmail = request.getEmail() == null
                ? ""
                : request.getEmail().trim().toLowerCase(Locale.ROOT);

        try {
            userService.resetForgottenPassword(
                    cleanEmail,
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );
            redirectAttributes.addFlashAttribute(
                    "passwordResetSuccess",
                    "Password updated successfully. Sign in with your new password."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("forgotEmail", cleanEmail);
            redirectAttributes.addFlashAttribute("forgotStep", "reset");
            redirectAttributes.addFlashAttribute("forgotError", ex.getMessage());
        }
        return "redirect:/login";
    }

    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return null;
        }
        String clean = redirect.trim();
        if (!clean.startsWith("/") || clean.startsWith("//")) {
            return null;
        }
        return clean;
    }

}

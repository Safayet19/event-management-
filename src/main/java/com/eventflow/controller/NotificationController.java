package com.eventflow.controller;

import com.eventflow.model.User;
import com.eventflow.service.NotificationService;
import com.eventflow.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NotificationController {

    private final UserService userService;
    private final NotificationService notificationService;

    public NotificationController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notifications(Authentication authentication, Model model) {
        User user = userService.currentUser(authentication);
        model.addAttribute("notifications", notificationService.findAllForUser(user.getId()));
        return "notifications";
    }

    @GetMapping("/notifications/{id}/open")
    public String open(@PathVariable String id, Authentication authentication) {
        User user = userService.currentUser(authentication);
        String link = notificationService.markReadAndGetLink(id, user.getId());
        return link != null && link.startsWith("/") && !link.startsWith("//")
                ? "redirect:" + link
                : "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String readAll(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.currentUser(authentication);
        notificationService.markAllRead(user.getId());
        redirectAttributes.addFlashAttribute("toastSuccess", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}

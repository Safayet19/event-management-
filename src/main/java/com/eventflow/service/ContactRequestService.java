package com.eventflow.service;

import com.eventflow.dto.ContactRequestForm;
import com.eventflow.model.ContactRequest;
import com.eventflow.model.User;
import com.eventflow.repository.ContactRequestRepository;
import com.eventflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ContactRequestService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9 ()-]{7,24}$");
    private static final Set<String> REQUEST_TYPES = Set.of(
            "GENERAL_INQUIRY", "EVENT_SUPPORT", "REGISTRATION_ISSUE", "COUPON_PRICING_ISSUE",
            "ORGANIZER_SUPPORT", "TECHNICAL_PROBLEM", "OTHER"
    );
    private static final Set<String> STATUSES = Set.of("NEW", "IN_PROGRESS", "RESOLVED");

    private final ContactRequestRepository contactRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ContactRequestService(ContactRequestRepository contactRequestRepository,
                                 UserRepository userRepository,
                                 NotificationService notificationService) {
        this.contactRequestRepository = contactRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public ContactRequest submit(ContactRequestForm form) {
        if (form == null) throw new IllegalArgumentException("Enter your contact request details.");

        String name = clean(form.getFullName());
        String email = clean(form.getEmail()).toLowerCase(Locale.ROOT);
        String phone = clean(form.getPhone());
        String requestType = clean(form.getRequestType()).toUpperCase(Locale.ROOT);
        String subject = clean(form.getSubject());
        String message = clean(form.getMessage());

        if (name.length() < 3 || name.length() > 80) {
            throw new IllegalArgumentException("Full name must be between 3 and 80 characters.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (!phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Enter a valid phone or WhatsApp number.");
        }
        if (!REQUEST_TYPES.contains(requestType)) {
            throw new IllegalArgumentException("Choose a valid request type.");
        }
        if (subject.length() < 3 || subject.length() > 120) {
            throw new IllegalArgumentException("Subject must be between 3 and 120 characters.");
        }
        if (message.length() < 10 || message.length() > 1500) {
            throw new IllegalArgumentException("Message must be between 10 and 1500 characters.");
        }

        ContactRequest request = new ContactRequest();
        request.setFullName(name);
        request.setEmail(email);
        request.setPhone(phone);
        request.setRequestType(requestType);
        request.setSubject(subject);
        request.setMessage(message);
        request.setStatus("NEW");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        request = contactRequestRepository.save(request);

        for (User admin : userRepository.findByRole("ADMIN")) {
            notificationService.notifyUser(
                    admin.getId(),
                    "New contact request from " + request.getFullName() + ": " + request.getSubject(),
                    "/admin/contact-requests#request-" + request.getId()
            );
        }
        return request;
    }

    public List<ContactRequest> findAll() {
        return contactRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public long countNew() {
        return contactRequestRepository.countByStatus("NEW");
    }

    public long countByStatus(String status) {
        return contactRequestRepository.countByStatus(status);
    }

    public ContactRequest update(String id, String status, String adminNote) {
        ContactRequest request = findById(id);
        String cleanStatus = clean(status).toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(cleanStatus)) {
            throw new IllegalArgumentException("Choose a valid request status.");
        }
        String note = clean(adminNote);
        if (note.length() > 1000) {
            throw new IllegalArgumentException("Admin note must be 1000 characters or fewer.");
        }
        request.setStatus(cleanStatus);
        request.setAdminNote(note);
        request.setUpdatedAt(LocalDateTime.now());
        return contactRequestRepository.save(request);
    }

    public void delete(String id) {
        contactRequestRepository.delete(findById(id));
    }

    public ContactRequest findById(String id) {
        return contactRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact request not found."));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

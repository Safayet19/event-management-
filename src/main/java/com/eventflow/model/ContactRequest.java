package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "contact_requests")
public class ContactRequest {

    @Id
    private String id;

    private String fullName;
    private String email;
    private String phone;
    private String requestType;
    private String subject;
    private String message;

    @Indexed
    private String status = "NEW";

    private String adminNote;

    @Indexed
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getRequestTypeLabel() {
        return Map.of(
                "GENERAL_INQUIRY", "General Inquiry",
                "EVENT_SUPPORT", "Event Support",
                "REGISTRATION_ISSUE", "Registration Issue",
                "COUPON_PRICING_ISSUE", "Coupon / Pricing Issue",
                "ORGANIZER_SUPPORT", "Organizer Support",
                "TECHNICAL_PROBLEM", "Technical Problem",
                "OTHER", "Other"
        ).getOrDefault(requestType == null ? "" : requestType, "Other");
    }

    public String getStatusLabel() {
        return switch (status == null ? "NEW" : status) {
            case "IN_PROGRESS" -> "In Progress";
            case "RESOLVED" -> "Resolved";
            default -> "New";
        };
    }

    public String getFormattedCreatedAt() {
        return createdAt == null ? "—" : createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String getFormattedUpdatedAt() {
        return updatedAt == null ? "—" : updatedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }
}

package com.eventflow.service;

import com.eventflow.model.Event;
import com.eventflow.model.Notification;
import com.eventflow.model.Registration;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.NotificationRepository;
import com.eventflow.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               EventRepository eventRepository,
                               RegistrationRepository registrationRepository) {
        this.notificationRepository = notificationRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    public void notifyUser(String userId, String message, String link) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        notificationRepository.save(new Notification(userId, message, link));
    }

    public List<Notification> findAllForUser(String userId) {
        return notificationRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> findRecentForUser(String userId) {
        return notificationRepository.findTop8ByUserIdOrderByCreatedAtDesc(userId);
    }

    public long countUnread(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public String markReadAndGetLink(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null || !userId.equals(notification.getUserId())) {
            return null;
        }

        String resolvedLink = resolveSpecificLink(notification, userId);
        boolean changed = !notification.isRead() || !Objects.equals(notification.getLink(), resolvedLink);
        notification.setRead(true);
        if (!Objects.equals(notification.getLink(), resolvedLink)) {
            notification.setLink(resolvedLink);
        }
        if (changed) {
            notificationRepository.save(notification);
        }
        return resolvedLink;
    }

    private String resolveSpecificLink(Notification notification, String userId) {
        String link = notification.getLink();
        if (link == null || link.isBlank()) {
            return "/notifications";
        }

        String message = notification.getMessage() == null ? "" : notification.getMessage();
        Event mentionedEvent = findMentionedEvent(message);

        if ("/participant/registrations".equals(link)) {
            if (mentionedEvent != null) {
                String lowerMessage = message.toLowerCase(Locale.ROOT);
                if (lowerMessage.contains("rate") || lowerMessage.contains("completed")) {
                    return "/events/" + mentionedEvent.getId() + "#participant-feedback";
                }
                if (lowerMessage.contains("updated")) {
                    return "/events/" + mentionedEvent.getId();
                }
                if (registrationRepository.existsByEventIdAndParticipantId(mentionedEvent.getId(), userId)) {
                    return "/participant/registrations#registration-" + mentionedEvent.getId();
                }
                return "/events/" + mentionedEvent.getId();
            }

            Registration couponRegistration = findRegistrationForCouponMessage(userId, message);
            if (couponRegistration != null) {
                return "/participant/registrations#registration-" + couponRegistration.getEventId();
            }
        }

        if (link.startsWith("/organizer/feedback") && mentionedEvent != null && !link.contains("eventId=")) {
            return "/organizer/feedback?eventId=" + mentionedEvent.getId()
                    + "#review-event-" + mentionedEvent.getId();
        }

        if ("/admin/events".equals(link) && mentionedEvent != null) {
            return "/events/" + mentionedEvent.getId();
        }

        return link;
    }

    private Event findMentionedEvent(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        return eventRepository.findAllByOrderByDateAsc().stream()
                .filter(event -> event.getTitle() != null && !event.getTitle().isBlank())
                .sorted(Comparator.comparingInt((Event event) -> event.getTitle().length()).reversed())
                .filter(event -> lowerMessage.contains(event.getTitle().toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
    }

    private Registration findRegistrationForCouponMessage(String userId, String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        if (!lowerMessage.contains("coupon")) {
            return null;
        }
        return registrationRepository.findByParticipantIdOrderByRegisteredAtDesc(userId).stream()
                .filter(registration -> registration.getCouponCode() != null && !registration.getCouponCode().isBlank())
                .filter(registration -> lowerMessage.contains(registration.getCouponCode().toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
    }

    public void markAllRead(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        if (unread.isEmpty()) {
            return;
        }
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public void deleteAllForUser(String userId) {
        notificationRepository.deleteAllByUserId(userId);
    }
}

package com.eventflow.service;

import com.eventflow.model.Event;
import com.eventflow.model.Registration;
import com.eventflow.model.User;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.FeedbackRepository;
import com.eventflow.repository.RegistrationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationService notificationService;
    private final TicketTypeService ticketTypeService;
    private final CouponService couponService;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               FeedbackRepository feedbackRepository,
                               NotificationService notificationService,
                               TicketTypeService ticketTypeService,
                               CouponService couponService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.feedbackRepository = feedbackRepository;
        this.notificationService = notificationService;
        this.ticketTypeService = ticketTypeService;
        this.couponService = couponService;
    }

    @CacheEvict(value = "registrationCount", allEntries = true, beforeInvocation = true)
    public Registration register(String eventId, User participant) {
        return register(eventId, participant, null);
    }

    @CacheEvict(value = "registrationCount", allEntries = true, beforeInvocation = true)
    public Registration register(String eventId, User participant, String ticketTypeId) {
        return register(eventId, participant, ticketTypeId, null);
    }

    @CacheEvict(value = "registrationCount", allEntries = true, beforeInvocation = true)
    public Registration register(String eventId, User participant, String ticketTypeId, String couponCode) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        if (isClosed(event)) {
            throw new IllegalArgumentException("Registration is closed for this event.");
        }
        if (registrationRepository.existsByEventIdAndParticipantId(eventId, participant.getId())) {
            throw new IllegalArgumentException("You are already registered for this event.");
        }
        if (registrationRepository.countByEventId(eventId) >= event.getCapacity()) {
            throw new IllegalArgumentException("This event is already full.");
        }

        TicketTypeService.TicketSelection ticket = ticketTypeService.resolveForRegistration(event, ticketTypeId);
        CouponService.AppliedCoupon coupon = couponService.apply(event, ticket.price(), couponCode);

        try {
            Registration registration = registrationRepository.save(new Registration(
                    eventId,
                    participant.getId(),
                    participant.getFullName(),
                    participant.getEmail(),
                    ticket.ticketTypeId(),
                    ticket.ticketTypeName(),
                    ticket.price(),
                    coupon.couponId(),
                    coupon.couponCode(),
                    coupon.discountAmount(),
                    coupon.finalPrice()
            ));

            notificationService.notifyUser(
                    event.getOrganizerId(),
                    participant.getFullName() + " registered for " + event.getTitle()
                            + " (" + ticket.ticketTypeName() + ").",
                    "/organizer/events/" + eventId + "/participants"
            );
            notificationService.notifyUser(
                    participant.getId(),
                    "Registration confirmed for " + event.getTitle() + ".",
                    "/participant/registrations#registration-" + eventId
            );
            if (coupon.couponCode() != null && !coupon.couponCode().isBlank()) {
                notificationService.notifyUser(
                        participant.getId(),
                        "Your " + coupon.couponCode() + " coupon discount was applied successfully.",
                        "/participant/registrations#registration-" + eventId
                );
            }
            return registration;
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("You are already registered for this event.");
        }
    }

    @CacheEvict(value = "registrationCount", allEntries = true, beforeInvocation = true)
    public Event cancel(String eventId, User participant) {
        Registration registration = registrationRepository.findByEventIdAndParticipantId(eventId, participant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Registration was not found."));

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event != null && isClosed(event)) {
            throw new IllegalArgumentException("Completed event registrations are read-only and cannot be cancelled.");
        }
        registrationRepository.delete(registration);
        feedbackRepository.deleteByEventIdAndParticipantId(eventId, participant.getId());

        if (event != null) {
            notificationService.notifyUser(
                    event.getOrganizerId(),
                    participant.getFullName() + " cancelled registration for " + event.getTitle() + ".",
                    "/organizer/events/" + eventId + "/participants"
            );
        }
        return event;
    }

    @CacheEvict(value = "registrationCount", allEntries = true, beforeInvocation = true)
    public void removeByAdmin(String registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found."));
        Event event = eventRepository.findById(registration.getEventId()).orElse(null);
        if (event != null && isClosed(event)) {
            throw new IllegalArgumentException("Bookings for completed events are read-only and cannot be removed.");
        }
        registrationRepository.delete(registration);
        feedbackRepository.deleteByEventIdAndParticipantId(registration.getEventId(), registration.getParticipantId());

        notificationService.notifyUser(
                registration.getParticipantId(),
                "An administrator removed your registration"
                        + (event == null ? "." : " for " + event.getTitle() + "."),
                event == null ? "/participant/registrations" : "/events/" + event.getId()
        );
    }

    public List<Registration> findByParticipant(String participantId) {
        return registrationRepository.findByParticipantIdOrderByRegisteredAtDesc(participantId);
    }

    public List<Registration> findByEvent(String eventId) {
        return registrationRepository.findByEventIdOrderByRegisteredAtAsc(eventId);
    }

    public List<Registration> findAllNewestFirst() {
        return registrationRepository.findAllByOrderByRegisteredAtDesc();
    }

    public List<Registration> findForEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        Set<String> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());
        return registrationRepository.findByEventIdInOrderByRegisteredAtDesc(eventIds);
    }

    @CacheEvict(value = "registrationCount", allEntries = true, beforeInvocation = true)
    public void removeByOrganizer(String registrationId, User organizer) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found."));
        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        if (event.getOrganizerId() == null || !event.getOrganizerId().equals(organizer.getId())) {
            throw new IllegalArgumentException("You cannot manage this registration.");
        }
        if (isClosed(event)) {
            throw new IllegalArgumentException("Bookings for completed events are read-only and cannot be removed.");
        }

        registrationRepository.delete(registration);
        feedbackRepository.deleteByEventIdAndParticipantId(registration.getEventId(), registration.getParticipantId());
        notificationService.notifyUser(
                registration.getParticipantId(),
                "The organizer removed your registration for " + event.getTitle() + ".",
                "/events/" + event.getId()
        );
    }

    @Cacheable(value = "registrationCount", key = "#eventId")
    public long countByEvent(String eventId) {
        return registrationRepository.countByEventId(eventId);
    }

    public long count() {
        return registrationRepository.count();
    }

    public boolean isRegistered(String eventId, String participantId) {
        return registrationRepository.existsByEventIdAndParticipantId(eventId, participantId);
    }

    @CacheEvict(value = "registrationCount", allEntries = true, beforeInvocation = true)
    public void deleteAllByEvent(String eventId) {
        registrationRepository.deleteAllByEventId(eventId);
        feedbackRepository.deleteAllByEventId(eventId);
    }

    private boolean isClosed(Event event) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        return event.getDate() == null
                || "COMPLETED".equals(event.getStatus())
                || event.getDate().isBefore(today)
                || (event.getDate().isEqual(today)
                && event.getTime() != null
                && event.getTime().isBefore(now));
    }
}

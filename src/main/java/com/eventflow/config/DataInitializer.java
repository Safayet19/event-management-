package com.eventflow.config;

import com.eventflow.model.Event;
import com.eventflow.model.EventImage;
import com.eventflow.model.Feedback;
import com.eventflow.model.Registration;
import com.eventflow.model.User;
import com.eventflow.repository.EventImageRepository;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.FeedbackRepository;
import com.eventflow.repository.RegistrationRepository;
import com.eventflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadDemoData(EventRepository eventRepository,
                                   EventImageRepository eventImageRepository,
                                   RegistrationRepository registrationRepository,
                                   FeedbackRepository feedbackRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            if (eventRepository.count() == 0) {
                LocalDate today = LocalDate.now();
                eventRepository.saveAll(List.of(
                        new Event("Tech Innovation Summit", "Technology", "Dhaka Convention Center",
                                today.plusDays(5), LocalTime.of(10, 0),
                                "A full-day gathering for technology, startup and innovation enthusiasts.",
                                450, "UPCOMING", true, "EventFlow Team"),
                        new Event("Creative Design Meetup", "Design", "Banani Creative Hub",
                                today.plusDays(8), LocalTime.of(17, 30),
                                "Meet designers, creators and students for talks, networking and portfolio exchange.",
                                180, "UPCOMING", true, "Pixel Society"),
                        new Event("Campus Music Night", "Music", "University Auditorium",
                                today.plusDays(12), LocalTime.of(18, 30),
                                "An energetic evening of live music, student performances and community celebration.",
                                700, "UPCOMING", true, "Campus Culture Club"),
                        new Event("Startup Founders Circle", "Business", "Gulshan Business Lounge",
                                today.plusDays(15), LocalTime.of(15, 0),
                                "A focused networking session for founders, aspiring entrepreneurs and mentors.",
                                120, "UPCOMING", false, "LaunchPad"),
                        new Event("Photography Walk", "Community", "Hatirjheel",
                                today.plusDays(18), LocalTime.of(7, 0),
                                "A relaxed city photo walk for beginners and experienced photographers.",
                                90, "UPCOMING", false, "Dhaka Frames"),
                        new Event("Career & Skills Expo", "Career", "International Convention City",
                                today.plusDays(24), LocalTime.of(9, 30),
                                "Explore career opportunities, workshops and practical skill sessions in one place.",
                                1000, "UPCOMING", false, "FutureWorks")
                ));
            }

            migrateLegacyInlineImages(eventRepository, eventImageRepository);

            ensureDemoUser(userRepository, passwordEncoder,
                    "Safayet Ullah", "admin@eventflow.com", "Admin123!", "ADMIN");
            User demoOrganizer = ensureDemoUser(userRepository, passwordEncoder,
                    "Safayet Ullah", "organizer@eventflow.com", "Organizer123!", "ORGANIZER");
            User demoParticipant = ensureDemoUser(userRepository, passwordEncoder,
                    "Safayet Ullah", "participant@eventflow.com", "Participant123!", "PARTICIPANT");

            syncDemoDisplayNames(eventRepository, registrationRepository, feedbackRepository,
                    demoOrganizer, demoParticipant);
        };
    }

    private User ensureDemoUser(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                String fullName,
                                String email,
                                String plainPassword,
                                String role) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        user.setFullName(fullName);
        user.setEmail(email);
        if (user.getPassword() == null || !passwordEncoder.matches(plainPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(plainPassword));
        }
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void syncDemoDisplayNames(EventRepository eventRepository,
                                      RegistrationRepository registrationRepository,
                                      FeedbackRepository feedbackRepository,
                                      User demoOrganizer,
                                      User demoParticipant) {
        List<Event> organizerEvents = eventRepository.findByOrganizerIdOrderByDateAsc(demoOrganizer.getId());
        organizerEvents.forEach(event -> event.setOrganizerName(demoOrganizer.getFullName()));
        if (!organizerEvents.isEmpty()) {
            eventRepository.saveAll(organizerEvents);
        }

        List<Registration> participantRegistrations =
                registrationRepository.findByParticipantIdOrderByRegisteredAtDesc(demoParticipant.getId());
        participantRegistrations.forEach(registration ->
                registration.setParticipantName(demoParticipant.getFullName()));
        if (!participantRegistrations.isEmpty()) {
            registrationRepository.saveAll(participantRegistrations);
        }

        List<Feedback> participantFeedback =
                feedbackRepository.findByParticipantIdOrderByUpdatedAtDesc(demoParticipant.getId());
        participantFeedback.forEach(item -> item.setParticipantName(demoParticipant.getFullName()));
        if (!participantFeedback.isEmpty()) {
            feedbackRepository.saveAll(participantFeedback);
        }
    }

    private void migrateLegacyInlineImages(EventRepository eventRepository,
                                           EventImageRepository eventImageRepository) {
        for (Event event : eventRepository.findAll()) {
            String imageUrl = event.getImageUrl();
            if (imageUrl == null || !imageUrl.startsWith("data:")) {
                continue;
            }

            int comma = imageUrl.indexOf(',');
            int semicolon = imageUrl.indexOf(';');
            if (comma < 0 || semicolon < 5 || semicolon > comma) {
                continue;
            }

            try {
                String contentType = imageUrl.substring(5, semicolon);
                byte[] data = Base64.getDecoder().decode(imageUrl.substring(comma + 1));
                eventImageRepository.deleteByEventId(event.getId());
                EventImage storedImage = eventImageRepository.save(new EventImage(event.getId(), contentType, data));
                event.setImageUrl("/event-images/" + event.getId() + "?v=" + storedImage.getId());
                eventRepository.save(event);
            } catch (IllegalArgumentException ignored) {
                // Keep the old image value if an old data URL is malformed.
            }
        }
    }
}

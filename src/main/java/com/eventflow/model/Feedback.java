package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "feedback")
@CompoundIndexes({
        @CompoundIndex(name = "event_participant_feedback_unique", def = "{'eventId': 1, 'participantId': 1}", unique = true)
})
public class Feedback {

    @Id
    private String id;

    @Indexed
    private String eventId;

    @Indexed
    private String participantId;

    private String participantName;
    private String participantEmail;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Feedback(String eventId,
                    String participantId,
                    String participantName,
                    String participantEmail,
                    int rating,
                    String comment) {
        this.eventId = eventId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getFormattedUpdatedAt() {
        return updatedAt == null
                ? "—"
                : updatedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }
}

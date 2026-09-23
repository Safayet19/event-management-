package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "notifications")
@CompoundIndexes({
        @CompoundIndex(name = "notification_user_created", def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "notification_user_read", def = "{'userId': 1, 'read': 1}")
})
public class Notification {

    @Id
    private String id;
    private String userId;
    private String message;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;

    public String getFormattedCreatedAt() {
        return createdAt == null ? "—" : createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public Notification(String userId, String message, String link) {
        this.userId = userId;
        this.message = message;
        this.link = link;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }
}

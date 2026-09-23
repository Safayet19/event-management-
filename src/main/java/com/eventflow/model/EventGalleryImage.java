package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "event_gallery_images")
public class EventGalleryImage {

    @Id
    private String id;

    @Indexed
    private String eventId;

    private String contentType;
    private byte[] data;
    private String caption;
    private int sortOrder;
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public EventGalleryImage(String eventId, String contentType, byte[] data, String caption, int sortOrder) {
        this.eventId = eventId;
        this.contentType = contentType;
        this.data = data;
        this.caption = caption;
        this.sortOrder = sortOrder;
        this.uploadedAt = LocalDateTime.now();
    }

    public String getImageUrl() {
        return id == null ? "" : "/event-gallery-images/" + id;
    }
}

package com.eventflow.dto;

import java.time.LocalDateTime;

public interface EventGalleryItemView {
    String getId();
    String getEventId();
    String getCaption();
    int getSortOrder();
    LocalDateTime getUploadedAt();

    default String getImageUrl() {
        return getId() == null ? "" : "/event-gallery-images/" + getId();
    }
}

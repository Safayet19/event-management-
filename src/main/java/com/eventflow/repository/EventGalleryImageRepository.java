package com.eventflow.repository;

import com.eventflow.dto.EventGalleryItemView;
import com.eventflow.model.EventGalleryImage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventGalleryImageRepository extends MongoRepository<EventGalleryImage, String> {
    List<EventGalleryItemView> findByEventIdOrderBySortOrderAscUploadedAtAsc(String eventId);
    long countByEventId(String eventId);
    void deleteAllByEventId(String eventId);
}

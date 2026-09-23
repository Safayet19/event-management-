package com.eventflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class FeedbackSummary {
    private double averageRating;
    private long totalReviews;
    private long fiveStarReviews;
    private Map<Integer, Long> ratingCounts;

    public long countFor(int rating) {
        return ratingCounts.getOrDefault(rating, 0L);
    }
}

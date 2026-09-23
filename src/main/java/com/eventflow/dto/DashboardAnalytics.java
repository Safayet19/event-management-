package com.eventflow.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DashboardAnalytics {

    private final long totalBookings;
    private final long completedEvents;
    private final long totalReviews;
    private final double averageRating;
    private final long couponUses;
    private final BigDecimal ticketValue;
    private final int capacityUsagePercent;
    private final List<String> trendLabels;
    private final List<Long> trendValues;
    private final List<String> categoryLabels;
    private final List<Long> categoryValues;
    private final List<String> topEventLabels;
    private final List<Long> topEventValues;
    private final List<Long> ratingValues;

    public DashboardAnalytics(long totalBookings, long completedEvents, long totalReviews, double averageRating,
                              long couponUses, BigDecimal ticketValue, int capacityUsagePercent,
                              List<String> trendLabels, List<Long> trendValues,
                              List<String> categoryLabels, List<Long> categoryValues,
                              List<String> topEventLabels, List<Long> topEventValues,
                              List<Long> ratingValues) {
        this.totalBookings = totalBookings;
        this.completedEvents = completedEvents;
        this.totalReviews = totalReviews;
        this.averageRating = averageRating;
        this.couponUses = couponUses;
        this.ticketValue = ticketValue == null ? BigDecimal.ZERO : ticketValue;
        this.capacityUsagePercent = capacityUsagePercent;
        this.trendLabels = trendLabels;
        this.trendValues = trendValues;
        this.categoryLabels = categoryLabels;
        this.categoryValues = categoryValues;
        this.topEventLabels = topEventLabels;
        this.topEventValues = topEventValues;
        this.ratingValues = ratingValues;
    }

    public long getTotalBookings() { return totalBookings; }
    public long getCompletedEvents() { return completedEvents; }
    public long getTotalReviews() { return totalReviews; }
    public double getAverageRating() { return averageRating; }
    public long getCouponUses() { return couponUses; }
    public BigDecimal getTicketValue() { return ticketValue; }
    public int getCapacityUsagePercent() { return capacityUsagePercent; }
    public List<String> getTrendLabels() { return trendLabels; }
    public List<Long> getTrendValues() { return trendValues; }
    public List<String> getCategoryLabels() { return categoryLabels; }
    public List<Long> getCategoryValues() { return categoryValues; }
    public List<String> getTopEventLabels() { return topEventLabels; }
    public List<Long> getTopEventValues() { return topEventValues; }
    public List<Long> getRatingValues() { return ratingValues; }

    public String getFormattedTicketValue() {
        if (ticketValue.compareTo(BigDecimal.ZERO) <= 0) return "৳0";
        return "৳" + ticketValue.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}

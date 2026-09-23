package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "events")
public class Event {

    @Id
    private String id;

    private String title;

    @Indexed
    private String category;

    private String location;

    @Indexed
    private LocalDate date;

    private LocalTime time;
    private String description;
    private int capacity;
    private String status;
    private boolean featured;

    @Indexed
    private String organizerId;

    private String organizerName;
    private String imageUrl = "/images/event-visual.png";
    private String pricingType = "FREE";
    private BigDecimal basePrice = BigDecimal.ZERO;

    public Event(String title, String category, String location, LocalDate date, LocalTime time,
                 String description, int capacity, String status, boolean featured, String organizerName) {
        this.title = title;
        this.category = category;
        this.location = location;
        this.date = date;
        this.time = time;
        this.description = description;
        this.capacity = capacity;
        this.status = status;
        this.featured = featured;
        this.organizerName = organizerName;
        this.imageUrl = "/images/event-visual.png";
        this.pricingType = "FREE";
        this.basePrice = BigDecimal.ZERO;
    }

    public String getStatus() {
        if (date != null) {
            LocalDate today = LocalDate.now();
            if (date.isBefore(today)
                    || (date.isEqual(today) && time != null
                    && time.isBefore(LocalTime.now().withSecond(0).withNano(0)))) {
                return "COMPLETED";
            }
        }
        return status == null || status.isBlank() ? "UPCOMING" : status;
    }


    public String getPricingType() {
        return pricingType == null || pricingType.isBlank() ? "FREE" : pricingType;
    }

    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(getPricingType());
    }

    public BigDecimal getEffectiveBasePrice() {
        return basePrice == null ? BigDecimal.ZERO : basePrice;
    }

    public String getFormattedBasePrice() {
        BigDecimal value = getEffectiveBasePrice();
        if (!isPaid() || value.compareTo(BigDecimal.ZERO) <= 0) {
            return "Free";
        }
        return "৳" + value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public String getFormattedDate() {
        return date == null ? "TBA" : date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public String getFormattedTime() {
        return time == null ? "TBA" : time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}

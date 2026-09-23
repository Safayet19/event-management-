package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "event_coupons")
@CompoundIndexes({
        @CompoundIndex(name = "event_coupon_code_unique", def = "{'eventId': 1, 'code': 1}", unique = true)
})
public class Coupon {

    @Id
    private String id;

    @Indexed
    private String eventId;

    @Indexed
    private String organizerId;

    private String code;
    private String discountType;
    private BigDecimal discountValue = BigDecimal.ZERO;
    private LocalDate expiryDate;
    private int usageLimit;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public String getFormattedDiscount() {
        BigDecimal value = discountValue == null ? BigDecimal.ZERO : discountValue;
        String number = value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        return "PERCENT".equalsIgnoreCase(discountType) ? number + "% OFF" : "৳" + number + " OFF";
    }

    public String getFormattedExpiryDate() {
        return expiryDate == null ? "No expiry" : expiryDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public String getDisplayStatus() {
        if (!active) return "PAUSED";
        if (isExpired()) return "EXPIRED";
        return "ACTIVE";
    }
}

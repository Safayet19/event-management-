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
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "registrations")
@CompoundIndexes({
        @CompoundIndex(name = "event_participant_unique", def = "{'eventId': 1, 'participantId': 1}", unique = true)
})
public class Registration {

    @Id
    private String id;

    @Indexed
    private String eventId;

    @Indexed
    private String participantId;

    private String participantName;
    private String participantEmail;
    private LocalDateTime registeredAt;
    private String ticketTypeId;
    private String ticketTypeName;
    private BigDecimal ticketPrice = BigDecimal.ZERO;
    private String couponId;
    private String couponCode;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal finalPrice;

    public String getFormattedTicketPrice() {
        BigDecimal value = finalPrice == null ? (ticketPrice == null ? BigDecimal.ZERO : ticketPrice) : finalPrice;
        if (value.compareTo(BigDecimal.ZERO) <= 0) return "Free";
        return "৳" + value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public String getFormattedOriginalTicketPrice() {
        BigDecimal value = ticketPrice == null ? BigDecimal.ZERO : ticketPrice;
        if (value.compareTo(BigDecimal.ZERO) <= 0) return "Free";
        return "৳" + value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public String getFormattedDiscountAmount() {
        BigDecimal value = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        if (value.compareTo(BigDecimal.ZERO) <= 0) return "৳0";
        return "৳" + value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public boolean isDiscounted() {
        return couponCode != null && !couponCode.isBlank()
                && discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getDisplayTicketType() {
        return ticketTypeName == null || ticketTypeName.isBlank() ? "General Admission" : ticketTypeName;
    }

    public String getFormattedRegisteredAt() {
        return registeredAt == null ? "—" : registeredAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public Registration(String eventId, String participantId, String participantName, String participantEmail) {
        this(eventId, participantId, participantName, participantEmail, null, "General Admission", BigDecimal.ZERO);
    }

    public Registration(String eventId, String participantId, String participantName, String participantEmail,
                        String ticketTypeId, String ticketTypeName, BigDecimal ticketPrice) {
        this(eventId, participantId, participantName, participantEmail, ticketTypeId, ticketTypeName, ticketPrice,
                null, null, BigDecimal.ZERO, ticketPrice);
    }

    public Registration(String eventId, String participantId, String participantName, String participantEmail,
                        String ticketTypeId, String ticketTypeName, BigDecimal ticketPrice,
                        String couponId, String couponCode, BigDecimal discountAmount, BigDecimal finalPrice) {
        this.eventId = eventId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.ticketTypeId = ticketTypeId;
        this.ticketTypeName = ticketTypeName;
        this.ticketPrice = ticketPrice == null ? BigDecimal.ZERO : ticketPrice;
        this.couponId = couponId;
        this.couponCode = couponCode;
        this.discountAmount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        this.finalPrice = finalPrice == null ? this.ticketPrice : finalPrice;
        this.registeredAt = LocalDateTime.now();
    }
}

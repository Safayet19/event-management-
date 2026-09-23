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

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "event_ticket_types")
@CompoundIndexes({
        @CompoundIndex(name = "event_ticket_name_unique", def = "{'eventId': 1, 'name': 1}", unique = true)
})
public class EventTicketType {

    @Id
    private String id;

    @Indexed
    private String eventId;

    private String name;
    private String description;
    private BigDecimal price = BigDecimal.ZERO;
    private int capacity;
    private boolean active = true;
    private int sortOrder;

    public String getFormattedPrice() {
        BigDecimal value = price == null ? BigDecimal.ZERO : price;
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return "Free";
        }
        return "৳" + value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}

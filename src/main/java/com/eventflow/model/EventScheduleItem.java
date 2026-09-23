package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "event_schedule")
public class EventScheduleItem {

    @Id
    private String id;

    @Indexed
    private String eventId;

    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String description;

    @Indexed
    private String speakerId;

    public String getFormattedStartTime() {
        return startTime == null ? "TBA" : startTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getFormattedEndTime() {
        return endTime == null ? "TBA" : endTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getFormattedTimeRange() {
        if (startTime == null) {
            return "TBA";
        }
        if (endTime == null) {
            return getFormattedStartTime();
        }
        return getFormattedStartTime() + " - " + getFormattedEndTime();
    }
}

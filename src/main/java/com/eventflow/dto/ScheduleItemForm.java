package com.eventflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleItemForm {
    private String title;
    private String startTime;
    private String endTime;
    private String location;
    private String description;
    private String speakerId;
}

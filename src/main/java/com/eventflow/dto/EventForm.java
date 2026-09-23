package com.eventflow.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class EventForm {
    private String title;
    private String category;
    private String location;
    private String date;
    private String time;
    private String description;
    private int capacity;
    private String pricingType;
    private String basePrice;
    private MultipartFile image;
}

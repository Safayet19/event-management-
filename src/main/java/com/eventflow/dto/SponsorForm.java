package com.eventflow.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SponsorForm {
    private String name;
    private String level;
    private String website;
    private String description;
    private int sortOrder;
    private MultipartFile logo;
}

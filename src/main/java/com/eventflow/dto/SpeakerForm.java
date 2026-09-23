package com.eventflow.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SpeakerForm {
    private String name;
    private String designation;
    private String organization;
    private String bio;
    private MultipartFile photo;
}

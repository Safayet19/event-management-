package com.eventflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequestForm {
    private String fullName;
    private String email;
    private String phone;
    private String requestType;
    private String subject;
    private String message;
}

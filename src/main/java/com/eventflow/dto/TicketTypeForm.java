package com.eventflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketTypeForm {
    private String name;
    private String description;
    private String price;
    private int capacity;
    private boolean active;
    private int sortOrder;
}

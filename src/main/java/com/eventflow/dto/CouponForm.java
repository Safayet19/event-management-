package com.eventflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponForm {
    private String eventId;
    private String code;
    private String discountType;
    private String discountValue;
    private String expiryDate;
    private int usageLimit;
    private boolean active;
}

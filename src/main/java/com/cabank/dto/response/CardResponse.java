package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CardResponse {
    private String id;
    private String last4;
    private String holderName;
    private String brand;
    private String cardType;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private String validFrom;
    private String goodThru;
    private String color;
    private boolean active;
}
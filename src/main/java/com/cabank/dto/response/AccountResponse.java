package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountResponse {
    private String id;
    private String accountNumber;
    private BigDecimal balance;
    private String branch;
    private String type;
    private boolean active;
}
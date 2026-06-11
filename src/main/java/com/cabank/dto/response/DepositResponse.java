package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DepositResponse {
    private String id;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String note;
    private String status;
    private LocalDateTime createdAt;
}
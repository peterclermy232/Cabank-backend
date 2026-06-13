package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WithdrawResponse {
    private String id;
    private BigDecimal amount;
    private String cardLast4;
    private String phone;
    private String status;
    private BigDecimal newBalance; // null on history listing
    private LocalDateTime createdAt;
}
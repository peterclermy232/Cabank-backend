package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillPaymentResponse {
    private String id;
    private String billType;
    private String billCode;
    private String customerName;
    private BigDecimal amount;
    private BigDecimal tax;
    private String status;
    private LocalDateTime createdAt;
}
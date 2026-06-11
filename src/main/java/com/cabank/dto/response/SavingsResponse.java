package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SavingsResponse {
    private String id;
    private String accountNumber;
    private BigDecimal amount;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String period;
    private BigDecimal interestRate;
    private String status;
}
package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferResponse {
    private String id;
    private BigDecimal amount;
    private String fromCardLast4;
    private String toAccountNumber;
    private String beneficiaryName;
    private String note;
    private BigDecimal fee;
    private String status;
    private LocalDateTime createdAt;
}
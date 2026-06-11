package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String id;
    private String title;
    private String category;
    private BigDecimal amount;
    private String type;
    private String status;
    private String emoji;
    private String description;
    private LocalDateTime createdAt;
}
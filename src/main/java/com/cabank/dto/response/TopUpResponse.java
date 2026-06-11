package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TopUpResponse {
    private String id;
    private String cardId;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String status;
    private LocalDateTime createdAt;
}
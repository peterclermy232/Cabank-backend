package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExchangeRateResponse {
    private String id;
    private String country;
    private String currencyCode;
    private String flag;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private LocalDateTime updatedAt;
}
package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CurrencyExchangeResponse {
    private String id;
    private String fromCurrencyCode;
    private String toCurrencyCode;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal exchangeRate;
    private String status;
    private LocalDateTime createdAt;
}

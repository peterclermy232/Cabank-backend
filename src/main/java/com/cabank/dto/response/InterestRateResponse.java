package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InterestRateResponse {
    private String id;
    private String kind;
    private String deposit;
    private BigDecimal rate;
    private LocalDateTime updatedAt;
}

package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CurrencyExchangeRequest {

    @NotBlank
    private String fromCurrencyCode;

    @NotBlank
    private String toCurrencyCode;

    @NotNull
    @Positive
    private BigDecimal fromAmount;
}

package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopUpRequest {

    @NotBlank(message = "Card ID is required")
    private String cardId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String note;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
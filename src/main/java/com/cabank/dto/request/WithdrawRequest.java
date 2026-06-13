package com.cabank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequest {

    @NotBlank(message = "Card id is required")
    private String cardId;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
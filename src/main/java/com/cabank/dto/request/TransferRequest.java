package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank
    private String fromCardLast4;

    @NotBlank
    private String toAccountNumber;

    @NotBlank
    private String beneficiaryName;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String note;

    // Fix #6: OTP is now required for all transfers
    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillPaymentRequest {

    @NotBlank(message = "Bill type is required")
    private String billType;

    @NotBlank(message = "Bill code is required")
    private String billCode;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer address is required")
    private String customerAddress;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
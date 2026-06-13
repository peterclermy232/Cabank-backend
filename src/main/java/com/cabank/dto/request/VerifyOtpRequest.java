package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    // Fix #7: phone is only required for password-reset OTP (auth/verify-otp).
    // The transaction OTP endpoint (/otp/verify) uses the authenticated user's
    // identity — phone is not needed there. Made nullable so both flows work
    // with a single DTO.
    private String phone;

    @NotBlank(message = "Code is required")
    private String code;
}
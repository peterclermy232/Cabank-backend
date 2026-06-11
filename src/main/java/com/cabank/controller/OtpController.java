package com.cabank.controller;

import com.cabank.dto.request.VerifyOtpRequest;
import com.cabank.dto.response.ApiResponse;
import com.cabank.service.impl.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Map<String, String>>> requestOtp(
            @AuthenticationPrincipal UserDetails user) {
        String code = otpService.requestTransactionOtp(user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Verification code sent", Map.of("code", code)));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest req,
            @AuthenticationPrincipal UserDetails user) {
        otpService.verifyTransactionOtp(user.getUsername(), req.getCode());
        return ResponseEntity.ok(ApiResponse.ok("Code verified", null));
    }
}
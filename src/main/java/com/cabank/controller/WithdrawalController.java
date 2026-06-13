package com.cabank.controller;

import com.cabank.dto.request.WithdrawRequest;
import com.cabank.dto.response.ApiResponse;
import com.cabank.dto.response.WithdrawResponse;
import com.cabank.service.impl.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/withdraw")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    public ResponseEntity<ApiResponse<WithdrawResponse>> withdraw(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody WithdrawRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Withdrawal successful",
                        withdrawalService.withdraw(user.getUsername(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WithdrawResponse>>> history(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(withdrawalService.getWithdrawals(user.getUsername())));
    }
}
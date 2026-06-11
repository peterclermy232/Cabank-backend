package com.cabank.controller;

import com.cabank.dto.request.SavingsRequest;
import com.cabank.dto.response.*;
import com.cabank.service.impl.SavingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavingsResponse>> createSavings(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody SavingsRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Savings account created",
                        savingsService.createSavings(user.getUsername(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingsResponse>>> getSavings(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(savingsService.getSavings(user.getUsername())));
    }
}
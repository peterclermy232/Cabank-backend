package com.cabank.controller;

import com.cabank.dto.request.CurrencyExchangeRequest;
import com.cabank.dto.response.ApiResponse;
import com.cabank.dto.response.CurrencyExchangeResponse;
import com.cabank.service.impl.CurrencyExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    @PostMapping
    public ResponseEntity<ApiResponse<CurrencyExchangeResponse>> createExchange(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CurrencyExchangeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Exchange completed",
                        currencyExchangeService.createExchange(user.getUsername(), req)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CurrencyExchangeResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                ApiResponse.ok(currencyExchangeService.getHistory(user.getUsername())));
    }
}

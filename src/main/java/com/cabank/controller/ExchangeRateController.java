package com.cabank.controller;

import com.cabank.dto.response.*;
import com.cabank.service.impl.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExchangeRateResponse>>> getRates() {
        return ResponseEntity.ok(ApiResponse.ok(exchangeRateService.getAllRates()));
    }
}
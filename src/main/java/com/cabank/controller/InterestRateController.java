package com.cabank.controller;

import com.cabank.dto.response.ApiResponse;
import com.cabank.dto.response.InterestRateResponse;
import com.cabank.service.impl.InterestRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interest-rates")
@RequiredArgsConstructor
public class InterestRateController {

    private final InterestRateService interestRateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterestRateResponse>>> getRates() {
        return ResponseEntity.ok(ApiResponse.ok(interestRateService.getAllRates()));
    }
}

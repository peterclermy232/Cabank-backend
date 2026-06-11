package com.cabank.controller;

import com.cabank.dto.request.DepositRequest;
import com.cabank.dto.response.DepositResponse;
import com.cabank.service.impl.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    public ResponseEntity<DepositResponse> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequest request) {

        DepositResponse response = depositService.deposit(
                userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
package com.cabank.controller;

import com.cabank.dto.request.TopUpRequest;
import com.cabank.dto.response.ApiResponse;
import com.cabank.dto.response.TopUpResponse;
import com.cabank.service.impl.TopUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topup")
@RequiredArgsConstructor
public class TopUpController {

    private final TopUpService topUpService;

    @PostMapping
    public ResponseEntity<ApiResponse<TopUpResponse>> topUp(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TopUpRequest req) {

        TopUpResponse response = topUpService.topUp(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Card topped up successfully", response));
    }
}
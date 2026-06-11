package com.cabank.controller;

import com.cabank.dto.request.TransferRequest;
import com.cabank.dto.response.*;
import com.cabank.service.impl.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody TransferRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transfer successful",
                        transferService.createTransfer(user.getUsername(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransferResponse>>> getTransfers(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(transferService.getTransfers(user.getUsername())));
    }
}
package com.cabank.controller;

import com.cabank.dto.request.BillPaymentRequest;
import com.cabank.dto.response.*;
import com.cabank.service.impl.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<BillPaymentResponse>> payBill(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody BillPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bill paid successfully",
                        billService.payBill(user.getUsername(), req)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BillPaymentResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(billService.getBillHistory(user.getUsername())));
    }
}
package com.cabank.controller;

import com.cabank.dto.request.BeneficiaryRequest;
import com.cabank.dto.response.*;
import com.cabank.service.impl.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiaries(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(
                beneficiaryService.getBeneficiaries(user.getUsername())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody BeneficiaryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Beneficiary added",
                        beneficiaryService.addBeneficiary(user.getUsername(), req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        beneficiaryService.deleteBeneficiary(id, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Beneficiary deleted", null));
    }
}
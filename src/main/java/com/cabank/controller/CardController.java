package com.cabank.controller;

import com.cabank.dto.request.AddCardRequest;
import com.cabank.dto.response.*;
import com.cabank.service.impl.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.getCards(user.getUsername())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardResponse>> getCard(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.getCard(id, user.getUsername())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CardResponse>> addCard(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody AddCardRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card added", cardService.addCard(user.getUsername(), req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        cardService.deleteCard(id, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Card deleted", null));
    }
}
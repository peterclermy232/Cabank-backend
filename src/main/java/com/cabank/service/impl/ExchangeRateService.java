package com.cabank.service.impl;

import com.cabank.dto.response.ExchangeRateResponse;
import com.cabank.entity.ExchangeRate;
import com.cabank.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public List<ExchangeRateResponse> getAllRates() {
        return exchangeRateRepository.findAllByOrderByCountryAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ExchangeRateResponse toResponse(ExchangeRate r) {
        return ExchangeRateResponse.builder()
                .id(r.getId())
                .country(r.getCountry())
                .currencyCode(r.getCurrencyCode())
                .flag(r.getFlag())
                .buyRate(r.getBuyRate())
                .sellRate(r.getSellRate())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
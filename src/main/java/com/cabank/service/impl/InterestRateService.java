package com.cabank.service.impl;

import com.cabank.dto.response.InterestRateResponse;
import com.cabank.entity.InterestRate;
import com.cabank.repository.InterestRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterestRateService {

    private final InterestRateRepository interestRateRepository;

    public List<InterestRateResponse> getAllRates() {
        return interestRateRepository.findAllByOrderByDepositAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private InterestRateResponse toResponse(InterestRate r) {
        return InterestRateResponse.builder()
                .id(r.getId())
                .kind(r.getKind())
                .deposit(r.getDeposit())
                .rate(r.getRate())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}

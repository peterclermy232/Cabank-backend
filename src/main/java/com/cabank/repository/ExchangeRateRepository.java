package com.cabank.repository;

import com.cabank.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
    List<ExchangeRate> findAllByOrderByCountryAsc();
    Optional<ExchangeRate> findByCurrencyCode(String currencyCode);
}
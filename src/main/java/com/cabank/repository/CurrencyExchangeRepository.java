package com.cabank.repository;

import com.cabank.entity.CurrencyExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchange, String> {
    List<CurrencyExchange> findByUserEmailOrderByCreatedAtDesc(String email);
}

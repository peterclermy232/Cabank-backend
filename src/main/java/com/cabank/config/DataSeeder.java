package com.cabank.config;

import com.cabank.entity.*;
import com.cabank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ExchangeRateRepository exchangeRateRepository;

    @Override
    public void run(String... args) {
        if (exchangeRateRepository.count() > 0) return; // already seeded

        exchangeRateRepository.saveAll(java.util.List.of(
                rate("United States", "USD", "🇺🇸", "1.0000", "1.0000"),
                rate("Euro Zone",     "EUR", "🇪🇺", "0.9200", "0.9350"),
                rate("United Kingdom","GBP", "🇬🇧", "0.7900", "0.8050"),
                rate("Japan",         "JPY", "🇯🇵", "149.50", "151.00"),
                rate("Kenya",         "KES", "🇰🇪", "128.00", "132.00"),
                rate("China",         "CNY", "🇨🇳", "7.2400", "7.3100")
        ));
    }

    private ExchangeRate rate(String country, String code, String flag,
                              String buy, String sell) {
        return ExchangeRate.builder()
                .country(country).currencyCode(code).flag(flag)
                .buyRate(new BigDecimal(buy))
                .sellRate(new BigDecimal(sell))
                .build();
    }
}
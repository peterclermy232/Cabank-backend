package com.cabank.config;

import com.cabank.entity.*;
import com.cabank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String INDIVIDUAL = "Individual customers";
    private static final String CORPORATE  = "Corporate customers";

    private final ExchangeRateRepository exchangeRateRepository;
    private final InterestRateRepository interestRateRepository;

    @Override
    public void run(String... args) {
        seedExchangeRates();
        seedInterestRates();
    }

    private void seedExchangeRates() {
        if (exchangeRateRepository.count() > 0) return;

        exchangeRateRepository.saveAll(List.of(
                rate("United States",  "USD", "🇺🇸", "1.0000",  "1.0000"),
                rate("Euro Zone",      "EUR", "🇪🇺", "0.9200",  "0.9350"),
                rate("United Kingdom", "GBP", "🇬🇧", "0.7900",  "0.8050"),
                rate("Japan",          "JPY", "🇯🇵", "149.50",  "151.00"),
                rate("Kenya",          "KES", "🇰🇪", "128.00",  "132.00"),
                rate("China",          "CNY", "🇨🇳", "7.2400",  "7.3100")
        ));
    }

    private void seedInterestRates() {
        if (interestRateRepository.count() > 0) return;

        interestRateRepository.saveAll(List.of(
                interest(INDIVIDUAL, "1m",  "4.50"),
                interest(CORPORATE,  "2m",  "5.50"),
                interest(INDIVIDUAL, "3m",  "4.50"),
                interest(CORPORATE,  "6m",  "2.50"),
                interest(INDIVIDUAL, "6m",  "4.50"),
                interest(CORPORATE,  "8m",  "6.50"),
                interest(INDIVIDUAL, "9m",  "4.50"),
                interest(INDIVIDUAL, "10m", "4.50"),
                interest(CORPORATE,  "7m",  "6.80"),
                interest(INDIVIDUAL, "11m", "4.50"),
                interest(INDIVIDUAL, "12m", "5.90"),
                interest(CORPORATE,  "12m", "7.20")
        ));
    }

    // ── builders ────────────────────────────────────────────────────────────

    private ExchangeRate rate(String country, String code, String flag,
                              String buy, String sell) {
        return ExchangeRate.builder()
                .country(country).currencyCode(code).flag(flag)
                .buyRate(new BigDecimal(buy))
                .sellRate(new BigDecimal(sell))
                .build();
    }

    private InterestRate interest(String kind, String deposit, String rate) {
        return InterestRate.builder()
                .kind(kind)
                .deposit(deposit)
                .rate(new BigDecimal(rate))
                .build();
    }
}

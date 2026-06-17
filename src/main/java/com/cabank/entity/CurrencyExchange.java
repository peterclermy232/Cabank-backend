package com.cabank.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "currency_exchanges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CurrencyExchange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fromCurrencyCode;

    @Column(nullable = false)
    private String toCurrencyCode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal fromAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal toAmount;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExchangeStatus status = ExchangeStatus.COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum ExchangeStatus {
        COMPLETED, FAILED
    }
}

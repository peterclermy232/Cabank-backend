package com.cabank.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exchange_rates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExchangeRate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private String flag;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal buyRate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sellRate;
}

package com.cabank.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "interest_rates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterestRate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String kind;

    @Column(nullable = false)
    private String deposit;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;
}

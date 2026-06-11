package com.cabank.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transfers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String fromCardLast4;

    @Column(nullable = false)
    private String toAccountNumber;

    @Column(nullable = false)
    private String beneficiaryName;

    private String note;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal fee = new BigDecimal("10.00");

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransferStatus status = TransferStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum TransferStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}

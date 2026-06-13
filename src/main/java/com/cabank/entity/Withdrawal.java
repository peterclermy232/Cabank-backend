package com.cabank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String cardLast4;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum WithdrawalStatus {
        COMPLETED, PENDING, FAILED
    }
}
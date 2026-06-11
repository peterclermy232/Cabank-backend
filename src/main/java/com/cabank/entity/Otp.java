package com.cabank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Otp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private String phone;

    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean verified = false;

    @Builder.Default
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OtpPurpose purpose = OtpPurpose.PASSWORD_RESET;

    public enum OtpPurpose {
        PASSWORD_RESET, TRANSACTION
    }
}
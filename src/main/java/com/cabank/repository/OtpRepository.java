package com.cabank.repository;

import com.cabank.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, String> {
    Optional<Otp> findTopByPhoneAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String phone, Otp.OtpPurpose purpose);

    Optional<Otp> findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String userId, Otp.OtpPurpose purpose);
}
package com.cabank.repository;

import com.cabank.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, String> {
    List<Withdrawal> findByUserIdOrderByCreatedAtDesc(String userId);
}
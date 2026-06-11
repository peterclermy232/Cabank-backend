package com.cabank.repository;

import com.cabank.entity.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, String> {
    List<Savings> findByUserId(String userId);
    List<Savings> findByUserIdAndStatus(String userId, Savings.SavingsStatus status);
}
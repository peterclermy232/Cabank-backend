package com.cabank.repository;

import com.cabank.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {
    List<Beneficiary> findByUserId(String userId);
    boolean existsByUserIdAndAccountNumber(String userId, String accountNumber);
}
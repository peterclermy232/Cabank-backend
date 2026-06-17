package com.cabank.repository;

import com.cabank.entity.InterestRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterestRateRepository extends JpaRepository<InterestRate, String> {
    List<InterestRate> findAllByOrderByDepositAsc();
}

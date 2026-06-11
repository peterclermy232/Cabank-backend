package com.cabank.repository;

import com.cabank.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByUserId(String userId);
    List<Card> findByUserIdAndActiveTrue(String userId);
}
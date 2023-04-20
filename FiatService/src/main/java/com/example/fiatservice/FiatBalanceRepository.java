package com.example.fiatservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiatBalanceRepository extends JpaRepository<FiatBalance, Long> {
    Optional<FiatBalance> findByUserIdAndCurrency(Long userId, String currency);
}
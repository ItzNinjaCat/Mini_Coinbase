package com.example.cryptoservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoBalanceRepository extends JpaRepository<CryptoBalance, Long>
{
    Optional<CryptoBalance> findByUserIdAndCrypto(Long userId, String crypto);
}
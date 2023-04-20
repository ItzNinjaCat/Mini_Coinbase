package com.example.cryptoservice;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "crypto_balances")
public class CryptoBalance
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "crypto", nullable = false)
    private String crypto;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "reserved", nullable = false)
    private BigDecimal reserved;
}

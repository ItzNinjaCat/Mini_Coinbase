package com.example.fiatservice;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data

@Entity
@Table(name = "fiat_balances")
public class FiatBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "reserved", nullable = false)
    private BigDecimal reserved;
}

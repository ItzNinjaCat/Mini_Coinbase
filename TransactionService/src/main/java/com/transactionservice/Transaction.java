package com.transactionservice;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "fiat_currency", nullable = false)
    private String fiatCurrency;

    @Column(name = "crypto_currency", nullable = false)
    private String cryptoCurrency;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status", nullable = false)
    private String status;
    public enum TransactionType {
        BUY,
        SELL
    }
}
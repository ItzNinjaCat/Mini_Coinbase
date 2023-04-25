package com.transactionservice;


import lombok.Data;

import java.math.BigDecimal;


@Data
public class TransactionDto
{
    private Long id;
    private Long userId;
    private TransactionType transactionType;
    private String fiatCurrency;
    private String cryptoCurrency;
    private BigDecimal price;
    private BigDecimal quantity;
    private Status status;
    private String source;

    public enum TransactionType
    {
        BUY,
        SELL
    }

    public enum Status
    {
        NEW,
        CONFIRMED,
        ROLLBACK,
        ACCEPT,
        REJECT,
        REJECTED,
    }

}

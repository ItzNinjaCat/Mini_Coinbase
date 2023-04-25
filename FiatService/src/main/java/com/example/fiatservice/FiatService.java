package com.example.fiatservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.fiatservice.CustomExceptions.UnsupportedCurrencyException;
import com.example.fiatservice.CustomExceptions.InsufficientFundsException;
import com.example.fiatservice.CustomExceptions.InvalidAmountException;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FiatService
{

    private static final Logger LOG = LoggerFactory.getLogger(FiatService.class);

    @Value("${spring.application.name}")
    private String SOURCE;

    @Autowired
    private FiatBalanceRepository fiatRepository;

    @Value("${supported.fiatcurrencies}")
    private String supportedFiatCurrencies;

    @Autowired
    private KafkaTemplate<Long, TransactionDto> transactionKafkaTemplate;

    @Autowired
    private KafkaTemplate<byte[], DepositWithdraw> depositWithdrawKafkaTemplate;

    public String getSupportedFiatCurrencies()
    {
        return supportedFiatCurrencies;
    }

    public List<FiatBalance> initializeFiatBalancesForUser(Long userId)
    {
        List<FiatBalance> fiatBalances = new ArrayList<>();
        for (String currency : supportedFiatCurrencies.split(","))
        {
            FiatBalance fiatBalance = new FiatBalance();
            fiatBalance.setUserId(userId);
            fiatBalance.setCurrency(currency);
            fiatBalance.setBalance(BigDecimal.ZERO);
            fiatBalance.setReserved(BigDecimal.ZERO);
            fiatBalances.add(fiatBalance);
            fiatRepository.save(fiatBalance);
        }
        return fiatBalances;
    }

    public void reserve(TransactionDto transaction)
    {
        switch (transaction.getTransactionType())
        {
            case BUY:
                reserveBuy(transaction);
                break;
            case SELL:
                reserveSell(transaction);
                break;
        }
    }
    public void confirm(TransactionDto transaction)
    {
        transaction.setSource(SOURCE);
        FiatBalance fiatBalance = fiatRepository.findByUserIdAndCurrency(transaction.getUserId(), transaction.getFiatCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

        switch (transaction.getTransactionType())
        {
            case BUY:
                fiatBalance.setReserved(fiatBalance.getReserved().subtract(transaction.getPrice()));
                break;
            case SELL:
                fiatBalance.setBalance(fiatBalance.getBalance().add(transaction.getPrice()));
                fiatBalance.setReserved(fiatBalance.getReserved().subtract(transaction.getPrice()));
                break;
        }
        fiatRepository.save(fiatBalance);

        transactionKafkaTemplate.send("fiat", transaction.getId(), transaction);
        LOG.info("Sent: {}", transaction);
    }
    public void reserveBuy(TransactionDto transaction)
    {
        transaction.setSource(SOURCE);
        FiatBalance fiatBalance = fiatRepository.findByUserIdAndCurrency(transaction.getUserId(), transaction.getFiatCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));


        if (fiatBalance.getBalance().compareTo(transaction.getPrice()) < 0)
        {
            transaction.setStatus(TransactionDto.Status.REJECT);
        } else
        {
            fiatBalance.setBalance(fiatBalance.getBalance().subtract(transaction.getPrice()));
            fiatBalance.setReserved(fiatBalance.getReserved().add(transaction.getPrice()));
            fiatRepository.save(fiatBalance);
            transaction.setStatus(TransactionDto.Status.ACCEPT);
        }

        transactionKafkaTemplate.send("fiat", transaction.getId(), transaction);
        LOG.info("Sent: {}", transaction);
    }

    private void reserveSell(TransactionDto transaction)
    {
        transaction.setSource(SOURCE);
        FiatBalance fiatBalance = fiatRepository.findByUserIdAndCurrency(transaction.getUserId(), transaction.getFiatCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

        transaction.setStatus(TransactionDto.Status.ACCEPT);
        fiatBalance.setReserved(fiatBalance.getReserved().add(transaction.getPrice()));
        fiatRepository.save(fiatBalance);

        transactionKafkaTemplate.send("fiat", transaction.getId(), transaction);
        LOG.info("Sent: {}", transaction);
    }

    public void rollback(TransactionDto transaction)
    {
        FiatBalance fiatBalance = fiatRepository.findByUserIdAndCurrency(transaction.getUserId(), transaction.getFiatCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

        switch (transaction.getTransactionType())
        {
            case BUY:
                fiatBalance.setReserved(fiatBalance.getReserved().subtract(transaction.getPrice()));
                fiatBalance.setBalance(fiatBalance.getBalance().add(transaction.getPrice()));
                break;
            case SELL:
                fiatBalance.setReserved(fiatBalance.getReserved().subtract(transaction.getPrice()));
                break;
        }

        fiatRepository.save(fiatBalance);
    }

    @Transactional
    public void deposit(DepositWithdraw request) throws UnsupportedCurrencyException, InvalidAmountException
    {
        if (!isCurrencySupported(request.getCurrency()))
        {
            throw new UnsupportedCurrencyException("Unsupported currency: " + request.getCurrency());
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new InvalidAmountException("Invalid amount: " + request.getAmount());
        }

        // Update the user's fiat balance in the database
        FiatBalance fiatBalance = fiatRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

        fiatBalance.setBalance(fiatBalance.getBalance().add(request.getAmount()));
        fiatRepository.save(fiatBalance);
        depositWithdrawKafkaTemplate.send("deposit-completed", null, request);
    }

    @Transactional
    public void withdraw(DepositWithdraw request) throws UnsupportedCurrencyException, InsufficientFundsException, InvalidAmountException
    {
        if (!isCurrencySupported(request.getCurrency()))
        {
            throw new UnsupportedCurrencyException("Unsupported currency: " + request.getCurrency());
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new InvalidAmountException("Invalid amount: " + request.getAmount());
        }

        // Update the user's fiat balance in the database
        FiatBalance fiatBalance = fiatRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

        if (fiatBalance.getBalance().subtract(request.getAmount()).compareTo(BigDecimal.ZERO) < 0)
        {
            throw new InsufficientFundsException("Insufficient funds.");
        }

        fiatBalance.setBalance(fiatBalance.getBalance().subtract(request.getAmount()));
        fiatRepository.save(fiatBalance);
        depositWithdrawKafkaTemplate.send("withdraw-completed", null, request);
    }

    private boolean isCurrencySupported(String currency)
    {
        for (String supportedCurrency : supportedFiatCurrencies.split(","))
        {
            if (supportedCurrency.equals(currency))
            {
                return true;
            }
        }
        return false;
    }

}

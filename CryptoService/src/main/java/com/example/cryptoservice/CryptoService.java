package com.example.cryptoservice;

import com.shared.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CryptoService
{
    private static final Logger LOG = LoggerFactory.getLogger(CryptoService.class);

    @Value("${spring.application.name}")
    private String SOURCE;

    @Autowired
    private CryptoBalanceRepository cryptoBalanceRepository;

    @Value("${supported.cryptocurrencies}")
    private String supportedCryptoCurrencies;

    @Autowired
    private CoinGeckoService coinGeckoClient;

    @Autowired
    private KafkaTemplate<Long, TransactionDto> transactionKafkaTemplate;


    public List<CryptoBalance> initializeCryptoBalancesForUser(Long userId)
    {
        List<CryptoBalance> CryptoBalances = new ArrayList<>();
        for (String currency : supportedCryptoCurrencies.split(","))
        {
            CryptoBalance cryptoBalance = new CryptoBalance();
            cryptoBalance.setUserId(userId);
            cryptoBalance.setCrypto(currency);
            cryptoBalance.setBalance(BigDecimal.ZERO);
            cryptoBalance.setReserved(BigDecimal.ZERO);
            CryptoBalances.add(cryptoBalance);
            cryptoBalanceRepository.save(cryptoBalance);
        }
        return CryptoBalances;
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

    private void reserveBuy(TransactionDto transaction)
    {
        transaction.setSource(SOURCE);
        if (transaction.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            transaction.setStatus(TransactionDto.Status.REJECT);
        } else
        {
            transaction.setStatus(TransactionDto.Status.ACCEPT);
            CryptoBalance cryptoBalance = cryptoBalanceRepository.findByUserIdAndCrypto(transaction.getUserId(), transaction.getCryptoCurrency())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

            cryptoBalance.setReserved(cryptoBalance.getReserved().add(transaction.getQuantity()));
            cryptoBalanceRepository.save(cryptoBalance);
        }

        transactionKafkaTemplate.send("crypto", transaction.getId(), transaction);
        LOG.info("Sent: {}", transaction);
    }

    private void reserveSell(TransactionDto transaction)
    {
        transaction.setSource(SOURCE);
        if (transaction.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            transaction.setStatus(TransactionDto.Status.REJECT);
        } else
        {
            CryptoBalance cryptoBalance = cryptoBalanceRepository.findByUserIdAndCrypto(transaction.getUserId(), transaction.getCryptoCurrency())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

            if (cryptoBalance.getBalance().compareTo(transaction.getQuantity()) < 0)
            {
                transaction.setStatus(TransactionDto.Status.REJECT);
            } else
            {
                transaction.setStatus(TransactionDto.Status.ACCEPT);
                cryptoBalance.setBalance(cryptoBalance.getBalance().subtract(transaction.getQuantity()));
                cryptoBalance.setReserved(cryptoBalance.getReserved().add(transaction.getQuantity()));
                cryptoBalanceRepository.save(cryptoBalance);
            }
        }
        transactionKafkaTemplate.send("crypto", transaction.getId(), transaction);
        LOG.info("Sent: {}", transaction);
    }

    public void confirm(TransactionDto transactionDto)
    {
        CryptoBalance cryptoBalance = cryptoBalanceRepository.findByUserIdAndCrypto(transactionDto.getUserId(), transactionDto.getCryptoCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));

        switch (transactionDto.getTransactionType())
        {
            case BUY:
                cryptoBalance.setBalance(cryptoBalance.getBalance().add(transactionDto.getQuantity()));
                cryptoBalance.setReserved(cryptoBalance.getReserved().subtract(transactionDto.getQuantity()));
                break;
            case SELL:
                cryptoBalance.setReserved(cryptoBalance.getReserved().subtract(transactionDto.getQuantity()));
                break;
        }
//        cryptoBalance.setBalance(cryptoBalance.getBalance().add(transactionDto.getQuantity()));
//        cryptoBalance.setReserved(cryptoBalance.getReserved().subtract(transactionDto.getQuantity()));
        cryptoBalanceRepository.save(cryptoBalance);
    }

    public void rollback(TransactionDto transactionDto)
    {
        CryptoBalance cryptoBalance = cryptoBalanceRepository.findByUserIdAndCrypto(transactionDto.getUserId(), transactionDto.getCryptoCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id or currency."));
        switch (transactionDto.getTransactionType())
        {
            case BUY:
                cryptoBalance.setReserved(cryptoBalance.getReserved().subtract(transactionDto.getQuantity()));
                break;
            case SELL:
                cryptoBalance.setBalance(cryptoBalance.getBalance().add(transactionDto.getQuantity()));
                cryptoBalance.setReserved(cryptoBalance.getReserved().subtract(transactionDto.getQuantity()));
                break;
        }
        cryptoBalanceRepository.save(cryptoBalance);
    }

    public BigDecimal getPrice(String cryptoCurrency, String fiatCurrency)
    {
        String apiName = CoinGeckoMapper.getCryptoApiName(cryptoCurrency);
        System.out.println("Api name: " + apiName);
        if (apiName == null)
        {
            return null;
        }

        return coinGeckoClient.getPrice(apiName, fiatCurrency.toLowerCase());
    }
}

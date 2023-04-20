package com.transactionservice;

import com.shared.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransactionService
{
    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private AtomicLong id = new AtomicLong();

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private KafkaTemplate<Long, TransactionDto> template;

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    public BigDecimal getCryptoPrice(String cryptoCurrency, String fiatCurrency)
    {
        String url = "http://localhost:8082/api/crypto/{cryptoCurrency}/price/{fiatCurrency}";
        BigDecimal price = null;

        try
        {
            ResponseEntity<BigDecimal> response = restTemplate.getForEntity(url, BigDecimal.class, cryptoCurrency, fiatCurrency);
            if (response.getStatusCode() == HttpStatus.OK)
            {
                price = response.getBody();
            }
        } catch (HttpClientErrorException e)
        {
            // Handle specific HTTP errors (e.g., 400 Bad Request, 404 Not Found)
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e)
        {
            // Handle other exceptions
            System.err.println("Error: " + e.getMessage());
        }

        return price;
    }

    public boolean isFiatCurrencySupported(String fiatCurrency)
    {
        String url = "http://localhost:8083/api/fiat/supported-currencies";
        String supportedCurrenciesString = restTemplate.getForObject(url, String.class);

        if (supportedCurrenciesString != null)
        {
            Set<String> supportedCurrencies = new HashSet<>(Arrays.asList(supportedCurrenciesString.split(",")));
            return supportedCurrencies.contains(fiatCurrency);
        }

        return false;
    }

    public void buyCrypto(TransactionDto transactionDto)
    {
        BigDecimal price = getCryptoPrice(transactionDto.getCryptoCurrency(), transactionDto.getFiatCurrency());
        if (price == null)
        {
            throw new RuntimeException("Couldn't establish the price of the crypto currency");
        }

        boolean isFiatCurrencySupported = isFiatCurrencySupported(transactionDto.getFiatCurrency());
        if (!isFiatCurrencySupported)
        {
            throw new RuntimeException("Fiat currency not supported");
        }
        if(transactionDto.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("Quantity must be greater than 0");
        }


        transactionDto.setPrice(transactionDto.getQuantity().multiply(price));
        transactionDto.setTransactionType(TransactionDto.TransactionType.BUY);
        transactionDto.setStatus(TransactionDto.Status.NEW);

        transactionDto.setId(id.incrementAndGet());
        template.send("transactions", transactionDto.getId(), transactionDto);

        Transaction transaction = new Transaction();
        transaction.setUserId(transactionDto.getUserId());
        transaction.setTransactionType(Transaction.TransactionType.valueOf(transactionDto.getTransactionType().name()));
        transaction.setPrice(transactionDto.getPrice());
        transaction.setQuantity(transactionDto.getQuantity());
        transaction.setFiatCurrency(transactionDto.getFiatCurrency());
        transaction.setCryptoCurrency(transactionDto.getCryptoCurrency());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("PENDING");
        transactionRepository.save(transaction);
    }

    public void sellCrypto(TransactionDto transactionDto)
    {
//        transactionDto.setId(id.incrementAndGet());
//        transactionDto.setTransactionType(TransactionDto.TransactionType.SELL);
//        transactionDto.setStatus(TransactionDto.Status.NEW);
//
//        template.send("transactions", transactionDto.getId(), transactionDto);
    }

    public TransactionDto confirm(TransactionDto fiat, TransactionDto crypto)
    {
        TransactionDto main = new TransactionDto();
        main.setId(fiat.getId());
        main.setUserId(fiat.getUserId());
        main.setTransactionType(fiat.getTransactionType());
        main.setFiatCurrency(fiat.getFiatCurrency());
        main.setCryptoCurrency(fiat.getCryptoCurrency());
        main.setPrice(fiat.getPrice());
        main.setQuantity(fiat.getQuantity());
        main.setSource("transaction-service");

        Transaction transaction = transactionRepository.findById(fiat.getId()).orElseThrow(() -> new RuntimeException("Transaction not found"));
        if(fiat.getStatus().equals(TransactionDto.Status.ACCEPT) && crypto.getStatus().equals(TransactionDto.Status.ACCEPT))
        {
            transaction.setStatus("CONFIRMED");
            main.setStatus(TransactionDto.Status.CONFIRMED);
        }
        else if(fiat.getStatus().equals(TransactionDto.Status.REJECT) && crypto.getStatus().equals(TransactionDto.Status.REJECT))
        {
            transaction.setStatus("REJECTED");
            main.setStatus(TransactionDto.Status.REJECTED);
        }
        else if(fiat.getStatus().equals(TransactionDto.Status.REJECT) || crypto.getStatus().equals(TransactionDto.Status.REJECT))
        {
            transaction.setStatus("REJECTED");
            String source = fiat.getStatus().equals(TransactionDto.Status.REJECT) ? "fiat-service" : "crypto-service";
            main.setStatus(TransactionDto.Status.ROLLBACK);
            main.setSource(source);
        }
        transactionRepository.save(transaction);

        return main;
    }
}

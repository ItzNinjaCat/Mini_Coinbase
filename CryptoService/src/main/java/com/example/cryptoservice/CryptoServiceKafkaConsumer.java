package com.example.cryptoservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@EnableKafka
public class CryptoServiceKafkaConsumer
{
    private static final Logger LOG = LoggerFactory.getLogger(CryptoServiceKafkaConsumer.class);

    @Autowired
    private CryptoService cryptoService;

    @KafkaListener(topics = "user-registered", groupId = "cryptoServiceUserRegistrationGroup", containerFactory = "userRegistrationKafkaListenerContainerFactory")
    public void onUserRegistration(String message) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        long  userId = Long.parseLong(map.get("userId").toString());
        LOG.info("Initializing fiat balances for user: {}", userId);
        cryptoService.initializeCryptoBalancesForUser(userId);
    }

    @KafkaListener(topics = "transactions", groupId = "cryptoServiceTransactionGroup", containerFactory = "transactionKafkaListenerContainerFactory")
    public void onTransaction(TransactionDto transaction) // ConsumerRecord<Long, TransactionDto> record
    {
        LOG.info("Received: {}", transaction);
        if(transaction.getStatus() == null) // this shouldn't be happening but it does
        {
            LOG.error("Transaction status is null");
            return;
        }
        if (transaction.getStatus().equals(TransactionDto.Status.NEW))
        {
            LOG.info("RESERVE FROM CRYPTO APP");
            cryptoService.reserve(transaction);
        } else if (transaction.getStatus().equals(TransactionDto.Status.CONFIRMED))
        {
            LOG.info("CONFIRM FROM CRYPTO APP");
            cryptoService.confirm(transaction);
        } else if (transaction.getStatus().equals(TransactionDto.Status.ROLLBACK) && !transaction.getSource().equals("crypto-service"))
        {
            LOG.info("ROLLBACK FROM CRYPTO APP");
            cryptoService.rollback(transaction);
        }
    }
}

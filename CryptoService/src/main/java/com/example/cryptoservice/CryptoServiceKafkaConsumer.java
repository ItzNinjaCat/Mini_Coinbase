package com.example.cryptoservice;

import com.shared.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@EnableKafka
public class CryptoServiceKafkaConsumer
{
    private static final Logger LOG = LoggerFactory.getLogger(CryptoServiceKafkaConsumer.class);

    @Autowired
    private CryptoService cryptoService;

    @KafkaListener(topics = "user_registered", groupId = "cryptoServiceUserRegistrationGroup", containerFactory = "userRegistrationKafkaListenerContainerFactory")
    public void onUserRegistration(Long userId)
    {
        LOG.info("Initializing crypto balances for user: {}", userId);
        cryptoService.initializeCryptoBalancesForUser(userId);
    }

    @KafkaListener(topics = "transactions", groupId = "cryptoServiceTransactionGroup", containerFactory = "transactionKafkaListenerContainerFactory")
    public void onTransaction(TransactionDto transaction) // ConsumerRecord<Long, TransactionDto> record
    {
        LOG.info("Received: {}", transaction);
        if (transaction.getStatus().equals(TransactionDto.Status.NEW))
        {
            LOG.info("RESERVE FROM PAYMENT APP");
            cryptoService.reserve(transaction);
        } else if (transaction.getStatus().equals(TransactionDto.Status.CONFIRMED))
        {
            LOG.info("CONFIRM FROM PAYMENT APP");
            cryptoService.confirm(transaction);
        } else if (transaction.getStatus().equals(TransactionDto.Status.ROLLBACK) && !transaction.getSource().equals("crypto-service"))
        {
            cryptoService.rollback(transaction);
            LOG.info("ROLLBACK FROM PAYMENT APP");
        }

    }
}

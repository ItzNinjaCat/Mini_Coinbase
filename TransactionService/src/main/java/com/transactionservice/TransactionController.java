package com.transactionservice;

import com.shared.TransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/transaction")
public class TransactionController
{
    @Autowired
    private TransactionService transactionService;


    @PostMapping("/buy")
    public ResponseEntity<?> buyCrypto(@RequestBody TransactionDto transactionDto)
    {
        // Check if the user is authenticated
//        System.out.println(transactionDto);
        try
        {
            transactionService.buyCrypto(transactionDto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellCrypto(@RequestBody TransactionDto transactionDto)
    {
        try
        {
            transactionService.sellCrypto(transactionDto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
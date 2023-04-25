package com.transactionservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
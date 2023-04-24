package com.example.fiatservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.fiatservice.CustomExceptions.UnsupportedCurrencyException;
import com.example.fiatservice.CustomExceptions.InsufficientFundsException;
import com.example.fiatservice.CustomExceptions.InvalidAmountException;

@RestController
@RequestMapping("/api/fiat")
public class FiatController {

    @Autowired
    private FiatService fiatService;


    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositWithdraw request) {
        System.out.println("Deposit request: " + request);
        try {
            fiatService.deposit(request);
            return ResponseEntity.ok().build();
        } catch (UnsupportedCurrencyException | InvalidAmountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody DepositWithdraw request) {
        System.out.println("Withdraw request: " + request);
        try {
            fiatService.withdraw(request);
            return ResponseEntity.ok().build();
        } catch (UnsupportedCurrencyException | InvalidAmountException | InsufficientFundsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/supported-currencies")
    public ResponseEntity<?> supportedCurrencies() {
        return ResponseEntity.ok(fiatService.getSupportedFiatCurrencies());
    }
}
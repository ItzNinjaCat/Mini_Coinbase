package com.example.cryptoservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/crypto")
public class CryptoServiceController
{

    @Autowired
    private CryptoBalanceRepository cryptoBalanceRepository;

    @Autowired
    private CryptoService cryptoService;

    @GetMapping("/{cryptoCurrency}/price/{fiatCurrency}")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable String cryptoCurrency,
                                               @PathVariable String fiatCurrency)
    {
        BigDecimal price = cryptoService.getPrice(cryptoCurrency, fiatCurrency);

        if (price == null)
        {
            return ResponseEntity.badRequest().body(null);
        } else
        {
            return ResponseEntity.ok(price);
        }
    }

//    @PostMapping("/exchange")
//    public ResponseEntity<BigDecimal> getPrice(@RequestParam long userId,
//                                               @RequestParam int amount,
//                                               @RequestParam String crypto,
//                                               @RequestParam String cryptoReceive)
//    {
//        CryptoBalance user = cryptoBalanceRepository.findByUserIdAndCrypto(userId, crypto).orElseThrow();
//        BigDecimal userCryptoAmount = user.getBalance();
//        if (userCryptoAmount.compareTo(BigDecimal.valueOf(amount)) < 0) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        double rate = getExchangeRate(crypto, cryptoReceive);
//
//        double receivedAmount = amount * rate;
//
//        user.setBalance(userCryptoAmount.subtract(BigDecimal.valueOf(amount)));
//
//        cryptoBalanceRepository.save(user);
//        return ResponseEntity.ok("Exchanged " + amount + " " + crypto + " for " + receivedAmount + " " + cryptoReceive).build();
//
//    }

//    private double getExchangeRate(String cryptoFrom, String cryptoTo) {
//        if (cryptoFrom == cryptoTo) {
//            return 1.0;
//        }
//
//        double cryptoFromValue = cryptoService.getPrice(cryptoFrom, "usd").doubleValue();
//        double cryptoToValue = cryptoService.getPrice(cryptoTo, "usd").doubleValue();
//
//        return cryptoFromValue / cryptoToValue;
//    }
}
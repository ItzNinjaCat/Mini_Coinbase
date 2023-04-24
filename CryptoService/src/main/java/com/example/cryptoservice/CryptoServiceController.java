package com.example.cryptoservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/crypto")
public class CryptoServiceController
{

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
}
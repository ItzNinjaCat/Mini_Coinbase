package com.example.cryptoservice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.litesoftwares.coingecko.constant.Currency;
import org.springframework.stereotype.Service;


public class CoinGeckoMapper {

    private static final Map<String, String> CRYPTO_MAP;

    static {
        Map<String, String> cryptoMap = new HashMap<>();
        cryptoMap.put("BTC", "bitcoin");
        cryptoMap.put("ETH", "ethereum");
        cryptoMap.put("DOGE", "dogecoin");
        CRYPTO_MAP = Collections.unmodifiableMap(cryptoMap);
    }


    public static String getCryptoApiName(String currency) {
        return CRYPTO_MAP.get(currency);
    }

}
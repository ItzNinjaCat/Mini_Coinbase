package com.example.cryptoservice;


import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;
import org.springframework.stereotype.Service;
import com.litesoftwares.coingecko.constant.Currency;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CoinGeckoService
{
        public BigDecimal getPrice(String coinGeckoId, String currency) {
        CoinGeckoApiClient client = new CoinGeckoApiClientImpl();
        Map<String, Map<String, Double>> priceData = client.getPrice(coinGeckoId, currency, true, true, true, true);
        if (priceData.get(coinGeckoId).get(currency) == null) {
            return null;
        }
        return BigDecimal.valueOf(priceData.get(coinGeckoId).get(currency));
    }


//    public static void main(String[] args) {
//        CoinGeckoService coinGeckoService = new CoinGeckoService();
//        System.out.println(coinGeckoService.getPrice("bitcoin","USD"));
//    }

}

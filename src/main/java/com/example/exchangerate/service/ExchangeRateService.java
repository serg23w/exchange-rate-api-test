package com.example.exchangerate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeRateService {

    @Value("${exchangerate.api.url}")
    private String apiUrl;

    @Value("${exchangerate.api.key}")
    private String apiKey;

    @Value("${exchangerate.api.operation.list}")
    private String listOperation;

    @Value("${exchangerate.api.operation.getexchangerate}")
    private String getExchangeRateOperation;

    @Value("${exchangerate.api.operation.convert}")
    private String convertOperation;

    private final RestTemplate restTemplate;
    private Map<String, String> currencies;

    public ExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isValidCurrency(String currency) {
        if (this.currencies == null) {
            String url = String.format(listOperation, apiUrl, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            this.currencies = (Map<String, String>) response.get("currencies");
        }
        return this.currencies != null && this.currencies.containsKey(currency);
    }

    @Cacheable("exchangeRates")
    public Map<String, BigDecimal> getExchangeRates(String baseCurrency) {
        if (!isValidCurrency(baseCurrency)) {
            throw new IllegalArgumentException("Invalid base currency");
        }
        String url = String.format(getExchangeRateOperation, apiUrl, baseCurrency, apiKey);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, BigDecimal> rates = new HashMap<>();
        Map<String, Object> rateData = (Map<String, Object>) response.get("quotes");
        for (Map.Entry<String, Object> entry : rateData.entrySet()) {
            rates.put(entry.getKey(), new BigDecimal(entry.getValue().toString()));
        }
        return rates;
    }

    public BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount) {
        if (!isValidCurrency(fromCurrency) || !isValidCurrency(toCurrency)) {
            throw new IllegalArgumentException("Invalid currency");
        }

        if (!isValidAmount(amount)) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        String url = String.format(convertOperation, apiUrl, fromCurrency, toCurrency, amount, apiKey);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || response.get("result") == null) {
            throw new IllegalArgumentException("Error retrieving conversion data");
        }

        return new BigDecimal(response.get("result").toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public Map<String, BigDecimal> convertToMultipleCurrencies(String fromCurrency, BigDecimal amount, String... toCurrencies) {
        if (!isValidCurrency(fromCurrency)) {
            throw new IllegalArgumentException("Invalid base currency");
        }

        if (!isValidAmount(amount)) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Map<String, BigDecimal> conversions = new HashMap<>();
        for (String toCurrency : toCurrencies) {
            if (!isValidCurrency(toCurrency)) {
                throw new IllegalArgumentException("Invalid target currency: " + toCurrency);
            }

            conversions.put(toCurrency, convert(fromCurrency, toCurrency, amount));
        }
        return conversions;
    }

}

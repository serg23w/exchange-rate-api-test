package com.example.exchangerate.controller;

import com.example.exchangerate.service.ExchangeRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/exchange")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/rate")
    public BigDecimal getExchangeRate(@RequestParam String fromCurrency, @RequestParam String toCurrency) {
        return exchangeRateService.convert(fromCurrency, toCurrency, BigDecimal.ONE);
    }

    @GetMapping("/rates")
    public Map<String, BigDecimal> getAllExchangeRates(@RequestParam String fromCurrency) {
        return exchangeRateService.getExchangeRates(fromCurrency);
    }

    @GetMapping("/convert")
    public BigDecimal convertCurrency(@RequestParam String fromCurrency, @RequestParam String toCurrency, @RequestParam BigDecimal amount) {
        return exchangeRateService.convert(fromCurrency, toCurrency, amount);
    }

    @GetMapping("/convertMultiple")
    public Map<String, BigDecimal> convertCurrencyToMultiple(@RequestParam String fromCurrency, @RequestParam BigDecimal amount, @RequestParam String[] toCurrencies) {
        return exchangeRateService.convertToMultipleCurrencies(fromCurrency, amount, toCurrencies);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

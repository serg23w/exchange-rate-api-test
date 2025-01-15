package com.example.exchangerate.controller;

import com.example.exchangerate.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ExchangeRateControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    public void testGetExchangeRate() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR";

        mockMvc.perform(get("/v1/api/exchange/rate")
                        .param("fromCurrency", fromCurrency)
                        .param("toCurrency", toCurrency))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    BigDecimal actualValue = new BigDecimal(result.getResponse().getContentAsString());
                    BigDecimal expectedValue = exchangeRateService.convert(fromCurrency, toCurrency, BigDecimal.ONE);
                    assert actualValue.compareTo(expectedValue) == 0;
                });
    }

    @Test
    public void testGetExchangeRateInvalidCurrency() throws Exception {
        String fromCurrency = "XYZ";
        String toCurrency = "EUR";

        mockMvc.perform(get("/v1/api/exchange/rate")
                        .param("fromCurrency", fromCurrency)
                        .param("toCurrency", toCurrency))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllExchangeRates() throws Exception {
        String fromCurrency = "USD";

        mockMvc.perform(get("/v1/api/exchange/rates")
                        .param("fromCurrency", fromCurrency))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    for (Map.Entry<String, BigDecimal> entry : exchangeRateService.getExchangeRates(fromCurrency).entrySet()) {
                        String currency = entry.getKey();
                        BigDecimal rate = entry.getValue();
                        assert responseBody.contains("\"" + currency + "\":" + rate);
                    }
                });
    }

    @Test
    public void testGetAllExchangeRatesInvalidCurrency() throws Exception {
        String fromCurrency = "XYZ";

        mockMvc.perform(get("/v1/api/exchange/rates")
                        .param("fromCurrency", fromCurrency))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testConvertCurrency() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        BigDecimal amount = new BigDecimal("100.00");

        mockMvc.perform(get("/v1/api/exchange/convert")
                        .param("fromCurrency", fromCurrency)
                        .param("toCurrency", toCurrency)
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    BigDecimal actualValue = new BigDecimal(result.getResponse().getContentAsString());
                    BigDecimal convertedAmount = exchangeRateService.convert(fromCurrency, toCurrency, amount);
                    assert actualValue.compareTo(convertedAmount) == 0;
                });
    }

    @Test
    public void testConvertCurrencyInvalidAmount() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        BigDecimal amount = new BigDecimal("-100.00");

        mockMvc.perform(get("/v1/api/exchange/convert")
                        .param("fromCurrency", fromCurrency)
                        .param("toCurrency", toCurrency)
                        .param("amount", amount.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testConvertCurrencyInvalidCurrency() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "XYZ";
        BigDecimal amount = new BigDecimal("100.00");

        mockMvc.perform(get("/v1/api/exchange/convert")
                        .param("fromCurrency", fromCurrency)
                        .param("toCurrency", toCurrency)
                        .param("amount", amount.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testConvertCurrencyToMultiple() throws Exception {
        String fromCurrency = "USD";
        BigDecimal amount = new BigDecimal("100.00");
        String[] toCurrencies = {"EUR", "GBP"};

        mockMvc.perform(get("/v1/api/exchange/convertMultiple")
                        .param("fromCurrency", fromCurrency)
                        .param("amount", amount.toString())
                        .param("toCurrencies", String.join(",", toCurrencies)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    Map<String, BigDecimal> mockConversions = exchangeRateService.convertToMultipleCurrencies(fromCurrency, amount, toCurrencies);
                    for (Map.Entry<String, BigDecimal> entry : mockConversions.entrySet()) {
                        String currency = entry.getKey();
                        BigDecimal rate = entry.getValue();
                        assert responseBody.contains("\"" + currency + "\":" + rate);
                    }
                });
    }

    @Test
    public void testConvertCurrencyToMultipleInvalidCurrency() throws Exception {
        String fromCurrency = "USD";
        BigDecimal amount = new BigDecimal("100.00");
        String[] toCurrencies = {"XYZ", "GBP"};

        mockMvc.perform(get("/v1/api/exchange/convertMultiple")
                        .param("fromCurrency", fromCurrency)
                        .param("amount", amount.toString())
                        .param("toCurrencies", String.join(",", toCurrencies)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testConvertCurrencyToMultipleInvalidAmount() throws Exception {
        String fromCurrency = "USD";
        BigDecimal amount = new BigDecimal("-100.00");
        String[] toCurrencies = {"EUR", "GBP"};

        mockMvc.perform(get("/v1/api/exchange/convertMultiple")
                        .param("fromCurrency", fromCurrency)
                        .param("amount", amount.toString())
                        .param("toCurrencies", String.join(",", toCurrencies)))
                .andExpect(status().isBadRequest());
    }
}


# Exchange Rate API

## Description
This API allows fetching exchange rates and performing currency conversions.

## Endpoints

1. `GET /api/exchange/rate?fromCurrency=USD&toCurrency=EUR` - Get the exchange rate between two currencies.
2. `GET /api/exchange/rates?fromCurrency=USD` - Get all exchange rates for a specific base currency.
3. `GET /api/exchange/convert?fromCurrency=USD&toCurrency=EUR&amount=100` - Convert a specific amount from one currency to another.
4. `GET /api/exchange/convertMultiple?fromCurrency=USD&amount=100&toCurrencies=EUR,GBP` - Convert a specific amount from one currency to multiple currencies.

## Setup

1. Clone the repository.
2. Add your apiKey in application.properties file. It is needed to run the tests and the application.
3. Run `mvn clean install` to build the project.
4. Run `mvn spring-boot:run` to start the application.

## Swagger UI
Access the Swagger UI at `http://localhost:8080/swagger-ui/index.html` to interact with the API.

package data.currency;

import java.math.BigDecimal;

public class CurrencyConverter {

    private final ExchangeRateClient exchangeRateClient;

    public CurrencyConverter(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    public BigDecimal convert(String base, String quote, BigDecimal amount) {
        if (base == null || base.trim().isEmpty()) throw new IllegalArgumentException("Base currency cannot be empty");
        if (quote == null || quote.trim().isEmpty()) throw new IllegalArgumentException("Quote currency cannot be empty");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

        return amount.multiply(exchangeRateClient.getCurrencyRate(base, quote).getRate());
    }
}

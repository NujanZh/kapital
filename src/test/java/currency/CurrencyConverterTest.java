package currency;

import data.currency.CurrencyConverter;
import data.currency.ExchangeRateClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConverterTest {

    private ExchangeRateClient mokClient;
    private CurrencyConverter converter;

    @BeforeEach
    void setUp() {
        mokClient = mock(ExchangeRateClient.class);
        converter = new CurrencyConverter(mokClient);
    }

    @Test
    @DisplayName("Should successfully return a converted amount")
    void shouldReturnConvertedAmount() {
        when(converter.convert("CZK", "KZT", new BigDecimal("1000.00"))).thenReturn(new BigDecimal("22400.00"));

        BigDecimal converted = converter.convert("CZK", "KZT", new BigDecimal("1000.00"));

        assertThat(converted).isEqualTo(new BigDecimal("22400.00"));
    }

    @Test
    @DisplayName("Should throw an exception when the base currency is null")
    void shouldThrowExceptionWhenBaseCurrencyIsNull() {
        when(converter.convert(null, "KZT", new BigDecimal("1000.00"))).thenThrow(IllegalArgumentException.class);
        assertThat(converter.convert(null, "KZT", new BigDecimal("1000.00"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw an exception when the target currency is null")
    void shouldThrowExceptionWhenTargetCurrencyIsNull() {
        when(converter.convert("CZK", null, new BigDecimal("1000.00"))).thenThrow(IllegalArgumentException.class);
        assertThat(converter.convert("CZK", null, new BigDecimal("1000.00"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw an exception when the amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        when(converter.convert("CZK", "KZT", null)).thenThrow(IllegalArgumentException.class);
        assertThat(converter.convert("CZK", "KZT", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw an exception when the amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        when(converter.convert("CZK", "KZT", new BigDecimal("-1000.00"))).thenThrow(IllegalArgumentException.class);
        assertThat(converter.convert("CZK", "KZT", new BigDecimal("-1000.00"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw an exception when the amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        when(converter.convert("CZK", "KZT", new BigDecimal("0.00"))).thenThrow(IllegalArgumentException.class);
        assertThat(converter.convert("CZK", "KZT", new BigDecimal("0.00"))).isInstanceOf(IllegalArgumentException.class);
    }
}

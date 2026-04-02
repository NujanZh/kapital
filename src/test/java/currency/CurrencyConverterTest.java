package currency;

import data.currency.CurrencyConverter;
import data.currency.ExchangeRateClient;
import data.currency.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConverterTest {

    private ExchangeRateClient mockClient;
    private CurrencyConverter converter;

    @BeforeEach
    void setUp() {
        mockClient = mock(ExchangeRateClient.class);
        converter = new CurrencyConverter(mockClient);
    }

    @Test
    @DisplayName("Should successfully return a converted amount")
    void shouldReturnConvertedAmount() {
        ExchangeRateResponse fakeResponse = new ExchangeRateResponse();
        fakeResponse.setRate(new BigDecimal("22.400"));

        when(mockClient.getCurrencyRate("CZK", "KZT")).thenReturn(fakeResponse);

        BigDecimal result = converter.convert("CZK", "KZT", new BigDecimal("1000.00"));
        assertThat(result).isEqualByComparingTo("22400.00");
    }

    @Test
    @DisplayName("Should throw an exception when the base currency is null")
    void shouldThrowExceptionWhenBaseCurrencyIsNull() {
        BigDecimal amount = new BigDecimal("1000.00");

        assertThatThrownBy(() -> converter.convert(null, "KZT", amount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should had message when base currency is null")
    void shouldHadMessageWhenBaseCurrencyIsNull() {
        BigDecimal amount = new BigDecimal("1000.00");

        assertThatThrownBy(() -> converter.convert(null, "KZT", amount))
                .hasMessageContaining("Base currency cannot be empty");
    }

    @Test
    @DisplayName("Should throw an exception when the target currency is null")
    void shouldThrowExceptionWhenTargetCurrencyIsNull() {
        BigDecimal amount = new BigDecimal("1000.00");

        assertThatThrownBy(() -> converter.convert("CZK", null, amount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should had message when target currency is null")
    void shouldHadMessageWhenTargetCurrencyIsNull() {
        BigDecimal amount = new BigDecimal("1000.00");

        assertThatThrownBy(() -> converter.convert("CZK", null, amount))
                .hasMessageContaining("Quote currency cannot be empty");
    }

    @Test
    @DisplayName("Should throw an exception when the amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThatThrownBy(() -> converter.convert("CZK", "KZT", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should had message when amount is null")
    void shouldHadMessageWhenAmountIsNull() {
        assertThatThrownBy(() -> converter.convert("CZK", "KZT", null))
                .hasMessageContaining("Amount must be positive");
    }

    @Test
    @DisplayName("Should throw an exception when the amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        BigDecimal amount = new BigDecimal("-1000.00");

        assertThatThrownBy(() -> converter.convert("CZK", "KZT", amount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw an exception when the amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        BigDecimal amount = new BigDecimal(0);

        assertThatThrownBy(() -> converter.convert("CZK", "KZT", amount))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

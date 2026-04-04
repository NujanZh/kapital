package currency;

import data.currency.ExchangeRateClient;
import data.currency.ExchangeRateResponse;
import exception.CurrencyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateClientTest {

    private ExchangeRateClient client;

    @BeforeEach
    void setUp() {
        client = new ExchangeRateClient();
    }

    @Test
    @DisplayName("Should successfully return ExchangeRateResponse with not null rate")
    void shouldReturnExchangeRateResponse() {
        ExchangeRateResponse response = client.getCurrencyRate("CZK", "KZT");

        assertThat(response.getBase())
                .as("The base should be CZK")
                .isEqualTo("CZK");

        assertThat(response.getQuote())
                .as("The quote should be KZT")
                .isEqualTo("KZT");

        assertThat(response.getRate())
                .as("The rate should not be null")
                .isNotNull()
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should throw CurrencyNotFoundException for unrecognized currency code")
    void shouldThrowExceptionWhenPairNotFound() {
        assertThatThrownBy(() -> client.getCurrencyRate("ABC", "USD"))
                .as("The client should throw an exception when pair not found")
                .isInstanceOf(CurrencyNotFoundException.class);
    }

}

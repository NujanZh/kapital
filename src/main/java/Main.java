import data.currency.CurrencyConverter;
import data.currency.ExchangeRateClient;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        var er = new ExchangeRateClient();
        var cl = new CurrencyConverter(er);
        var currencyRate = cl.convert("USD", "CZK", new BigDecimal("100"));
        System.out.println(currencyRate);
    }
}

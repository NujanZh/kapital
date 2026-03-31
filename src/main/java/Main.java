import data.currency.ExchangeRateClient;

public class Main {
    public static void main(String[] args) {
        var cl = new ExchangeRateClient();
        var currencyRate = cl.getCurrencyRate("USD", "KZT");
        System.out.println(currencyRate);
    }
}

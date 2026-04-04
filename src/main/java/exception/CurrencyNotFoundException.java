package exception;

public class CurrencyNotFoundException extends ExchangeRateException{
    public CurrencyNotFoundException(String message) {
        super(message);
    }
}

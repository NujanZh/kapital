package exception;

public class ExchangeRateServerException extends ExchangeRateException {
    public ExchangeRateServerException(String message) {
        super(message);
    }
    public ExchangeRateServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

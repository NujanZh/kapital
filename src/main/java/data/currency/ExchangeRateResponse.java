package data.currency;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class ExchangeRateResponse {

    @JsonProperty("date")
    private String date;

    @JsonProperty("base")
    private String base;

    @JsonProperty("quote")
    private String quote;

    @JsonProperty("rate")
    private BigDecimal rate;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "ExchangeRateResponse{" +
                "date='" + date + '\'' +
                ", base='" + base + '\'' +
                ", quote='" + quote + '\'' +
                ", rate=" + rate +
                '}';
    }
}

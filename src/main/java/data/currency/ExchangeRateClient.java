package data.currency;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExchangeRateClient {

    private static final String BASE_URL = "https://api.frankfurter.dev/v2/rate/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ExchangeRateClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public ExchangeRateResponse getCurrencyRate(String base, String quote) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + base + "/" + quote))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return objectMapper.readValue(response.body(), ExchangeRateResponse.class);
            }

            if (statusCode == 404) {
                throw new RuntimeException("Currency pair not found: " + base + " to " + quote);
            } else if (statusCode == 400 && statusCode < 500) {
                throw new RuntimeException("Invalid client request (" + statusCode + "): " + response.body());
            } else if (statusCode >= 500) {
                throw new RuntimeException("Downstream exchange rate API is currently unavailable (" + statusCode + ")");
            } else {
                throw new RuntimeException("Unexpected response status from exchange rate API: " + statusCode);
            }

        } catch (IOException e) {
            throw new RuntimeException("Network error communicating with exchange rate API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while fetching currency rate", e);
        }
    }
}

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

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), ExchangeRateResponse.class);
            } else {
                throw new RuntimeException("Failed to get currency rate");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to get currency rate", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to get currency rate", e);
        }
    }
}

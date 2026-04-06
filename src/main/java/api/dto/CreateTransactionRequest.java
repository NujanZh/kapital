package api.dto;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        int categoryId,
        BigDecimal amount,
        String description,
        String currency
) {
}

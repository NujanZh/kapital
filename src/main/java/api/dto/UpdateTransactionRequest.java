package api.dto;

import java.math.BigDecimal;

public record UpdateTransactionRequest(
        int categoryId,
        String description,
        BigDecimal amount,
        String currency
) {
}

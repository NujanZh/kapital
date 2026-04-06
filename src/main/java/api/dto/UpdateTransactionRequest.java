package api.dto;

import java.math.BigDecimal;

public record UpdateTransactionRequest(
        int transactionId,
        int categoryId,
        String description,
        BigDecimal amount
) {
}

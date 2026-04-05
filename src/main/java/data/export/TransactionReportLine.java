package data.export;

import data.entity.Transaction;

import java.math.BigDecimal;

public record TransactionReportLine (
        Transaction transaction,
        BigDecimal amountInKzt
) {
}

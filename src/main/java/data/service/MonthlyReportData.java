package data.service;

import data.export.TransactionReportLine;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.Map;

public record MonthlyReportData(
        int year,
        Month month,
        Map<String, List<TransactionReportLine>> transactionsByCategory,
        Map<String, BigDecimal> totalsByCategory,
        BigDecimal grandTotal
) {
}

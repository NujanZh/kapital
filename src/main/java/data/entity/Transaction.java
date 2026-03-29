package data.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    int id;
    Category category;
    BigDecimal amount;
    String description;
    LocalDateTime transactionDate;

    public Transaction(Category category, BigDecimal amount, String description) {
        if (category == null) throw new IllegalArgumentException("Category must be valid");
        if (amount == null) throw new IllegalArgumentException("Amount must be positive");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("Description cannot be empty");

        this.category= category;
        this.amount = amount;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}

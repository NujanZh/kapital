package data.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final int id;
    private Category category;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;

    private Transaction(int id, Category category, BigDecimal amount, String description) {
        validate(category, amount, description);
        this.id = id;
        this.category= category;
        this.amount = amount;
        this.description = description;
    }

    public static Transaction createNew(Category category, BigDecimal amount, String description) {
        return new Transaction(0, category, amount, description);
    }

    public static Transaction fromDatabase(int id, Category category, BigDecimal amount, String description) {
        if (id <= 0) throw new IllegalArgumentException("Database transaction must have a positive ID");
        return new Transaction(id, category, amount, description);
    }

    private static void validate(Category category, BigDecimal amount, String description) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        if (category.getId() <= 0) throw new IllegalArgumentException("Category must be persisted before use");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("Description cannot be empty");
    }

    public int getId() {
        return id;
    }

    public boolean isPersisted() {
        return id > 0;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        if (category.getId() <= 0) throw new IllegalArgumentException("Category must be persisted before use");
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("Description cannot be empty");
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}

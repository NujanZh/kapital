package data.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private final int id;
    private Category category;
    private BigDecimal amount;
    private String description;
    private final String currency;
    private LocalDateTime transactionDate;

    private Transaction(int id, Category category, BigDecimal amount, String description, String currency) {
        validate(category, amount, description, currency);
        this.id = id;
        this.category= category;
        this.amount = amount;
        this.description = description;
        this.currency = currency;
    }

    public static Transaction createNew(Category category, BigDecimal amount, String description, String currency) {
        return new Transaction(0, category, amount, description, currency);
    }

    public static Transaction fromDatabase(int id, Category category, BigDecimal amount, String description, String currency) {
        if (id <= 0) throw new IllegalArgumentException("Database transaction must have a positive ID");
        return new Transaction(id, category, amount, description, currency);
    }

    private static void validate(Category category, BigDecimal amount, String description, String currency) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        if (category.getId() <= 0) throw new IllegalArgumentException("Category must be persisted before use");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("Description cannot be empty");
        if (currency == null || currency.trim().isEmpty()) throw new IllegalArgumentException("Currency cannot be empty");
        if (currency.length() != 3) throw new IllegalArgumentException("Currency must be 3 characters long");
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

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id && Objects.equals(category, that.category) && Objects.equals(amount, that.amount) && Objects.equals(description, that.description) && Objects.equals(transactionDate, that.transactionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, amount, description, transactionDate);
    }
}

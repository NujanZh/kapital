package data.repository;

import data.entity.Transaction;

import java.util.List;

public interface TransactionRepository {
    void save(Transaction transaction);
    List<Transaction> findAll();
    boolean update(Transaction transaction);
    void delete(int id);
}

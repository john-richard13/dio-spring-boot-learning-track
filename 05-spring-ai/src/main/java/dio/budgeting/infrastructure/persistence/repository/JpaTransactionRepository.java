package dio.budgeting.infrastructure.persistence.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionEntityRepository transactionEntityRepository;

    public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository) {
        this.transactionEntityRepository = transactionEntityRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = TransactionEntity.from(transaction);
        TransactionEntity savedEntity = transactionEntityRepository.save(entity);

        return savedEntity.toDomain();
    }

    @Override
    public List<Transaction> findAllByCategory(Category category) {
        List<Transaction> transactions = new ArrayList<>();

        for (TransactionEntity entity : transactionEntityRepository.findAllByCategory(category)) {
            transactions.add(Objects.requireNonNull(entity).toDomain());
        }

        return transactions;
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();

        for (TransactionEntity entity : transactionEntityRepository.findAll()) {
            transactions.add(Objects.requireNonNull(entity).toDomain());
        }

        return transactions;
    }
}
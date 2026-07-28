package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionEntityRepository transactionEntityRepository;

    public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository) {
        this.transactionEntityRepository = transactionEntityRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        var entity = TransactionEntity.from(transaction);
        return transactionEntityRepository.save(entity).toDomain();
    }

    @Override
    public List<Transaction> findAllByCategory(Category category) {
        return transactionEntityRepository.findAllByCategory(category)
                .stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }

    /**
     * Retorna todas as transações persistidas.
     *
     * <p><strong>Nota didática:</strong> a conversão de entidades para domínio
     * é feita com um {@code for} explícito e uma {@code ArrayList} ao invés de
     * {@code Stream#map(...).toList()}. Essa escolha é proposital — o objetivo
     * é manter o código o mais linear possível para quem está acompanhando o
     * raciocínio passo a passo, sem abstrações intermediárias do Stream API.
     * Em código de produção, a abordagem com streams seria perfeitamente válida.
     */
    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();

        for (TransactionEntity entity : transactionEntityRepository.findAll()) {
            transactions.add(entity.toDomain());
        }

        return transactions;
    }
}
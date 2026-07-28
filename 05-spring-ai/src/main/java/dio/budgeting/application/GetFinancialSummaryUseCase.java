package dio.budgeting.application;

import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetFinancialSummaryUseCase {
    private final TransactionRepository transactionRepository;

    public GetFinancialSummaryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "get-financial-summary",
            description = "Consulta o resumo financeiro: valor total gasto e o total gasto em cada categoria")
    public FinancialSummaryOutput execute() {
        List<Transaction> transactions = transactionRepository.findAll();

        // Inicializa o total de cada categoria em zero, mesmo sem transações ainda.
        Map<Category, Long> totalByCategoryInCents = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            totalByCategoryInCents.put(category, 0L);
        }

        long totalInCents = 0L;

        for (Transaction transaction : transactions) {
            long amount = transaction.getAmount();
            Category category = transaction.getCategory();

            totalInCents += amount;

            long currentCategoryTotal = totalByCategoryInCents.get(category);
            totalByCategoryInCents.put(category, currentCategoryTotal + amount);
        }

        Map<String, Double> totalByCategory = new LinkedHashMap<>();
        for (Map.Entry<Category, Long> entry : totalByCategoryInCents.entrySet()) {
            totalByCategory.put(entry.getKey().name(), toReais(entry.getValue()));
        }

        return new FinancialSummaryOutput(toReais(totalInCents), totalByCategory);
    }

    /**
     * Converte um valor em centavos para reais (ex: 5000 centavos -> 50.00).
     */
    private double toReais(long amountInCents) {
        return BigDecimal.valueOf(amountInCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}


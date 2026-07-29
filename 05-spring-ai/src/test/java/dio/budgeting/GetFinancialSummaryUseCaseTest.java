package dio.budgeting;

import dio.budgeting.application.GetFinancialSummaryUseCase;
import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFinancialSummaryUseCaseTest {

    @Mock
    TransactionRepository transactionRepository;

    @Test
    void should_sumTotalAndTotalByCategory_when_thereAreTransactions() {
        List<Transaction> transactions = List.of(
                new Transaction("Supermercado", 5000L, Category.GROCERIES),
                new Transaction("Farmácia", 2000L, Category.PHARMA),
                new Transaction("Mercado do bairro", 1500L, Category.GROCERIES)
        );
        when(transactionRepository.findAll()).thenReturn(transactions);

        var useCase = new GetFinancialSummaryUseCase(transactionRepository);
        FinancialSummaryOutput summary = useCase.execute();

        assertThat(summary.total()).isCloseTo(85.00, within(0.01));
        assertThat(summary.totalByCategory().get("GROCERIES")).isCloseTo(65.00, within(0.01));
        assertThat(summary.totalByCategory().get("PHARMA")).isCloseTo(20.00, within(0.01));
        assertThat(summary.totalByCategory().get("AUTO")).isCloseTo(0.00, within(0.01));
    }

    @Test
    void should_returnZeroedSummary_when_thereAreNoTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of());

        var useCase = new GetFinancialSummaryUseCase(transactionRepository);
        FinancialSummaryOutput summary = useCase.execute();

        assertThat(summary.total()).isCloseTo(0.00, within(0.01));
        assertThat(summary.totalByCategory())
                .hasSize(3)
                .containsEntry("GROCERIES", 0.0)
                .containsEntry("PHARMA", 0.0)
                .containsEntry("AUTO", 0.0);
    }
}
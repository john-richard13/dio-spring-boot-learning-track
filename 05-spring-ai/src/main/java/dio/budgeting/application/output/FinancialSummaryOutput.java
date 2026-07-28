package dio.budgeting.application.output;

import java.util.Map;

public record FinancialSummaryOutput(double total, Map<String, Double> totalByCategory) {
}


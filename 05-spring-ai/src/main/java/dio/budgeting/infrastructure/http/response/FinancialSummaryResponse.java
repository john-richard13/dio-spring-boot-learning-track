package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.FinancialSummaryOutput;

import java.util.Map;

public record FinancialSummaryResponse(double total, Map<String, Double> totalByCategory) {
    public static FinancialSummaryResponse from(FinancialSummaryOutput output) {
        return new FinancialSummaryResponse(output.total(), output.totalByCategory());
    }
}


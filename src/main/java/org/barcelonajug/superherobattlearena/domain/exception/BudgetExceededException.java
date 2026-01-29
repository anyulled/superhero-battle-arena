package org.barcelonajug.superherobattlearena.domain.exception;

/**
 * Exception thrown when team cost exceeds the budget cap.
 */
public class BudgetExceededException extends ValidationException {

    public BudgetExceededException(int totalCost, int budgetCap) {
        super("Team cost exceeds maximum: " + totalCost + " > " + budgetCap);
    }
}

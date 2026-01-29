package org.barcelonajug.superherobattlearena.application.usecase.validation;

import java.util.List;

import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.exception.BudgetExceededException;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Component;

/**
 * Validates that the team cost does not exceed the budget cap.
 */
@Component
public class CostValidationRule implements ValidationRule {

    @Override
    public void validate(List<Hero> heroes, RoundSpec roundSpec) {
        int totalCost = heroes.stream().mapToInt(Hero::cost).sum();
        if (totalCost > roundSpec.budgetCap()) {
            throw new BudgetExceededException(totalCost, roundSpec.budgetCap());
        }
    }
}

package org.barcelonajug.superherobattlearena.application.usecase.validation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.exception.RoleCompositionException;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Component;

/**
 * Validates role composition requirements and limits.
 */
@Component
public class RoleCompositionValidationRule implements ValidationRule {

    @Override
    public void validate(List<Hero> heroes, RoundSpec roundSpec) {
        Map<String, Long> roleCounts = heroes.stream()
                .collect(Collectors.groupingBy(Hero::role, Collectors.counting()));

        validateRequiredRoles(roleCounts, roundSpec);
        validateMaxSameRole(roleCounts, roundSpec);
    }

    private void validateRequiredRoles(Map<String, Long> roleCounts, RoundSpec roundSpec) {
        if (roundSpec.requiredRoles() != null) {
            roundSpec.requiredRoles().entrySet().stream()
                    .filter(entry -> roleCounts.getOrDefault(entry.getKey(), 0L) < entry.getValue())
                    .findFirst()
                    .ifPresent(entry -> {
                        throw RoleCompositionException.missingRequiredRole(entry.getKey(), entry.getValue());
                    });
        }
    }

    private void validateMaxSameRole(Map<String, Long> roleCounts, RoundSpec roundSpec) {
        if (roundSpec.maxSameRole() != null) {
            roundSpec.maxSameRole().entrySet().stream()
                    .filter(entry -> roleCounts.getOrDefault(entry.getKey(), 0L) > entry.getValue())
                    .findFirst()
                    .ifPresent(entry -> {
                        throw RoleCompositionException.tooManyOfRole(entry.getKey(), entry.getValue());
                    });
        }
    }
}

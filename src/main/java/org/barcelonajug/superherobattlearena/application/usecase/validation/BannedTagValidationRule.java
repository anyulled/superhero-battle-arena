package org.barcelonajug.superherobattlearena.application.usecase.validation;

import java.util.List;
import java.util.Map;

import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.exception.BannedTagException;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Component;

/**
 * Validates that heroes do not have banned tags.
 */
@Component
public class BannedTagValidationRule implements ValidationRule {

    @Override
    public void validate(List<Hero> heroes, RoundSpec roundSpec) {
        if (roundSpec.bannedTags() != null) {
            heroes.stream()
                    .flatMap(hero -> hero.tags().stream().map(tag -> Map.entry(hero, tag)))
                    .filter(entry -> roundSpec.bannedTags().contains(entry.getValue()))
                    .findFirst()
                    .ifPresent(entry -> {
                        throw new BannedTagException(entry.getKey().name(), entry.getValue());
                    });
        }
    }
}

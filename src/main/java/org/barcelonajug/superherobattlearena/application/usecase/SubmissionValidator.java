package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.barcelonajug.superherobattlearena.application.usecase.validation.ValidationRule;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.exception.DuplicateHeroException;
import org.barcelonajug.superherobattlearena.domain.exception.HeroNotFoundException;
import org.barcelonajug.superherobattlearena.domain.exception.TeamSizeException;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Component;

@Component
public class SubmissionValidator {

    private final RosterService rosterService;
    private final List<ValidationRule> validationRules;

    public SubmissionValidator(RosterService rosterService, List<ValidationRule> validationRules) {
        this.rosterService = rosterService;
        this.validationRules = validationRules;
    }

    public void validate(DraftSubmission submission, RoundSpec roundSpec) {
        List<Integer> heroIds = submission.heroIds();

        validateTeamSize(heroIds, roundSpec);
        validateDuplicates(heroIds);

        List<Hero> heroes = resolveHeroes(heroIds);

        // Apply all validation rules using strategy pattern
        validationRules.forEach(rule -> rule.validate(heroes, roundSpec));
    }

    private void validateTeamSize(List<Integer> heroIds, RoundSpec roundSpec) {
        if (heroIds.size() != roundSpec.teamSize()) {
            throw new TeamSizeException(roundSpec.teamSize(), heroIds.size());
        }
    }

    private void validateDuplicates(List<Integer> heroIds) {
        Set<Integer> uniqueIds = new HashSet<>(heroIds);
        if (uniqueIds.size() != heroIds.size()) {
            throw new DuplicateHeroException();
        }
    }

    private List<Hero> resolveHeroes(List<Integer> heroIds) {
        List<Hero> heroes = rosterService.getHeroes(heroIds);
        if (heroes.size() != heroIds.size()) {
            Set<Integer> foundIds = heroes.stream().map(Hero::id).collect(Collectors.toSet());
            for (Integer id : heroIds) {
                if (!foundIds.contains(id)) {
                    throw new HeroNotFoundException(id);
                }
            }
        }
        return heroes;
    }
}

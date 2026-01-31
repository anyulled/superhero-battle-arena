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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SubmissionValidatorUseCase {

  private static final Logger log = LoggerFactory.getLogger(SubmissionValidatorUseCase.class);

  private final RosterUseCase rosterUseCase;
  private final List<ValidationRule> validationRules;

  public SubmissionValidatorUseCase(
      RosterUseCase rosterUseCase, List<ValidationRule> validationRules) {
    this.rosterUseCase = rosterUseCase;
    this.validationRules = validationRules;
  }

  public void validate(DraftSubmission submission, RoundSpec roundSpec) {
    log.debug(
        "Validating submission - heroIds={}, roundSpec={}",
        submission.heroIds(),
        roundSpec.description());

    try {
      List<Integer> heroIds = submission.heroIds();

      validateTeamSize(heroIds, roundSpec);
      validateDuplicates(heroIds);

      List<Hero> heroes = resolveHeroes(heroIds);

      // Apply all validation rules using strategy pattern
      validationRules.forEach(rule -> rule.validate(heroes, roundSpec));

      log.info("Submission validation successful - {} heroes validated", heroIds.size());
    } catch (Exception e) {
      log.error("Submission validation failed - heroIds={}", submission.heroIds(), e);
      throw e;
    }
  }

  private void validateTeamSize(List<Integer> heroIds, RoundSpec roundSpec) {
    if (heroIds.size() != roundSpec.teamSize()) {
      log.warn(
          "Team size validation failed - expected={}, actual={}",
          roundSpec.teamSize(),
          heroIds.size());
      throw new TeamSizeException(roundSpec.teamSize(), heroIds.size());
    }
  }

  private void validateDuplicates(List<Integer> heroIds) {
    Set<Integer> uniqueIds = new HashSet<>(heroIds);
    if (uniqueIds.size() != heroIds.size()) {
      log.warn("Duplicate heroes detected in submission - heroIds={}", heroIds);
      throw new DuplicateHeroException();
    }
  }

  private List<Hero> resolveHeroes(List<Integer> heroIds) {
    List<Hero> heroes = rosterUseCase.getHeroes(heroIds);
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

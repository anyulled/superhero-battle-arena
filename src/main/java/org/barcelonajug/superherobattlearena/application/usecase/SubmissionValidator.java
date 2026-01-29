package org.barcelonajug.superherobattlearena.application.usecase;

import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.domain.Hero;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SubmissionValidator {

    private final RosterService rosterService;

    public SubmissionValidator(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    public void validate(DraftSubmission submission, RoundSpec roundSpec) {
        List<Integer> heroIds = submission.heroIds();

        validateTeamSize(heroIds, roundSpec);
        validateDuplicates(heroIds);

        List<Hero> heroes = resolveHeroes(heroIds);

        validateCost(heroes, roundSpec);
        validateBannedTags(heroes, roundSpec);
        validateRoleComposition(heroes, roundSpec);
    }

    private void validateTeamSize(List<Integer> heroIds, RoundSpec roundSpec) {
        if (heroIds.size() != roundSpec.teamSize()) {
            throw new IllegalArgumentException(
                    "Team size must be " + roundSpec.teamSize() + " (was " + heroIds.size() + ")");
        }
    }

    private void validateDuplicates(List<Integer> heroIds) {
        Set<Integer> uniqueIds = new HashSet<>(heroIds);
        if (uniqueIds.size() != heroIds.size()) {
            throw new IllegalArgumentException("Duplicate heroes are not allowed");
        }
    }

    private List<Hero> resolveHeroes(List<Integer> heroIds) {
        List<Hero> heroes = rosterService.getHeroes(heroIds);
        if (heroes.size() != heroIds.size()) {
            Set<Integer> foundIds = heroes.stream().map(Hero::id).collect(Collectors.toSet());
            for (Integer id : heroIds) {
                if (!foundIds.contains(id)) {
                    throw new IllegalArgumentException("Hero not found: " + id);
                }
            }
        }
        return heroes;
    }

    private void validateCost(List<Hero> heroes, RoundSpec roundSpec) {
        int totalCost = heroes.stream().mapToInt(Hero::cost).sum();
        if (totalCost > roundSpec.budgetCap()) {
            throw new IllegalArgumentException(
                    "Team cost exceeds maximum: " + totalCost + " > " + roundSpec.budgetCap());
        }
    }

    private void validateBannedTags(List<Hero> heroes, RoundSpec roundSpec) {
        if (roundSpec.bannedTags() != null) {
            heroes.stream()
                    .flatMap(hero -> hero.tags().stream().map(tag -> Map.entry(hero, tag)))
                    .filter(entry -> roundSpec.bannedTags().contains(entry.getValue()))
                    .findFirst()
                    .ifPresent(entry -> {
                        throw new IllegalArgumentException(
                                "Hero " + entry.getKey().name() + " has banned tag: " + entry.getValue());
                    });
        }
    }

    private void validateRoleComposition(List<Hero> heroes, RoundSpec roundSpec) {
        Map<String, Integer> roleCounts = new HashMap<>();
        for (Hero hero : heroes) {
            roleCounts.put(hero.role(), roleCounts.getOrDefault(hero.role(), 0) + 1);
        }

        validateRequiredRoles(roleCounts, roundSpec);
        validateMaxSameRole(roleCounts, roundSpec);
    }

    private void validateRequiredRoles(Map<String, Integer> roleCounts, RoundSpec roundSpec) {
        if (roundSpec.requiredRoles() != null) {
            roundSpec.requiredRoles().entrySet().stream()
                    .filter(entry -> roleCounts.getOrDefault(entry.getKey(), 0) < entry.getValue())
                    .findFirst()
                    .ifPresent(entry -> {
                        throw new IllegalArgumentException(
                                "Missing required role: " + entry.getKey() + " (required " + entry.getValue() + ")");
                    });
        }
    }

    private void validateMaxSameRole(Map<String, Integer> roleCounts, RoundSpec roundSpec) {
        if (roundSpec.maxSameRole() != null) {
            roundSpec.maxSameRole().entrySet().stream()
                    .filter(entry -> roleCounts.getOrDefault(entry.getKey(), 0) > entry.getValue())
                    .findFirst()
                    .ifPresent(entry -> {
                        throw new IllegalArgumentException(
                                "Too many of role: " + entry.getKey() + " (max " + entry.getValue() + ")");
                    });
        }
    }
}

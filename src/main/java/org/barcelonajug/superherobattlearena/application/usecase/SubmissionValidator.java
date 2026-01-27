package org.barcelonajug.superherobattlearena.application.usecase;

import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.domain.Hero;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import org.springframework.stereotype.Component;

@Component
public class SubmissionValidator {

    private final RosterService rosterService;

    public SubmissionValidator(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    public void validate(DraftSubmission submission, RoundSpec roundSpec) {
        List<Integer> heroIds = submission.heroIds();

        // 1. Check Team Size
        if (heroIds.size() != roundSpec.teamSize()) {
            throw new IllegalArgumentException(
                    "Team size must be " + roundSpec.teamSize() + " (was " + heroIds.size() + ")");
        }

        // 2. Check Duplicates
        Set<Integer> uniqueIds = new HashSet<>(heroIds);
        if (uniqueIds.size() != heroIds.size()) {
            throw new IllegalArgumentException("Duplicate heroes are not allowed");
        }

        int totalCost = 0;
        Map<String, Integer> roleCounts = new HashMap<>();

        for (Integer heroId : heroIds) {
            Optional<Hero> heroOpt = rosterService.getHero(heroId);
            if (heroOpt.isEmpty()) {
                throw new IllegalArgumentException("Hero not found: " + heroId);
            }
            Hero hero = heroOpt.get();

            // 3. Check Cost
            totalCost += hero.cost();

            // Count Roles
            roleCounts.put(hero.role(), roleCounts.getOrDefault(hero.role(), 0) + 1);

            // 4. Check Banned Tags
            if (roundSpec.bannedTags() != null) {
                for (String tag : hero.tags()) {
                    if (roundSpec.bannedTags().contains(tag)) {
                        throw new IllegalArgumentException("Hero " + hero.name() + " has banned tag: " + tag);
                    }
                }
            }
        }

        if (totalCost > roundSpec.budgetCap()) {
            throw new IllegalArgumentException(
                    "Team cost exceeds maximum: " + totalCost + " > " + roundSpec.budgetCap());
        }

        // 5. Check Required Roles
        if (roundSpec.requiredRoles() != null) {
            for (Map.Entry<String, Integer> entry : roundSpec.requiredRoles().entrySet()) {
                String role = entry.getKey();
                int minCount = entry.getValue();
                if (roleCounts.getOrDefault(role, 0) < minCount) {
                    throw new IllegalArgumentException(
                            "Missing required role: " + role + " (required " + minCount + ")");
                }
            }
        }

        // 6. Check Max Same Role (if applicable, though test doesn't explicitly fail on
        // this yet, good to have)
        if (roundSpec.maxSameRole() != null) {
            for (Map.Entry<String, Integer> entry : roundSpec.maxSameRole().entrySet()) {
                String role = entry.getKey();
                int maxCount = entry.getValue();
                if (roleCounts.getOrDefault(role, 0) > maxCount) {
                    throw new IllegalArgumentException("Too many of role: " + role + " (max " + maxCount + ")");
                }
            }
        }
    }
}

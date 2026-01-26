package org.barcelonajug.superherobattlearena.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Service;

@Service
public class SubmissionValidator {

    private final RosterService rosterService;

    public SubmissionValidator(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    public void validate(DraftSubmission submission, RoundSpec roundSpec) {
        List<Integer> heroIds = submission.heroIds();

        // Check Team Size
        if (heroIds.size() != roundSpec.teamSize()) {
            throw new IllegalArgumentException(
                    "Team size must be " + roundSpec.teamSize() + ", but was " + heroIds.size());
        }

        // Check Duplicates
        Set<Integer> uniqueIds = new HashSet<>(heroIds);
        if (uniqueIds.size() != heroIds.size()) {
            throw new IllegalArgumentException("Duplicate heroes are not allowed");
        }

        List<Hero> heroes = new ArrayList<>();
        int totalCost = 0;
        Map<String, Integer> roleCounts = new HashMap<>();

        for (Integer id : heroIds) {
            Hero hero = rosterService.getHero(id)
                    .orElseThrow(() -> new IllegalArgumentException("Hero with ID " + id + " not found"));
            heroes.add(hero);
            totalCost += hero.cost();
            roleCounts.merge(hero.role(), 1, Integer::sum);

            // Check Banned Tags
            if (roundSpec.bannedTags() != null) {
                for (String tag : hero.tags()) {
                    if (roundSpec.bannedTags().contains(tag)) {
                        throw new IllegalArgumentException("Hero " + hero.name() + " has banned tag: " + tag);
                    }
                }
            }
        }

        // Check Cost Cap
        if (totalCost > roundSpec.budgetCap()) {
            throw new IllegalArgumentException(
                    "Total cost " + totalCost + " exceeds budget cap of " + roundSpec.budgetCap());
        }

        // Check Required Roles
        if (roundSpec.requiredRoles() != null) {
            for (Map.Entry<String, Integer> entry : roundSpec.requiredRoles().entrySet()) {
                int count = roleCounts.getOrDefault(entry.getKey(), 0);
                if (count < entry.getValue()) {
                    throw new IllegalArgumentException("Missing required role: " + entry.getKey() + ". Required: "
                            + entry.getValue() + ", Found: " + count);
                }
            }
        }

        // Check Max Same Role
        if (roundSpec.maxSameRole() != null) {
            for (Map.Entry<String, Integer> entry : roundSpec.maxSameRole().entrySet()) {
                int count = roleCounts.getOrDefault(entry.getKey(), 0);
                if (count > entry.getValue()) {
                    throw new IllegalArgumentException("Exceeded max count for role: " + entry.getKey() + ". Max: "
                            + entry.getValue() + ", Found: " + count);
                }
            }
        }
    }
}

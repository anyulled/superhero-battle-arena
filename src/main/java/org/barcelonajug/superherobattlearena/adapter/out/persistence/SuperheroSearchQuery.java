package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroAppearanceEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroPowerStatsEntity;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class SuperheroSearchQuery {

  private static final String POWER_STATS_PATH = "powerStats";
  private static final String APPEARANCE_PATH = "appearance";
  private static final String NAME_PATH = "name";
  private static final String ALIGNMENT_PATH = "alignment";
  private static final String PUBLISHER_PATH = "publisher";
  private static final String GENDER_PATH = "gender";
  private static final String RACE_PATH = "race";
  private static final String COST_PATH = "cost";
  private static final String POWER_PATH = "power";
  private static final String STRENGTH_PATH = "strength";
  private static final String SPEED_PATH = "speed";
  private static final String INTELLIGENCE_PATH = "intelligence";
  private static final String DURABILITY_PATH = "durability";
  private static final String COMBAT_PATH = "combat";
  private static final String SORT_BY_COST = "cost";
  private static final String SORT_BY_INTELLIGENCE = "intelligence";
  private static final String SORT_BY_STRENGTH = "strength";
  private static final String SORT_BY_SPEED = "speed";
  private static final String SORT_BY_DURABILITY = "durability";
  private static final String SORT_BY_POWER = "power";
  private static final String SORT_BY_COMBAT = "combat";

  private static final Set<String> POWER_STATS_SORT_FIELDS =
      Set.of(
          SORT_BY_COST,
          SORT_BY_INTELLIGENCE,
          SORT_BY_STRENGTH,
          SORT_BY_SPEED,
          SORT_BY_DURABILITY,
          SORT_BY_POWER,
          SORT_BY_COMBAT);

  @SuppressWarnings("NullAway")
  @PersistenceContext
  private EntityManager entityManager;

  public SearchResultPage search(HeroSearchCriteria criteria) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<SuperheroEntity> query = cb.createQuery(SuperheroEntity.class);
    Root<SuperheroEntity> root = query.from(SuperheroEntity.class);
    SearchJoins joins = createJoins(root, criteria, JoinType.INNER);

    List<Predicate> predicates = buildPredicates(cb, root, joins, criteria);
    applyPredicates(query, predicates);
    query.orderBy(buildOrder(cb, root, joins, criteria));

    long totalElements = countTotalElements(cb, criteria);
    List<SuperheroEntity> entities =
        entityManager
            .createQuery(query)
            .setFirstResult(criteria.page() * criteria.size())
            .setMaxResults(criteria.size())
            .getResultList();

    int totalPages = calculateTotalPages(totalElements, criteria.size());

    return new SearchResultPage(entities, totalElements, totalPages);
  }

  private long countTotalElements(CriteriaBuilder cb, HeroSearchCriteria criteria) {
    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<SuperheroEntity> countRoot = countQuery.from(SuperheroEntity.class);
    SearchJoins joins = createJoins(countRoot, criteria, JoinType.INNER);

    List<Predicate> predicates = buildPredicates(cb, countRoot, joins, criteria);
    applyPredicates(countQuery, predicates);
    countQuery.select(cb.count(countRoot));

    return entityManager.createQuery(countQuery).getSingleResult();
  }

  private SearchJoins createJoins(
      Root<SuperheroEntity> root, HeroSearchCriteria criteria, JoinType joinType) {
    Join<SuperheroEntity, SuperheroPowerStatsEntity> powerStatsJoin = null;
    Join<SuperheroEntity, SuperheroAppearanceEntity> appearanceJoin = null;

    if (needsPowerStatsJoin(criteria)) {
      powerStatsJoin = root.join(POWER_STATS_PATH, joinType);
    }
    if (needsAppearanceJoin(criteria)) {
      appearanceJoin = root.join(APPEARANCE_PATH, joinType);
    }

    return new SearchJoins(powerStatsJoin, appearanceJoin);
  }

  private boolean needsPowerStatsJoin(HeroSearchCriteria criteria) {
    return criteria.hasCostRange() || criteria.hasPowerStatsFilter();
  }

  private boolean needsAppearanceJoin(HeroSearchCriteria criteria) {
    return criteria.hasGenderFilter() || criteria.hasRaceFilter();
  }

  private List<Predicate> buildPredicates(
      CriteriaBuilder cb,
      Root<SuperheroEntity> root,
      SearchJoins joins,
      HeroSearchCriteria criteria) {
    List<Predicate> predicates = new ArrayList<>();

    addNamePredicate(cb, root, predicates, criteria);
    addStringPredicate(
        cb,
        predicates,
        root.get(ALIGNMENT_PATH),
        criteria.alignment(),
        criteria.hasAlignmentFilter());
    addStringPredicate(
        cb,
        predicates,
        root.get(PUBLISHER_PATH),
        criteria.publisher(),
        criteria.hasPublisherFilter());
    Join<SuperheroEntity, SuperheroPowerStatsEntity> powerStatsJoin = joins.powerStatsJoin();
    if (powerStatsJoin != null) {
      addPowerStatsPredicates(cb, powerStatsJoin, predicates, criteria);
    }
    Join<SuperheroEntity, SuperheroAppearanceEntity> appearanceJoin = joins.appearanceJoin();
    if (appearanceJoin != null) {
      addAppearancePredicates(cb, appearanceJoin, predicates, criteria);
    }

    return predicates;
  }

  private void addNamePredicate(
      CriteriaBuilder cb,
      Root<SuperheroEntity> root,
      List<Predicate> predicates,
      HeroSearchCriteria criteria) {
    if (criteria.hasNameFilter()) {
      String name = Objects.requireNonNull(criteria.name());
      predicates.add(cb.like(cb.lower(root.get(NAME_PATH)), "%" + name.toLowerCase() + "%"));
    }
  }

  private void addStringPredicate(
      CriteriaBuilder cb,
      List<Predicate> predicates,
      Path<String> path,
      @Nullable String value,
      boolean include) {
    if (include) {
      predicates.add(cb.equal(path, Objects.requireNonNull(value)));
    }
  }

  private void addPowerStatsPredicates(
      CriteriaBuilder cb,
      Join<SuperheroEntity, SuperheroPowerStatsEntity> powerStatsJoin,
      List<Predicate> predicates,
      HeroSearchCriteria criteria) {
    addRangePredicate(
        cb, predicates, powerStatsJoin.get(COST_PATH), criteria.minCost(), criteria.maxCost());
    addRangePredicate(
        cb, predicates, powerStatsJoin.get(POWER_PATH), criteria.minPower(), criteria.maxPower());
    addRangePredicate(
        cb,
        predicates,
        powerStatsJoin.get(STRENGTH_PATH),
        criteria.minStrength(),
        criteria.maxStrength());
    addRangePredicate(
        cb, predicates, powerStatsJoin.get(SPEED_PATH), criteria.minSpeed(), criteria.maxSpeed());
    addRangePredicate(
        cb,
        predicates,
        powerStatsJoin.get(INTELLIGENCE_PATH),
        criteria.minIntelligence(),
        criteria.maxIntelligence());
    addRangePredicate(
        cb,
        predicates,
        powerStatsJoin.get(DURABILITY_PATH),
        criteria.minDurability(),
        criteria.maxDurability());
    addRangePredicate(
        cb,
        predicates,
        powerStatsJoin.get(COMBAT_PATH),
        criteria.minCombat(),
        criteria.maxCombat());
  }

  private void addAppearancePredicates(
      CriteriaBuilder cb,
      Join<SuperheroEntity, SuperheroAppearanceEntity> appearanceJoin,
      List<Predicate> predicates,
      HeroSearchCriteria criteria) {
    addStringPredicate(
        cb,
        predicates,
        appearanceJoin.get(GENDER_PATH),
        criteria.gender(),
        criteria.hasGenderFilter());
    addStringPredicate(
        cb, predicates, appearanceJoin.get(RACE_PATH), criteria.race(), criteria.hasRaceFilter());
  }

  private void addRangePredicate(
      CriteriaBuilder cb,
      List<Predicate> predicates,
      Path<Integer> path,
      @Nullable Integer min,
      @Nullable Integer max) {
    if (min != null) {
      predicates.add(cb.greaterThanOrEqualTo(path, Objects.requireNonNull(min)));
    }
    if (max != null) {
      predicates.add(cb.lessThanOrEqualTo(path, Objects.requireNonNull(max)));
    }
  }

  private void applyPredicates(CriteriaQuery<?> query, List<Predicate> predicates) {
    if (!predicates.isEmpty()) {
      query.where(predicates.toArray(new Predicate[0]));
    }
  }

  private Order buildOrder(
      CriteriaBuilder cb,
      Root<SuperheroEntity> root,
      SearchJoins joins,
      HeroSearchCriteria criteria) {
    Sort.Direction direction =
        criteria.sortDirection() == HeroSearchCriteria.SortDirection.DESC
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    String sortBy = Objects.requireNonNull(criteria.sortBy());

    if (POWER_STATS_SORT_FIELDS.contains(sortBy)) {
      Join<SuperheroEntity, SuperheroPowerStatsEntity> powerStatsJoin = joins.powerStatsJoin();
      if (powerStatsJoin == null) {
        powerStatsJoin = root.join(POWER_STATS_PATH, JoinType.LEFT);
      }
      return direction == Sort.Direction.ASC
          ? cb.asc(powerStatsJoin.get(sortBy))
          : cb.desc(powerStatsJoin.get(sortBy));
    }

    return direction == Sort.Direction.ASC ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy));
  }

  private int calculateTotalPages(long totalElements, int pageSize) {
    return (int) Math.ceil((double) totalElements / pageSize);
  }

  private record SearchJoins(
      @Nullable Join<SuperheroEntity, SuperheroPowerStatsEntity> powerStatsJoin,
      @Nullable Join<SuperheroEntity, SuperheroAppearanceEntity> appearanceJoin) {}

  public record SearchResultPage(
      List<SuperheroEntity> entities, long totalElements, int totalPages) {}
}

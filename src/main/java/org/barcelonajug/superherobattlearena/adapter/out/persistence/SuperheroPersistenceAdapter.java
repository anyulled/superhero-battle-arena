package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroAppearanceEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroPowerStatsEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.SuperheroMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataSuperheroRepository;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class SuperheroPersistenceAdapter implements SuperheroRepositoryPort {

  private final SpringDataSuperheroRepository repository;
  private final SuperheroMapper mapper;

  @SuppressWarnings("NullAway")
  @PersistenceContext private EntityManager entityManager;

  public SuperheroPersistenceAdapter(
      SpringDataSuperheroRepository repository, SuperheroMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public List<Hero> findAll() {
    return repository.findAll().stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public List<Hero> findAll(int page, int size) {
    return repository.findAll(PageRequest.of(page, size)).stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public Optional<Hero> findById(Integer id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Hero> findByIds(List<Integer> ids) {
    return repository.findAllById(ids).stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public List<Hero> searchByName(String term) {
    return repository.searchByName(term).stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public List<Hero> findByAlignmentAndPublisher(String alignment, String publisher) {
    if (alignment != null && publisher != null) {
      return repository.findByAlignmentAndPublisher(alignment, publisher).stream()
          .map(mapper::toDomain)
          .filter(Objects::nonNull)
          .map(Objects::requireNonNull)
          .toList();
    } else if (alignment != null) {
      return repository.findByAlignment(alignment).stream()
          .map(mapper::toDomain)
          .filter(Objects::nonNull)
          .map(Objects::requireNonNull)
          .toList();
    } else if (publisher != null) {
      return repository.findByPublisher(publisher).stream()
          .map(mapper::toDomain)
          .filter(Objects::nonNull)
          .map(Objects::requireNonNull)
          .toList();
    }
    return findAll();
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  @SuppressWarnings("NullAway")
  public HeroSearchResult search(HeroSearchCriteria criteria) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<SuperheroEntity> query = cb.createQuery(SuperheroEntity.class);
    Root<SuperheroEntity> root = query.from(SuperheroEntity.class);

    List<Predicate> predicates = new ArrayList<>();

    if (criteria.hasNameFilter()) {
      predicates.add(
          cb.like(cb.lower(root.get("name")), "%" + criteria.name().toLowerCase() + "%"));
    }

    if (criteria.hasAlignmentFilter()) {
      predicates.add(cb.equal(root.get("alignment"), criteria.alignment()));
    }

    if (criteria.hasPublisherFilter()) {
      predicates.add(cb.equal(root.get("publisher"), criteria.publisher()));
    }

    Join<SuperheroEntity, SuperheroPowerStatsEntity> powerStatsJoin = null;
    Join<SuperheroEntity, SuperheroAppearanceEntity> appearanceJoin = null;

    boolean needsPowerStatsJoin = criteria.hasCostRange() || criteria.hasPowerStatsFilter();
    boolean needsAppearanceJoin = criteria.hasGenderFilter() || criteria.hasRaceFilter();

    if (needsPowerStatsJoin) {
      powerStatsJoin = root.join("powerStats", JoinType.INNER);
    }

    if (needsAppearanceJoin) {
      appearanceJoin = root.join("appearance", JoinType.INNER);
    }

    if (criteria.minCost() != null) {
      predicates.add(cb.greaterThanOrEqualTo(powerStatsJoin.get("cost"), criteria.minCost()));
    }
    if (criteria.maxCost() != null) {
      predicates.add(cb.lessThanOrEqualTo(powerStatsJoin.get("cost"), criteria.maxCost()));
    }

    if (criteria.minPower() != null) {
      predicates.add(cb.greaterThanOrEqualTo(powerStatsJoin.get("power"), criteria.minPower()));
    }
    if (criteria.maxPower() != null) {
      predicates.add(cb.lessThanOrEqualTo(powerStatsJoin.get("power"), criteria.maxPower()));
    }

    if (criteria.minStrength() != null) {
      predicates.add(
          cb.greaterThanOrEqualTo(powerStatsJoin.get("strength"), criteria.minStrength()));
    }
    if (criteria.maxStrength() != null) {
      predicates.add(cb.lessThanOrEqualTo(powerStatsJoin.get("strength"), criteria.maxStrength()));
    }

    if (criteria.minSpeed() != null) {
      predicates.add(cb.greaterThanOrEqualTo(powerStatsJoin.get("speed"), criteria.minSpeed()));
    }
    if (criteria.maxSpeed() != null) {
      predicates.add(cb.lessThanOrEqualTo(powerStatsJoin.get("speed"), criteria.maxSpeed()));
    }

    if (criteria.minIntelligence() != null) {
      predicates.add(
          cb.greaterThanOrEqualTo(powerStatsJoin.get("intelligence"), criteria.minIntelligence()));
    }
    if (criteria.maxIntelligence() != null) {
      predicates.add(
          cb.lessThanOrEqualTo(powerStatsJoin.get("intelligence"), criteria.maxIntelligence()));
    }

    if (criteria.minDurability() != null) {
      predicates.add(
          cb.greaterThanOrEqualTo(powerStatsJoin.get("durability"), criteria.minDurability()));
    }
    if (criteria.maxDurability() != null) {
      predicates.add(
          cb.lessThanOrEqualTo(powerStatsJoin.get("durability"), criteria.maxDurability()));
    }

    if (criteria.minCombat() != null) {
      predicates.add(cb.greaterThanOrEqualTo(powerStatsJoin.get("combat"), criteria.minCombat()));
    }
    if (criteria.maxCombat() != null) {
      predicates.add(cb.lessThanOrEqualTo(powerStatsJoin.get("combat"), criteria.maxCombat()));
    }

    if (criteria.hasGenderFilter()) {
      predicates.add(cb.equal(appearanceJoin.get("gender"), criteria.gender()));
    }

    if (criteria.hasRaceFilter()) {
      predicates.add(cb.equal(appearanceJoin.get("race"), criteria.race()));
    }

    if (!predicates.isEmpty()) {
      query.where(predicates.toArray(new Predicate[0]));
    }

    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<SuperheroEntity> countRoot = countQuery.from(SuperheroEntity.class);
    Join<SuperheroEntity, SuperheroPowerStatsEntity> countPowerStatsJoin = null;
    Join<SuperheroEntity, SuperheroAppearanceEntity> countAppearanceJoin = null;

    if (needsPowerStatsJoin) {
      countPowerStatsJoin = countRoot.join("powerStats", JoinType.INNER);
    }
    if (needsAppearanceJoin) {
      countAppearanceJoin = countRoot.join("appearance", JoinType.INNER);
    }

    List<Predicate> countPredicates = new ArrayList<>();
    if (criteria.hasNameFilter()) {
      countPredicates.add(
          cb.like(cb.lower(countRoot.get("name")), "%" + criteria.name().toLowerCase() + "%"));
    }
    if (criteria.hasAlignmentFilter()) {
      countPredicates.add(cb.equal(countRoot.get("alignment"), criteria.alignment()));
    }
    if (criteria.hasPublisherFilter()) {
      countPredicates.add(cb.equal(countRoot.get("publisher"), criteria.publisher()));
    }
    if (criteria.minCost() != null) {
      countPredicates.add(
          cb.greaterThanOrEqualTo(countPowerStatsJoin.get("cost"), criteria.minCost()));
    }
    if (criteria.maxCost() != null) {
      countPredicates.add(
          cb.lessThanOrEqualTo(countPowerStatsJoin.get("cost"), criteria.maxCost()));
    }
    if (criteria.minPower() != null) {
      countPredicates.add(
          cb.greaterThanOrEqualTo(countPowerStatsJoin.get("power"), criteria.minPower()));
    }
    if (criteria.maxPower() != null) {
      countPredicates.add(
          cb.lessThanOrEqualTo(countPowerStatsJoin.get("power"), criteria.maxPower()));
    }
    if (criteria.minStrength() != null) {
      countPredicates.add(
          cb.greaterThanOrEqualTo(countPowerStatsJoin.get("strength"), criteria.minStrength()));
    }
    if (criteria.maxStrength() != null) {
      countPredicates.add(
          cb.lessThanOrEqualTo(countPowerStatsJoin.get("strength"), criteria.maxStrength()));
    }
    if (criteria.minSpeed() != null) {
      countPredicates.add(
          cb.greaterThanOrEqualTo(countPowerStatsJoin.get("speed"), criteria.minSpeed()));
    }
    if (criteria.maxSpeed() != null) {
      countPredicates.add(
          cb.lessThanOrEqualTo(countPowerStatsJoin.get("speed"), criteria.maxSpeed()));
    }
    if (criteria.minIntelligence() != null) {
      countPredicates.add(
          cb.greaterThanOrEqualTo(
              countPowerStatsJoin.get("intelligence"), criteria.minIntelligence()));
    }
    if (criteria.maxIntelligence() != null) {
      countPredicates.add(
          cb.lessThanOrEqualTo(
              countPowerStatsJoin.get("intelligence"), criteria.maxIntelligence()));
    }
    if (criteria.minDurability() != null) {
      countPredicates.add(
          cb.greaterThanOrEqualTo(countPowerStatsJoin.get("durability"), criteria.minDurability()));
    }
    if (criteria.maxDurability() != null) {
      countPredicates.add(
          cb.lessThanOrEqualTo(countPowerStatsJoin.get("durability"), criteria.maxDurability()));
    }
    if (criteria.minCombat() != null) {
      countPredicates.add(
          cb.greaterThanOrEqualTo(countPowerStatsJoin.get("combat"), criteria.minCombat()));
    }
    if (criteria.maxCombat() != null) {
      countPredicates.add(
          cb.lessThanOrEqualTo(countPowerStatsJoin.get("combat"), criteria.maxCombat()));
    }
    if (criteria.hasGenderFilter()) {
      countPredicates.add(cb.equal(countAppearanceJoin.get("gender"), criteria.gender()));
    }
    if (criteria.hasRaceFilter()) {
      countPredicates.add(cb.equal(countAppearanceJoin.get("race"), criteria.race()));
    }

    if (!countPredicates.isEmpty()) {
      countQuery.where(countPredicates.toArray(new Predicate[0]));
    }
    countQuery.select(cb.count(countRoot));
    long totalElements = entityManager.createQuery(countQuery).getSingleResult();

    String sortField = criteria.sortBy();
    Sort.Direction direction =
        criteria.sortDirection() == HeroSearchCriteria.SortDirection.DESC
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    Order order;
    if ("cost".equals(sortField)
        || "intelligence".equals(sortField)
        || "strength".equals(sortField)
        || "speed".equals(sortField)
        || "durability".equals(sortField)
        || "power".equals(sortField)
        || "combat".equals(sortField)) {
      if (powerStatsJoin == null) {
        powerStatsJoin = root.join("powerStats", JoinType.LEFT);
      }
      order =
          direction == Sort.Direction.ASC
              ? cb.asc(powerStatsJoin.get(sortField))
              : cb.desc(powerStatsJoin.get(sortField));
    } else {
      order =
          direction == Sort.Direction.ASC
              ? cb.asc(root.get(sortField))
              : cb.desc(root.get(sortField));
    }
    query.orderBy(order);

    List<SuperheroEntity> results =
        entityManager
            .createQuery(query)
            .setFirstResult(criteria.page() * criteria.size())
            .setMaxResults(criteria.size())
            .getResultList();

    List<Hero> heroes =
        results.stream()
            .map(mapper::toDomain)
            .filter(Objects::nonNull)
            .map(Objects::requireNonNull)
            .toList();

    int totalPages = (int) Math.ceil((double) totalElements / criteria.size());

    return new HeroSearchResult(
        heroes,
        totalElements,
        totalPages,
        criteria.page(),
        criteria.size(),
        criteria.page() < totalPages - 1,
        criteria.page() > 0);
  }
}

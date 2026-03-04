package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.barcelonajug.superherobattlearena.domain.filter.FilterCriteria;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

public class SuperheroSpecificationBuilder {

  public static Specification<SuperheroEntity> buildSpecification(
      List<FilterCriteria> criteriaList) {
    return (Root<SuperheroEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (criteriaList == null || criteriaList.isEmpty()) {
        return builder.conjunction();
      }

      for (FilterCriteria criteria : criteriaList) {
        String field = criteria.field();
        Path<?> path;

        // Handle nested fields like powerStats.strength or appearance.gender
        if (field.contains(".")) {
          List<String> parts = com.google.common.base.Splitter.on('.').splitToList(field);
          String joinEntity = parts.get(0);
          String entityField = parts.get(1);

          // Use Left Join so we don't drop heroes without related stats
          Join<Object, Object> join = root.join(joinEntity, JoinType.LEFT);
          path = join.get(entityField);
        } else {
          path = root.get(field);
        }

        Predicate predicate = buildPredicate(builder, path, criteria);
        if (predicate != null) {
          predicates.add(predicate);
        }
      }

      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static @Nullable Predicate buildPredicate(
      CriteriaBuilder builder, Path<?> path, FilterCriteria criteria) {
    String value = criteria.value();

    return switch (criteria.operator()) {
      case EQUALS -> {
        if (value == null) yield builder.isNull(path);
        if (path.getJavaType().equals(String.class)) {
          yield builder.equal(builder.lower((Path<String>) path), value.toLowerCase(Locale.ROOT));
        }
        yield builder.equal(path, parseValue(path.getJavaType(), value));
      }
      case NOT_EQUALS -> {
        if (value == null) yield builder.isNotNull(path);
        if (path.getJavaType().equals(String.class)) {
          yield builder.notEqual(
              builder.lower((Path<String>) path), value.toLowerCase(Locale.ROOT));
        }
        yield builder.notEqual(path, parseValue(path.getJavaType(), value));
      }
      case GREATER_THAN ->
          value == null
              ? null
              : builder.greaterThan(
                  (Path<Comparable>) path, (Comparable) parseValue(path.getJavaType(), value));
      case LESS_THAN ->
          value == null
              ? null
              : builder.lessThan(
                  (Path<Comparable>) path, (Comparable) parseValue(path.getJavaType(), value));
      case GREATER_THAN_OR_EQUALS ->
          value == null
              ? null
              : builder.greaterThanOrEqualTo(
                  (Path<Comparable>) path, (Comparable) parseValue(path.getJavaType(), value));
      case LESS_THAN_OR_EQUALS ->
          value == null
              ? null
              : builder.lessThanOrEqualTo(
                  (Path<Comparable>) path, (Comparable) parseValue(path.getJavaType(), value));
      case LIKE ->
          value == null
              ? null
              : builder.like(
                  builder.lower((Path<String>) path), "%" + value.toLowerCase(Locale.ROOT) + "%");
      case IN -> {
        if (criteria.values() == null || criteria.values().isEmpty()) yield null;
        CriteriaBuilder.In<Object> in = builder.in(path);
        for (String v : criteria.values()) {
          in.value(parseValue(path.getJavaType(), v));
        }
        yield in;
      }
      case BETWEEN ->
          (value == null || criteria.value2() == null)
              ? null
              : builder.between(
                  (Path<Comparable>) path,
                  (Comparable) parseValue(path.getJavaType(), value),
                  (Comparable) parseValue(path.getJavaType(), criteria.value2()));
    };
  }

  private static @Nullable Object parseValue(Class<?> type, String value) {
    if (value == null) return null;
    if (type.equals(Integer.class) || type.equals(int.class)) {
      return Integer.parseInt(value);
    } else if (type.equals(Long.class) || type.equals(long.class)) {
      return Long.parseLong(value);
    } else if (type.equals(Double.class) || type.equals(double.class)) {
      return Double.parseDouble(value);
    } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
      return Boolean.parseBoolean(value);
    }
    return value;
  }
}

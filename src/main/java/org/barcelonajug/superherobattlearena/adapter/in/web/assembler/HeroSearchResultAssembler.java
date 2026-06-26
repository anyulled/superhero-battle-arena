package org.barcelonajug.superherobattlearena.adapter.in.web.assembler;

import java.util.List;
import java.util.Objects;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class HeroSearchResultAssembler
    implements RepresentationModelAssembler<HeroSearchResult, PagedModel<EntityModel<Hero>>> {

  private final HeroModelAssembler heroModelAssembler;

  public HeroSearchResultAssembler(HeroModelAssembler heroModelAssembler) {
    this.heroModelAssembler = heroModelAssembler;
  }

  @Override
  public PagedModel<EntityModel<Hero>> toModel(HeroSearchResult searchResult) {
    return toModelWithPagination(searchResult, HeroSearchCriteria.builder().build());
  }

  public PagedModel<EntityModel<Hero>> toModelWithPagination(
      HeroSearchResult searchResult, HeroSearchCriteria criteria) {
    List<EntityModel<Hero>> heroModels =
        searchResult.heroes().stream().map(heroModelAssembler::toModel).toList();

    PagedModel.PageMetadata metadata =
        new PagedModel.PageMetadata(
            searchResult.pageSize(),
            searchResult.currentPage(),
            searchResult.totalElements(),
            searchResult.totalPages());

    PagedModel<EntityModel<Hero>> pagedModel = PagedModel.of(heroModels, metadata);

    addPaginationLinks(pagedModel, searchResult, criteria);

    return pagedModel;
  }

  private void addPaginationLinks(
      PagedModel<EntityModel<Hero>> pagedModel,
      HeroSearchResult result,
      HeroSearchCriteria criteria) {
    String baseUrl = "/api/heroes/search/advanced";

    pagedModel.add(Link.of(buildUrl(baseUrl, criteria, 0), "first"));

    if (result.hasPrevious()) {
      pagedModel.add(Link.of(buildUrl(baseUrl, criteria, result.currentPage() - 1), "prev"));
    }

    if (result.hasNext()) {
      pagedModel.add(Link.of(buildUrl(baseUrl, criteria, result.currentPage() + 1), "next"));
    }

    pagedModel.add(Link.of(buildUrl(baseUrl, criteria, result.totalPages() - 1), "last"));
    pagedModel.add(Link.of(buildUrl(baseUrl, criteria, result.currentPage()), "self"));
  }

  private String buildUrl(String baseUrl, HeroSearchCriteria criteria, int page) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromPath(baseUrl)
            .queryParam("page", page)
            .queryParam("size", criteria.size());

    addStringQueryParam(builder, "name", criteria.hasNameFilter(), criteria.name());
    addStringQueryParam(builder, "alignment", criteria.hasAlignmentFilter(), criteria.alignment());
    addStringQueryParam(builder, "publisher", criteria.hasPublisherFilter(), criteria.publisher());
    addStringQueryParam(builder, "role", criteria.hasRoleFilter(), criteria.role());
    addStringQueryParam(builder, "gender", criteria.hasGenderFilter(), criteria.gender());
    addStringQueryParam(builder, "race", criteria.hasRaceFilter(), criteria.race());
    addNullableQueryParam(builder, "minCost", criteria.minCost());
    addNullableQueryParam(builder, "maxCost", criteria.maxCost());
    addNullableQueryParam(builder, "minPower", criteria.minPower());
    addNullableQueryParam(builder, "maxPower", criteria.maxPower());
    addNullableQueryParam(builder, "minStrength", criteria.minStrength());
    addNullableQueryParam(builder, "maxStrength", criteria.maxStrength());
    addNullableQueryParam(builder, "minSpeed", criteria.minSpeed());
    addNullableQueryParam(builder, "maxSpeed", criteria.maxSpeed());
    addNullableQueryParam(builder, "minIntelligence", criteria.minIntelligence());
    addNullableQueryParam(builder, "maxIntelligence", criteria.maxIntelligence());
    addNullableQueryParam(builder, "minDurability", criteria.minDurability());
    addNullableQueryParam(builder, "maxDurability", criteria.maxDurability());
    addNullableQueryParam(builder, "minCombat", criteria.minCombat());
    addNullableQueryParam(builder, "maxCombat", criteria.maxCombat());
    addNonBlankQueryParam(builder, "sortBy", criteria.sortBy());
    addNullableQueryParam(builder, "sortDirection", criteria.sortDirection());

    return builder.build().toUriString();
  }

  private void addStringQueryParam(
      UriComponentsBuilder builder, String name, boolean include, @Nullable String value) {
    if (include) {
      builder.queryParam(name, Objects.requireNonNull(value));
    }
  }

  private void addNullableQueryParam(
      UriComponentsBuilder builder, String name, @Nullable Object value) {
    if (value != null) {
      builder.queryParam(name, value);
    }
  }

  private void addNonBlankQueryParam(
      UriComponentsBuilder builder, String name, @Nullable String value) {
    if (value != null && !value.isBlank()) {
      builder.queryParam(name, value);
    }
  }
}

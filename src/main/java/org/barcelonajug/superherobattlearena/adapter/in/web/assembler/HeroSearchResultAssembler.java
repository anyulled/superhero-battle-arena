package org.barcelonajug.superherobattlearena.adapter.in.web.assembler;

import java.util.List;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

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
    StringBuilder url = new StringBuilder(baseUrl);
    url.append("?page=").append(page);
    url.append("&size=").append(criteria.size());

    if (criteria.hasNameFilter()) {
      url.append("&name=").append(criteria.name());
    }
    if (criteria.hasAlignmentFilter()) {
      url.append("&alignment=").append(criteria.alignment());
    }
    if (criteria.hasPublisherFilter()) {
      url.append("&publisher=").append(criteria.publisher());
    }
    if (criteria.hasRoleFilter()) {
      url.append("&role=").append(criteria.role());
    }
    if (criteria.hasGenderFilter()) {
      url.append("&gender=").append(criteria.gender());
    }
    if (criteria.hasRaceFilter()) {
      url.append("&race=").append(criteria.race());
    }
    if (criteria.minCost() != null) {
      url.append("&minCost=").append(criteria.minCost());
    }
    if (criteria.maxCost() != null) {
      url.append("&maxCost=").append(criteria.maxCost());
    }
    if (criteria.minPower() != null) {
      url.append("&minPower=").append(criteria.minPower());
    }
    if (criteria.maxPower() != null) {
      url.append("&maxPower=").append(criteria.maxPower());
    }
    if (criteria.minStrength() != null) {
      url.append("&minStrength=").append(criteria.minStrength());
    }
    if (criteria.maxStrength() != null) {
      url.append("&maxStrength=").append(criteria.maxStrength());
    }
    if (criteria.minSpeed() != null) {
      url.append("&minSpeed=").append(criteria.minSpeed());
    }
    if (criteria.maxSpeed() != null) {
      url.append("&maxSpeed=").append(criteria.maxSpeed());
    }
    if (criteria.minIntelligence() != null) {
      url.append("&minIntelligence=").append(criteria.minIntelligence());
    }
    if (criteria.maxIntelligence() != null) {
      url.append("&maxIntelligence=").append(criteria.maxIntelligence());
    }
    if (criteria.minDurability() != null) {
      url.append("&minDurability=").append(criteria.minDurability());
    }
    if (criteria.maxDurability() != null) {
      url.append("&maxDurability=").append(criteria.maxDurability());
    }
    if (criteria.minCombat() != null) {
      url.append("&minCombat=").append(criteria.minCombat());
    }
    if (criteria.maxCombat() != null) {
      url.append("&maxCombat=").append(criteria.maxCombat());
    }
    if (criteria.sortBy() != null && !criteria.sortBy().isBlank()) {
      url.append("&sortBy=").append(criteria.sortBy());
    }
    if (criteria.sortDirection() != null) {
      url.append("&sortDirection=").append(criteria.sortDirection());
    }

    return url.toString();
  }
}

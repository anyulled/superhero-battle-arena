package org.barcelonajug.superherobattlearena.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.barcelonajug.superherobattlearena.adapter.in.web.assembler.HeroSearchResultAssembler;
import org.barcelonajug.superherobattlearena.application.usecase.HeroSearchUseCase;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria.SortDirection;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heroes/search")
@Tag(name = "Hero Search", description = "Advanced API for searching superheroes with filters")
public class HeroAdvancedSearchController {

  private final HeroSearchUseCase heroSearchUseCase;
  private final HeroSearchResultAssembler heroSearchResultAssembler;

  public HeroAdvancedSearchController(
      HeroSearchUseCase heroSearchUseCase, HeroSearchResultAssembler heroSearchResultAssembler) {
    this.heroSearchUseCase = heroSearchUseCase;
    this.heroSearchResultAssembler = heroSearchResultAssembler;
  }

  @GetMapping("/advanced")
  @Operation(
      summary = "Advanced hero search",
      description = "Search heroes with advanced filters, pagination, sorting, and HATEOAS links")
  @ApiResponse(responseCode = "200", description = "Search completed successfully")
  public ResponseEntity<PagedModel<EntityModel<Hero>>> advancedSearch(
      @Parameter(description = "Hero name filter (case-insensitive substring)")
          @RequestParam(required = false)
          @Nullable String name,
      @Parameter(description = "Alignment filter (good, bad, neutral)")
          @RequestParam(required = false)
          @Nullable String alignment,
      @Parameter(description = "Publisher filter") @RequestParam(required = false)
          @Nullable String publisher,
      @Parameter(description = "Role filter (Tank, Fighter, Support, Assassin)")
          @RequestParam(required = false)
          @Nullable String role,
      @Parameter(description = "Gender filter (Male, Female)") @RequestParam(required = false)
          @Nullable String gender,
      @Parameter(description = "Race/species filter") @RequestParam(required = false)
          @Nullable String race,
      @Parameter(description = "Minimum cost") @RequestParam(required = false)
          @Nullable Integer minCost,
      @Parameter(description = "Maximum cost") @RequestParam(required = false)
          @Nullable Integer maxCost,
      @Parameter(description = "Minimum power") @RequestParam(required = false)
          @Nullable Integer minPower,
      @Parameter(description = "Maximum power") @RequestParam(required = false)
          @Nullable Integer maxPower,
      @Parameter(description = "Minimum strength") @RequestParam(required = false)
          @Nullable Integer minStrength,
      @Parameter(description = "Maximum strength") @RequestParam(required = false)
          @Nullable Integer maxStrength,
      @Parameter(description = "Minimum speed") @RequestParam(required = false)
          @Nullable Integer minSpeed,
      @Parameter(description = "Maximum speed") @RequestParam(required = false)
          @Nullable Integer maxSpeed,
      @Parameter(description = "Minimum intelligence") @RequestParam(required = false)
          @Nullable Integer minIntelligence,
      @Parameter(description = "Maximum intelligence") @RequestParam(required = false)
          @Nullable Integer maxIntelligence,
      @Parameter(description = "Minimum durability") @RequestParam(required = false)
          @Nullable Integer minDurability,
      @Parameter(description = "Maximum durability") @RequestParam(required = false)
          @Nullable Integer maxDurability,
      @Parameter(description = "Minimum combat") @RequestParam(required = false)
          @Nullable Integer minCombat,
      @Parameter(description = "Maximum combat") @RequestParam(required = false)
          @Nullable Integer maxCombat,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size (1-100)", example = "20")
          @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Sort field (name, cost, power, speed)")
          @RequestParam(required = false)
          @Nullable String sortBy,
      @Parameter(description = "Sort direction (ASC, DESC)") @RequestParam(required = false)
          @Nullable SortDirection sortDirection) {

    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder()
            .name(name)
            .alignment(alignment)
            .publisher(publisher)
            .role(role)
            .gender(gender)
            .race(race)
            .minCost(minCost)
            .maxCost(maxCost)
            .minPower(minPower)
            .maxPower(maxPower)
            .minStrength(minStrength)
            .maxStrength(maxStrength)
            .minSpeed(minSpeed)
            .maxSpeed(maxSpeed)
            .minIntelligence(minIntelligence)
            .maxIntelligence(maxIntelligence)
            .minDurability(minDurability)
            .maxDurability(maxDurability)
            .minCombat(minCombat)
            .maxCombat(maxCombat)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

    HeroSearchResult searchResult = heroSearchUseCase.search(criteria);
    PagedModel<EntityModel<Hero>> response =
        heroSearchResultAssembler.toModelWithPagination(searchResult, criteria);

    return ResponseEntity.ok(response);
  }
}

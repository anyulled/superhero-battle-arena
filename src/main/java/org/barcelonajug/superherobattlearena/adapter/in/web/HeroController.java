package org.barcelonajug.superherobattlearena.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.barcelonajug.superherobattlearena.application.usecase.RosterUseCase;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heroes")
@Tag(name = "Heroes", description = "API for browsing and searching superheroes")
public class HeroController {

  private final RosterUseCase rosterUseCase;

  public HeroController(RosterUseCase rosterUseCase) {
    this.rosterUseCase = rosterUseCase;
  }

  @GetMapping
  @Operation(
      summary = "List all heroes",
      description = "Get a list of all available heroes with pagination support")
  public List<Hero> getAllHeroes(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
    return rosterUseCase.getAllHeroes(page, size);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get hero by ID",
      description = "Get detailed information about a specific hero")
  @ApiResponse(responseCode = "200", description = "Hero found")
  @ApiResponse(responseCode = "404", description = "Hero not found")
  public ResponseEntity<Hero> getHeroById(@PathVariable int id) {
    return rosterUseCase
        .getHero(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/search")
  @Operation(
      summary = "Search heroes",
      description = "Search heroes by name (case-insensitive substring match)")
  public List<Hero> searchHeroes(@RequestParam String q) {
    return rosterUseCase.searchHeroes(q);
  }

  @GetMapping("/filter")
  @Operation(summary = "Filter heroes", description = "Filter heroes by alignment and/or publisher")
  public List<Hero> filterHeroes(
      @RequestParam(required = false) String alignment,
      @RequestParam(required = false) String publisher) {
    return rosterUseCase.filterHeroes(alignment, publisher);
  }
}

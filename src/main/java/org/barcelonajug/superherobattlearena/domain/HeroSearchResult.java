package org.barcelonajug.superherobattlearena.domain;

import java.util.List;

public record HeroSearchResult(
    List<Hero> heroes,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious) {

  public HeroSearchResult {
    if (heroes == null) {
      heroes = List.of();
    }
    if (totalElements < 0) {
      totalElements = 0;
    }
    if (totalPages < 0) {
      totalPages = 0;
    }
    if (currentPage < 0) {
      currentPage = 0;
    }
    if (pageSize < 1) {
      pageSize = 20;
    }
  }

  public static HeroSearchResult empty(int page, int size) {
    return new HeroSearchResult(List.of(), 0, 0, page, size, false, false);
  }

  public boolean isEmpty() {
    return heroes.isEmpty();
  }
}

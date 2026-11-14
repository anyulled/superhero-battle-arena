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
  private static final int MIN_PAGE_SIZE = 1;
  private static final int DEFAULT_PAGE_SIZE = 20;

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
    if (pageSize < MIN_PAGE_SIZE) {
      pageSize = DEFAULT_PAGE_SIZE;
    }
  }

  public static HeroSearchResult empty(int page, int size) {
    return new HeroSearchResult(List.of(), 0, 0, page, size, false, false);
  }

  public boolean isEmpty() {
    return heroes.isEmpty();
  }
}

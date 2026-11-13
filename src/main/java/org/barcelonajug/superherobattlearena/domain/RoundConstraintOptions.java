package org.barcelonajug.superherobattlearena.domain;

import java.util.List;

public record RoundConstraintOptions(
    List<String> roles,
    List<String> genders,
    List<String> races,
    List<String> publishers,
    List<String> alignments) {}

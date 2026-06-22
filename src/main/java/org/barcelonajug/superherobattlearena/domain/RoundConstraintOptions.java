package org.barcelonajug.superherobattlearena.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Available superhero values for round constraints")
public record RoundConstraintOptions(
    List<String> roles,
    List<String> genders,
    List<String> races,
    List<String> publishers,
    List<String> alignments) {}

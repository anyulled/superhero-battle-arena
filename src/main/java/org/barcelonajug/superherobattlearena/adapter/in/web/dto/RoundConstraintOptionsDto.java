package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.barcelonajug.superherobattlearena.domain.RoundConstraintOptions;

@Schema(
    name = "RoundConstraintOptions",
    description = "Available superhero values for round constraints")
public record RoundConstraintOptionsDto(
    List<String> roles,
    List<String> genders,
    List<String> races,
    List<String> publishers,
    List<String> alignments) {
  public static RoundConstraintOptionsDto from(RoundConstraintOptions value) {
    return new RoundConstraintOptionsDto(
        value.roles(), value.genders(), value.races(), value.publishers(), value.alignments());
  }
}

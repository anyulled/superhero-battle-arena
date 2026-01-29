package org.barcelonajug.superherobattlearena.domain.json;

import java.util.List;

/** Represents a draft submission of heroes for a round. */
public record DraftSubmission(List<Integer> heroIds, String strategy) {}

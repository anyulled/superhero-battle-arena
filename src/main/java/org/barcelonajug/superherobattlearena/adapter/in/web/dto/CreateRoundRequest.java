package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

public record CreateRoundRequest(UUID sessionId, Integer roundNo, RoundSpec spec) {}

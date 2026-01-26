package com.superherobattle.superherobattlearena.domain.json;

import java.util.List;

public record DraftSubmission(
        List<Integer> heroIds,
        String strategy) {
}

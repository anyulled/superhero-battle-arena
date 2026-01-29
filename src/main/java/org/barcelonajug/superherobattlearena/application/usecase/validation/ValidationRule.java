package org.barcelonajug.superherobattlearena.application.usecase.validation;

import java.util.List;

import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

/**
 * Strategy interface for validating team submissions.
 */
public interface ValidationRule {

    /**
     * Validates the given heroes against the round specification.
     * 
     * @param heroes    the list of heroes to validate
     * @param roundSpec the round specification containing validation rules
     * @throws org.barcelonajug.superherobattlearena.domain.exception.ValidationException if
     *                                                                                    validation
     *                                                                                    fails
     */
    void validate(List<Hero> heroes, RoundSpec roundSpec);
}

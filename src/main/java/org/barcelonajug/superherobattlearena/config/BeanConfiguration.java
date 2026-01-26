package org.barcelonajug.superherobattlearena.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.application.usecase.BattleEngine;
import org.barcelonajug.superherobattlearena.application.usecase.FatigueService;
import org.barcelonajug.superherobattlearena.application.usecase.RosterService;
import org.barcelonajug.superherobattlearena.application.usecase.SubmissionValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RosterService rosterService(ObjectMapper objectMapper) {
        return new RosterService(objectMapper);
    }

    @Bean
    public FatigueService fatigueService(HeroUsageRepositoryPort heroUsageRepository) {
        return new FatigueService(heroUsageRepository);
    }

    @Bean
    public SubmissionValidator submissionValidator(RosterService rosterService) {
        return new SubmissionValidator(rosterService);
    }

    @Bean
    public BattleEngine battleEngine() {
        return new BattleEngine();
    }
}

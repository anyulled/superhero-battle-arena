package org.barcelonajug.superherobattlearena.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .servers(List.of(new Server().url("https://superhero-battle-arena.barcelonajug.org")));
  }
}

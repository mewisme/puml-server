package mew.pumlserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PUML Render Server API")
                        .description("REST API for rendering PlantUML diagrams to various formats (SVG, PNG, Text)")
                        .version("1.0"));
    }
}


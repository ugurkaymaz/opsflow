package com.opsflow.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI opsFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OpsFlow API")
                        .version("1.0.0")
                        .description(
                                "REST API for the OpsFlow operations management system. " +
                                        "Provides endpoints for creating, updating, deleting, " +
                                        "searching, filtering, paginating, and reporting operation records."
                        )
                        .contact(new Contact()
                                .name("Ugur Kaymaz")));
    }
}
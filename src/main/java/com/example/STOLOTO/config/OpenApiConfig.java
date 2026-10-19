package com.example.STOLOTO.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger UI: /swagger-ui.html, схема: /v3/api-docs.
 * Токен из POST /api/v1/auth/login вставляется кнопкой «Authorize» (без префикса Bearer).
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI stolotoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("STOLOTO REST API")
                        .version("v1")
                        .description("Мероприятия, запись и заявки, организации, обсуждения, модерация. "
                                + "Демо-вход: POST /api/v1/auth/login {\"demo_account_id\": 1..5}."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    /** Springdoc строит схемы своим Jackson 2 — задаём тот же snake_case, что у API. */
    @Bean
    ModelResolver snakeCaseModelResolver() {
        return new ModelResolver(Json.mapper().copy().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE));
    }

    /** API пишет енамы в нижнем регистре (open, pending ...) — показываем так же. */
    @Bean
    OpenApiCustomizer lowercaseEnums() {
        return openApi -> {
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                openApi.getComponents().getSchemas().values().forEach(OpenApiConfig::lowercaseEnums);
            }
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(path -> path.readOperations().forEach(op -> {
                    if (op.getParameters() != null) {
                        op.getParameters().forEach(p -> lowercaseEnums(p.getSchema()));
                    }
                }));
            }
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void lowercaseEnums(Schema schema) {
        if (schema == null) {
            return;
        }
        List values = schema.getEnum();
        if (values != null && !values.isEmpty() && values.getFirst() instanceof String) {
            schema.setEnum(values.stream().map(v -> ((String) v).toLowerCase(Locale.ROOT)).toList());
        }
        Map<String, Schema> props = schema.getProperties();
        if (props != null) {
            props.values().forEach(OpenApiConfig::lowercaseEnums);
        }
        lowercaseEnums(schema.getItems());
    }
}

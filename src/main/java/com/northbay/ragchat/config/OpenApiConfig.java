
package com.northbay.ragchat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
 * Defines API metadata and security settings for the NorthBay RAG Chat Service.
 * <p>
 * The API uses an API key for authentication, provided in the
 * {@code X-API-KEY} HTTP header.
 */

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ragChatOpenAPI() {
        final String securitySchemeName = "ApiKeyAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("NorthBay RAG Chat Service API")
                        .version("1.0.0")
                        .description("""
                            Use the "Authorize" button above and enter your API key.
                            Header name: **X-API-KEY**
                            Default value: **demo-key**
                        """))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name("X-API-KEY")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("Enter your API key to authorize requests")));
    }
}


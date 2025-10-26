package com.northbay.ragchat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Spring configuration class for customizing the Jackson {@link ObjectMapper}.
 * <p>
 * Registers additional modules to support:
 * <ul>
 *   <li>OpenAPI's {@code JsonNullable} types</li>
 *   <li>Java 8+ date/time classes such as {@code OffsetDateTime} and {@code Instant}</li>
 * </ul>
 * Disables timestamp serialization for date/time values to ensure ISO-8601 format in JSON.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // ✅ Support OpenAPI JsonNullable
        mapper.registerModule(new JsonNullableModule());
        // ✅ Support Java 8 date/time types (OffsetDateTime, Instant, etc.)
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}

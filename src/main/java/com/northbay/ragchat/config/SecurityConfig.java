package com.northbay.ragchat.config;

import com.northbay.ragchat.security.ApiKeyFilter;
import com.northbay.ragchat.security.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Spring configuration class for registering and ordering security filters.
 * <p>
 * Ensures that all API endpoints under {@code /api/*} are protected by both
 * API key validation and rate limiting filters, applied in the correct sequence.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(ApiKeyFilter filter) {
        FilterRegistrationBean<ApiKeyFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(1);
        reg.addUrlPatterns("/api/*");
        return reg;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(2);
        reg.addUrlPatterns("/api/*");
        return reg;
    }
}

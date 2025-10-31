package com.northbay.ragchat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for communicating with the Groq LLM API.
 * Compatible with Groq's OpenAI-style /chat/completions endpoint.
 */
@Slf4j
@Service
public class GroqLLMService {

    private final WebClient webClient;
    private final String defaultModel;

    public GroqLLMService(
            WebClient.Builder webClientBuilder,
            @Value("${groq.api.url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.model:llama3-70b-8192}") String model
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.defaultModel = model;
        log.info("✅ GroqLLMService initialized using model: {}", defaultModel);
    }

    /**
     * Sends a prompt to Groq and returns the model-generated text.
     */
    @SuppressWarnings("unchecked")
    public String generateCompletion(String userPrompt) {
        try {
            log.debug("Sending prompt to Groq model {}: {}", defaultModel, userPrompt);

            Map<String, Object> payload = Map.of(
                    "model", defaultModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful assistant."),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.3,
                    "stream", false
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.error("Groq API HTTP {} error: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Groq API error: " + e.getResponseBodyAsString(), e));
                    })
                    .onErrorResume(e -> {
                        log.error("Groq API call failed: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Groq API call failed: " + e.getMessage(), e));
                    })
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Groq API");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Groq response missing 'choices' field: " + response);
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

            if (message != null && message.get("content") != null) {
                String text = message.get("content").toString().trim();
                log.debug("✅ Groq LLM responded with {} chars", text.length());
                return text;
            }

            log.warn("Groq returned unexpected format: {}", response);
            return "[Unexpected Groq response format]";
        } catch (Exception e) {
            log.error("Groq LLM call failed: {}", e.getMessage(), e);
            return "[Error generating response from Groq: " + e.getMessage() + "]";
        }
    }
}

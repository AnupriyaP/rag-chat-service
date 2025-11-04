package com.northbay.ragchat.service.impl;

import com.northbay.ragchat.exception.ApiException;
import com.northbay.ragchat.service.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for communicating with the Groq LLM API.
 * Compatible with Groq's OpenAI-style /chat/completions endpoint.
 */
@Slf4j
@Service
public class GroqLLMServiceImpl implements LLMService {

    private final WebClient webClient;
    private final String defaultModel;
    private final String systemPrompt;


    public GroqLLMServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${groq.api.url}") String baseUrl,
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.model}") String model,
            @Value("${groq.system-prompt}") String systemPrompt
    ) {
        this.defaultModel = model;
        this.systemPrompt = systemPrompt;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        log.info("✅ GroqLLMService initialized using model: {} and systemPrompt='{}'", defaultModel,systemPrompt);
    }

    /**
     * Sends a prompt to Groq LLM and returns the generated assistant response text.
     * This method handles errors, retries, and metrics recording.
     */
    @SuppressWarnings("unchecked")
    public String generateResponse(String userPrompt) {
        long start = System.nanoTime();

        try {
            log.debug("Sending prompt to Groq model {}: {}", defaultModel, userPrompt);

            // ✅ Request body (Groq API expects 'model' and 'messages' fields)

            //It’s like giving the model a job description before the conversation starts.
            Map<String, Object> payload = Map.of(
                    "model", defaultModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.3,
                    "stream", false
            );

            // ✅ Perform API call (POST /chat/completions)

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                            .flatMap(msg -> {
                                log.error("Groq API returned error: {}", msg);
                                return Mono.error(new ApiException("Groq API error: " + msg));
                            }))
                    .bodyToMono(Map.class)
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                            .filter(ex -> !(ex instanceof ApiException)))
                    .block(Duration.ofSeconds(15));

            if (response == null) {
                throw new ApiException("Empty response from Groq API");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new ApiException("Groq response missing 'choices' field: " + response);
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
        } catch (WebClientResponseException e) {
            log.error("[{}] Groq HTTP {} error: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return "[Groq API error: " + e.getResponseBodyAsString() + "]";

        }
        catch (Exception e) {
            log.error("Groq LLM call failed: {}", e.getMessage(), e);
            return "[Error generating response from Groq: " + e.getMessage() + "]";
        }
        finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("[{}] Groq LLM API call completed in {} ms (model={})", durationMs, defaultModel);
        }
    }
}

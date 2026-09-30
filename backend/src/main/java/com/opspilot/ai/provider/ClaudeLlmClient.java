package com.opspilot.ai.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opspilot.ai.LlmClient;
import com.opspilot.ai.config.AiProperties;
import com.opspilot.ai.dto.AccountAiAnalysis;
import com.opspilot.ai.exception.AiConfigurationException;
import com.opspilot.ai.exception.AiProviderResponseException;
import com.opspilot.ai.exception.AiProviderUnavailableException;
import com.opspilot.ai.model.AccountAnalysisContext;
import com.opspilot.ai.prompt.AccountAnalysisPromptBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class ClaudeLlmClient implements LlmClient {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestClient restClient;
    private final AiProperties properties;
    private final AccountAnalysisPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public ClaudeLlmClient(
            @Qualifier("aiRestClient") RestClient restClient,
            AiProperties properties,
            AccountAnalysisPromptBuilder promptBuilder,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public AccountAiAnalysis analyze(AccountAnalysisContext context) {
        if (!properties.isConfigured()) {
            throw new AiConfigurationException();
        }

        ClaudeRequest request = new ClaudeRequest(
                properties.model(),
                properties.maxTokens(),
                List.of(new ClaudeMessage("user", promptBuilder.build(context)))
        );

        try {
            String responseBody = restClient.post()
                    .uri("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-api-key", properties.apiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            return parseResponse(responseBody);
        } catch (ResourceAccessException exception) {
            throw new AiProviderUnavailableException();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is5xxServerError()
                    || exception.getStatusCode().value() == 429) {
                throw new AiProviderUnavailableException();
            }
            throw new AiProviderResponseException();
        } catch (RestClientException exception) {
            throw new AiProviderUnavailableException();
        }
    }

    private AccountAiAnalysis parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiProviderResponseException();
        }

        try {
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode content = response.path("content");
            if (!content.isArray()) {
                throw new AiProviderResponseException();
            }

            String analysisJson = null;
            for (JsonNode block : content) {
                if ("text".equals(block.path("type").asText()) && block.path("text").isTextual()) {
                    analysisJson = block.path("text").asText();
                    break;
                }
            }
            if (analysisJson == null || analysisJson.isBlank()) {
                throw new AiProviderResponseException();
            }

            return objectMapper.readerFor(AccountAiAnalysis.class)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(analysisJson);
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            throw new AiProviderResponseException();
        }
    }

    private record ClaudeRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            List<ClaudeMessage> messages
    ) {
    }

    private record ClaudeMessage(String role, String content) {
    }
}

package com.opspilot.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opspilot.ai.config.AiProperties;
import com.opspilot.ai.dto.AccountAiAnalysis;
import com.opspilot.ai.exception.AiConfigurationException;
import com.opspilot.ai.exception.AiProviderResponseException;
import com.opspilot.ai.exception.AiProviderUnavailableException;
import com.opspilot.ai.model.AccountAnalysisContext;
import com.opspilot.ai.prompt.AccountAnalysisPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClaudeLlmClientTest {

    private static final String ANALYSIS_JSON = """
            {"summary":"Support pressure is elevated.","keyConcerns":["Critical tickets are open."],"recommendedActions":[{"action":"Review critical tickets.","evidence":"Critical open tickets are present."}]}
            """;

    private final AccountAnalysisContext context = mock(AccountAnalysisContext.class);
    private final AccountAnalysisPromptBuilder promptBuilder = mock(AccountAnalysisPromptBuilder.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer server;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://provider.test/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        when(promptBuilder.build(context)).thenReturn("trusted prompt");
    }

    @Test
    void shouldSendConfiguredRequestAndParseStructuredAnalysis() {
        server.expect(once(), requestTo("https://provider.test/v1/messages"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("x-api-key", "test-key"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.max_tokens").value(1200))
                .andExpect(jsonPath("$.messages[0].content").value("trusted prompt"))
                .andRespond(withSuccess(providerResponse(ANALYSIS_JSON), MediaType.APPLICATION_JSON));

        AccountAiAnalysis result = client(configuredProperties()).analyze(context);

        assertThat(result.summary()).isEqualTo("Support pressure is elevated.");
        assertThat(result.keyConcerns()).containsExactly("Critical tickets are open.");
        assertThat(result.recommendedActions().get(0).evidence())
                .isEqualTo("Critical open tickets are present.");
        server.verify();
    }

    @Test
    void shouldFailWithoutCallingProviderWhenConfigurationIsMissing() {
        AiProperties properties = new AiProperties(
                "", "", URI.create("https://provider.test/v1"),
                Duration.ofSeconds(5), Duration.ofSeconds(30), 1200
        );

        assertThatThrownBy(() -> client(properties).analyze(context))
                .isInstanceOf(AiConfigurationException.class);
        server.verify();
    }

    @Test
    void shouldMapTimeoutToProviderUnavailable() {
        server.expect(requestTo("https://provider.test/v1/messages"))
                .andRespond(withException(new SocketTimeoutException("timed out")));

        assertThatThrownBy(() -> client(configuredProperties()).analyze(context))
                .isInstanceOf(AiProviderUnavailableException.class)
                .hasMessage("AI Account Analyst is temporarily unavailable");
    }

    @Test
    void shouldMapProviderFailureToUnavailableWithoutLeakingResponse() {
        server.expect(requestTo("https://provider.test/v1/messages"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client(configuredProperties()).analyze(context))
                .isInstanceOf(AiProviderUnavailableException.class)
                .hasMessage("AI Account Analyst is temporarily unavailable");
    }

    @Test
    void shouldRejectMalformedStructuredAnalysis() {
        server.expect(requestTo("https://provider.test/v1/messages"))
                .andRespond(withSuccess(providerResponse("not-json"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client(configuredProperties()).analyze(context))
                .isInstanceOf(AiProviderResponseException.class)
                .hasMessage("AI Account Analyst returned an invalid response");
    }

    private ClaudeLlmClient client(AiProperties properties) {
        return new ClaudeLlmClient(restClient, properties, promptBuilder, objectMapper);
    }

    private AiProperties configuredProperties() {
        return new AiProperties(
                "test-key", "test-model", URI.create("https://provider.test/v1"),
                Duration.ofSeconds(5), Duration.ofSeconds(30), 1200
        );
    }

    private String providerResponse(String analysisJson) {
        try {
            return objectMapper.writeValueAsString(new ProviderResponse(
                    new ProviderContent("text", analysisJson)
            ));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private record ProviderResponse(java.util.List<ProviderContent> content) {
        private ProviderResponse(ProviderContent content) {
            this(java.util.List.of(content));
        }
    }

    private record ProviderContent(String type, String text) {
    }
}

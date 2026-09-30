package com.opspilot.ai.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opspilot.ai.model.AccountAnalysisContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountAnalysisPromptBuilder {

    private final ObjectMapper objectMapper;

    public String build(AccountAnalysisContext context) {
        return """
                You are the OpsPilot AI Account Analyst. Interpret the trusted account context below.

                Rules:
                - Use only facts explicitly present in the supplied context.
                - Do not invent facts, events, causes, customer intent, or unavailable history.
                - Do not infer a latest interaction when latestInteractionAt is null.
                - Risk, Potential, and Priority scores and levels are authoritative deterministic outputs.
                - Never recalculate, replace, challenge, or modify those scores or levels.
                - Explain observations using evidence present in the context.
                - Keep recommendations operational, concise, and grounded in supplied evidence.
                - Do not present speculation as fact.
                - Return one valid JSON object only. Do not use Markdown or code fences.
                - Return exactly these fields:
                  {"summary":"string","keyConcerns":["string"],"recommendedActions":[{"action":"string","evidence":"string"}]}
                - Do not include Risk, Potential, or Priority replacement fields in the response.

                Trusted account context:
                """ + serialize(context);
    }

    private String serialize(AccountAnalysisContext context) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize trusted account context", exception);
        }
    }
}

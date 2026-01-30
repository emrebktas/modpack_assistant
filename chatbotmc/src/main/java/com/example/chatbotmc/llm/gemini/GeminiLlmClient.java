package com.example.chatbotmc.llm.gemini;

import com.example.chatbotmc.llm.LlmClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@Profile("gemini")
public class GeminiLlmClient implements LlmClient {

    private final WebClient webClient;
    private final String apiKey;

    public GeminiLlmClient(
            WebClient geminiWebClient,
            @Value("${gemini.api-key}") String apiKey
    ) {
        this.webClient = geminiWebClient;
        this.apiKey = apiKey;
    }

    @Override
    public String generate(String prompt) {

        GeminiRequest request = new GeminiRequest(
                List.of(
                        new GeminiRequest.Content(
                                List.of(new GeminiRequest.Part(prompt))
                        )
                )
        );

        Map<String, Object> response = webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        return extractText(response);
    }

    private String extractText(Map<String, Object> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> part = (Map<?, ?>) parts.get(0);
            return (String) part.get("text");
        } catch (Exception e) {
            return "Gemini response parsing failed.";
        }
    }
}

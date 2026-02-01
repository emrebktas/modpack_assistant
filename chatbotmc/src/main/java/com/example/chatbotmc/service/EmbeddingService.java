package com.example.chatbotmc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Service for generating text embeddings using Google Gemini Embedding API
 */
@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    
    private final WebClient webClient;
    private final String apiKey;
    
    public EmbeddingService(
            WebClient geminiWebClient,
            @Value("${gemini.api-key}") String apiKey
    ) {
        this.webClient = geminiWebClient;
        this.apiKey = apiKey;
    }
    
    /**
     * Generate embedding vector for a given text using Gemini text-embedding-004 model
     * Returns a 768-dimensional vector
     */
    public List<Double> generateEmbedding(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                logger.warn("Empty text provided for embedding generation");
                return null;
            }
            
            // Truncate text if too long (Gemini has input limits)
            String truncatedText = text.length() > 10000 ? text.substring(0, 10000) : text;
            
            // Build request body
            Map<String, Object> requestBody = Map.of(
                "content", Map.of(
                    "parts", List.of(
                        Map.of("text", truncatedText)
                    )
                )
            );
            
            // Call Gemini embedding API
            Map<String, Object> response = webClient.post()
                    .uri("/v1beta/models/text-embedding-004:embedContent?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            return extractEmbedding(response);
            
        } catch (Exception e) {
            logger.error("Failed to generate embedding: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Generate embeddings for multiple texts in batch
     * Note: Currently processes sequentially. For production, consider batching API calls.
     */
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        return texts.stream()
                .map(this::generateEmbedding)
                .toList();
    }
    
    /**
     * Extract embedding vector from Gemini API response
     */
    @SuppressWarnings("unchecked")
    private List<Double> extractEmbedding(Map<String, Object> response) {
        try {
            Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
            List<Double> values = (List<Double>) embedding.get("values");
            
            if (values == null || values.isEmpty()) {
                logger.error("No embedding values in response");
                return null;
            }
            
            logger.debug("Generated embedding with {} dimensions", values.size());
            return values;
            
        } catch (Exception e) {
            logger.error("Failed to extract embedding from response: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert embedding vector to pgvector format string
     * Example: [0.1, 0.2, 0.3] -> "[0.1,0.2,0.3]"
     */
    public String embeddingToVectorString(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            sb.append(embedding.get(i));
            if (i < embedding.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

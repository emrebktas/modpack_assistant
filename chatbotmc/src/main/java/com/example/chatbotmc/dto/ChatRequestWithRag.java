package com.example.chatbotmc.dto;

/**
 * Extended chat request with RAG configuration options
 */
public record ChatRequestWithRag(
    String prompt,
    Long conversationId,
    Boolean useRag,          // Optional: explicitly enable/disable RAG for this request
    Integer topK,            // Optional: override default top-k value
    String modpackFilter     // Optional: filter results by modpack name
) {
    // Constructor with defaults
    public ChatRequestWithRag(String prompt, Long conversationId) {
        this(prompt, conversationId, true, null, null);
    }
}

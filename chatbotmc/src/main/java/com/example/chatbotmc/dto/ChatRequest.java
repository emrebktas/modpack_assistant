package com.example.chatbotmc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotBlank(message = "Prompt cannot be empty")
    @Size(min = 1, max = 5000, message = "Prompt must be between 1 and 5000 characters")
    String prompt,
    
    Long conversationId  // Optional: null for new conversation
) {}
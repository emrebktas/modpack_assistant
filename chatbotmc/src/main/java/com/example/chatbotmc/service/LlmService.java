package com.example.chatbotmc.service;

import com.example.chatbotmc.llm.LlmClient;
import com.example.chatbotmc.prompt.PromptBuilder;
import org.springframework.stereotype.Service;

@Service
public class LlmService {

    private final LlmClient llmClient;

    public LlmService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public String chat(String userInput) {
        String prompt = PromptBuilder.minecraftPrompt(userInput);
        return llmClient.generate(prompt);
    }
}

package com.example.chatbotmc.controller;

import com.example.chatbotmc.dto.ChatRequest;
import com.example.chatbotmc.service.LlmService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        return llmService.chat(request.prompt());
    }
}

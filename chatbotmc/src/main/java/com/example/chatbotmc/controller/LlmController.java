package com.example.chatbotmc.controller;

import com.example.chatbotmc.dto.ChatRequest;
import com.example.chatbotmc.dto.ChatResponse;
import com.example.chatbotmc.service.JwtService;
import com.example.chatbotmc.service.LlmService;
import com.example.chatbotmc.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;
    private final JwtService jwtService;
    private final UserService userService;

    public LlmController(LlmService llmService, JwtService jwtService, UserService userService) {
        this.llmService = llmService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract userId from JWT token with validation
        Long userId = extractUserIdFromHeader(authHeader);
        
        // Check and increment query count
        userService.incrementQueryCount(userId);
        
        ChatResponse response = llmService.chatWithHistory(userId, request.prompt(), request.conversationId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/remaining-queries")
    public ResponseEntity<RemainingQueriesResponse> getRemainingQueries(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract userId from JWT token with validation
        Long userId = extractUserIdFromHeader(authHeader);
        
        int remaining = userService.getRemainingQueries(userId);
        return ResponseEntity.ok(new RemainingQueriesResponse(remaining));
    }
    
    /**
     * Safely extracts userId from Authorization header with proper validation
     */
    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
    
    private record RemainingQueriesResponse(int remainingQueries) {}
}

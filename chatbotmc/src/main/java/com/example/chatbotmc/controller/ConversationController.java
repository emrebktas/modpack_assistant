package com.example.chatbotmc.controller;

import com.example.chatbotmc.dto.ChatMessageDTO;
import com.example.chatbotmc.dto.ConversationDTO;
import com.example.chatbotmc.service.ConversationService;
import com.example.chatbotmc.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    
    private final ConversationService conversationService;
    private final JwtService jwtService;
    
    public ConversationController(ConversationService conversationService, JwtService jwtService) {
        this.conversationService = conversationService;
        this.jwtService = jwtService;
    }
    
    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getUserConversations(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }
    
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(conversationService.getConversationMessages(conversationId, userId));
    }
    
    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody CreateConversationRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        var conversation = conversationService.createConversation(userId, request.title());
        return ResponseEntity.ok(new ConversationDTO(
            conversation.getId(),
            conversation.getTitle(),
            conversation.getCreatedAt(),
            conversation.getUpdatedAt(),
            0
        ));
    }
    
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{conversationId}/title")
    public ResponseEntity<Void> updateTitle(
            @PathVariable Long conversationId,
            @RequestBody UpdateTitleRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        conversationService.updateConversationTitle(conversationId, userId, request.title());
        return ResponseEntity.ok().build();
    }
    
    /**
     * Safely extracts userId from Authorization header with proper validation
     */
    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}

record CreateConversationRequest(String title) {}
record UpdateTitleRequest(String title) {}

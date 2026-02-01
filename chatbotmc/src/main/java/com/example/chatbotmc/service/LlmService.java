package com.example.chatbotmc.service;

import com.example.chatbotmc.dto.ChatResponse;
import com.example.chatbotmc.entity.Conversation;
import com.example.chatbotmc.entity.MessageRole;
import com.example.chatbotmc.entity.RagChunk;
import com.example.chatbotmc.llm.LlmClient;
import com.example.chatbotmc.prompt.PromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LlmService {

    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);
    
    private final LlmClient llmClient;
    private final ConversationService conversationService;
    private final RagService ragService;

    public LlmService(LlmClient llmClient, ConversationService conversationService, RagService ragService) {
        this.llmClient = llmClient;
        this.conversationService = conversationService;
        this.ragService = ragService;
    }

    /**
     * Simple chat without conversation history (uses RAG if available)
     */
    public String chat(String userInput) {
        String prompt = generatePromptWithRag(userInput);
        return llmClient.generate(prompt);
    }

    /**
     * Chat with conversation history and RAG support
     */
    @Transactional
    public ChatResponse chatWithHistory(Long userId, String userInput, Long conversationId) {
        // Create new conversation if none provided
        if (conversationId == null) {
            String title = conversationService.generateConversationTitle(userInput);
            Conversation newConversation = conversationService.createConversation(userId, title);
            conversationId = newConversation.getId();
        }
        
        // Save user message
        conversationService.saveMessage(conversationId, userId, userInput, MessageRole.USER);
        
        // Generate AI response with RAG
        String prompt = generatePromptWithRag(userInput);
        String aiResponse = llmClient.generate(prompt);
        
        // Save AI message
        var savedMessage = conversationService.saveMessage(conversationId, userId, aiResponse, MessageRole.ASSISTANT);
        
        return new ChatResponse(aiResponse, conversationId, savedMessage.getId());
    }
    
    /**
     * Generate prompt with RAG context if available, otherwise use fallback
     */
    private String generatePromptWithRag(String userInput) {
        try {
            // Check if RAG is available
            if (!ragService.isRagAvailable()) {
                logger.debug("RAG not available, using fallback prompt");
                return PromptBuilder.minecraftPrompt(userInput);
            }
            
            // Retrieve relevant chunks using RAG
            logger.debug("Retrieving RAG context for user input");
            List<RagChunk> relevantChunks = ragService.retrieveRelevantChunks(userInput);
            
            if (relevantChunks.isEmpty()) {
                logger.debug("No relevant chunks found, using fallback prompt");
                return PromptBuilder.minecraftPrompt(userInput);
            }
            
            logger.info("Found {} relevant chunks for RAG context", relevantChunks.size());
            
            // Build augmented prompt with RAG context
            return ragService.buildAugmentedPrompt(userInput, relevantChunks);
            
        } catch (Exception e) {
            logger.error("Error generating RAG prompt, falling back to standard prompt: {}", e.getMessage());
            return PromptBuilder.minecraftPrompt(userInput);
        }
    }
}

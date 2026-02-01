package com.example.chatbotmc.service;

import com.example.chatbotmc.entity.RagChunk;
import com.example.chatbotmc.repository.RagChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Retrieval-Augmented Generation (RAG)
 * Handles semantic search and context building for LLM queries
 */
@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);
    
    private final RagChunkRepository ragChunkRepository;
    private final EmbeddingService embeddingService;
    
    @Value("${rag.top-k:5}")
    private int defaultTopK;
    
    @Value("${rag.similarity-threshold:0.7}")
    private double similarityThreshold;
    
    @Value("${rag.enabled:true}")
    private boolean ragEnabled;
    
    public RagService(RagChunkRepository ragChunkRepository, EmbeddingService embeddingService) {
        this.ragChunkRepository = ragChunkRepository;
        this.embeddingService = embeddingService;
    }
    
    /**
     * Retrieve relevant chunks for a given query using semantic similarity
     * 
     * @param query The user's question or query
     * @param topK Number of top similar chunks to retrieve
     * @return List of relevant RAG chunks
     */
    public List<RagChunk> retrieveRelevantChunks(String query, int topK) {
        if (!ragEnabled) {
            logger.debug("RAG is disabled");
            return List.of();
        }
        
        try {
            logger.debug("Retrieving relevant chunks for query: {}", query);
            
            // Generate embedding for the query
            List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
            
            if (queryEmbedding == null || queryEmbedding.isEmpty()) {
                logger.warn("Failed to generate embedding for query");
                return List.of();
            }
            
            // Convert to vector string format
            String vectorString = embeddingService.embeddingToVectorString(queryEmbedding);
            
            // Perform similarity search
            List<RagChunk> chunks = ragChunkRepository.findSimilarByEmbedding(vectorString, topK);
            
            logger.debug("Retrieved {} relevant chunks", chunks.size());
            return chunks;
            
        } catch (Exception e) {
            logger.error("Error retrieving relevant chunks: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Retrieve relevant chunks using default topK value
     */
    public List<RagChunk> retrieveRelevantChunks(String query) {
        return retrieveRelevantChunks(query, defaultTopK);
    }
    
    /**
     * Retrieve relevant chunks filtered by modpack
     */
    public List<RagChunk> retrieveRelevantChunksByModpack(String query, String modpack, int topK) {
        if (!ragEnabled) {
            return List.of();
        }
        
        try {
            List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
            if (queryEmbedding == null) {
                return List.of();
            }
            
            String vectorString = embeddingService.embeddingToVectorString(queryEmbedding);
            return ragChunkRepository.findSimilarByEmbeddingAndModpack(vectorString, modpack, topK);
            
        } catch (Exception e) {
            logger.error("Error retrieving chunks by modpack: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Build context string from retrieved chunks
     * Formats chunks into a readable context for the LLM
     */
    public String buildContextString(List<RagChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < chunks.size(); i++) {
            RagChunk chunk = chunks.get(i);
            
            // Add metadata header
            context.append("--- Document ").append(i + 1).append(" ---\n");
            
            if (chunk.getModpack() != null) {
                context.append("Modpack: ").append(chunk.getModpack()).append("\n");
            }
            if (chunk.getModName() != null) {
                context.append("Mod: ").append(chunk.getModName()).append("\n");
            }
            if (chunk.getCategory() != null) {
                context.append("Category: ").append(chunk.getCategory()).append("\n");
            }
            
            context.append("\n");
            context.append(chunk.getText());
            context.append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * Build an augmented prompt combining user query with RAG context
     * 
     * @param userQuery The user's question
     * @param chunks Retrieved relevant chunks
     * @return Formatted prompt with context for the LLM
     */
    public String buildAugmentedPrompt(String userQuery, List<RagChunk> chunks) {
        String context = buildContextString(chunks);
        
        if (context.isEmpty()) {
            // No context available, return standard prompt
            return buildFallbackPrompt(userQuery);
        }
        
        return String.format("""
            You are a knowledgeable Minecraft modpack expert assistant. Your role is to help players understand and use various Minecraft mods and modpacks.
            
            CONTEXT FROM DOCUMENTATION:
            %s
            
            INSTRUCTIONS:
            - Answer the user's question based primarily on the provided context above
            - Be specific and reference the mod names and modpacks mentioned in the context
            - If the context contains relevant information, cite it in your answer
            - If the context doesn't fully answer the question, use your general Minecraft knowledge but mention the limitation
            - Provide step-by-step instructions when applicable
            - If the question is not about Minecraft, politely redirect to Minecraft topics
            
            USER'S QUESTION:
            %s
            
            Please provide a helpful, accurate, and friendly response.
            """, context, userQuery);
    }
    
    /**
     * Build a fallback prompt when no RAG context is available
     */
    private String buildFallbackPrompt(String userQuery) {
        return String.format("""
            You are a helpful Minecraft assistant chatbot. You have extensive knowledge about:
            - Minecraft gameplay, mechanics, and strategies
            - Modpacks, mods, and mod configurations
            - Building techniques and redstone circuits
            - Server setup and administration
            - Game updates and features
            
            Please provide helpful, accurate, and friendly responses to Minecraft-related questions.
            If the question is not about Minecraft, politely redirect the conversation back to Minecraft topics.
            
            User's question: %s
            """, userQuery);
    }
    
    /**
     * Check if RAG system is enabled and has data loaded
     */
    public boolean isRagAvailable() {
        if (!ragEnabled) {
            return false;
        }
        
        try {
            long count = ragChunkRepository.countByEmbeddingIsNotNull();
            return count > 0;
        } catch (Exception e) {
            logger.error("Error checking RAG availability: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get statistics about the RAG system
     */
    public RagStats getStats() {
        long totalChunks = ragChunkRepository.count();
        long chunksWithEmbeddings = ragChunkRepository.countByEmbeddingIsNotNull();
        
        return new RagStats(
            ragEnabled,
            totalChunks,
            chunksWithEmbeddings,
            defaultTopK,
            similarityThreshold
        );
    }
    
    /**
     * Simple record for RAG statistics
     */
    public record RagStats(
        boolean enabled,
        long totalChunks,
        long chunksWithEmbeddings,
        int defaultTopK,
        double similarityThreshold
    ) {}
}

package com.example.chatbotmc.controller;

import com.example.chatbotmc.entity.RagChunk;
import com.example.chatbotmc.service.EmbeddingService;
import com.example.chatbotmc.repository.RagChunkRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test endpoints for RAG functionality
 * ADMIN ONLY - Use these to verify embeddings and similarity search work correctly
 * These endpoints consume API quota and should not be public
 */
@RestController
@RequestMapping("/api/test")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class RagTestController {
    
    private final EmbeddingService embeddingService;
    private final RagChunkRepository ragChunkRepository;
    
    public RagTestController(EmbeddingService embeddingService, RagChunkRepository ragChunkRepository) {
        this.embeddingService = embeddingService;
        this.ragChunkRepository = ragChunkRepository;
    }
    
    /**
     * Test embedding generation
     * GET /api/test/embedding?text=your text here
     */
    @GetMapping("/embedding")
    public ResponseEntity<?> testEmbedding(
            @RequestParam @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters") String text) {
        List<Double> embedding = embeddingService.generateEmbedding(text);
        
        if (embedding == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate embedding"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "text", text,
            "embedding_dimensions", embedding.size(),
            "embedding_sample", embedding.subList(0, Math.min(10, embedding.size())),
            "vector_string", embeddingService.embeddingToVectorString(embedding)
        ));
    }
    
    /**
     * Test similarity search with a query
     * GET /api/test/search?query=how to tame dragons&limit=3
     */
    @GetMapping("/search")
    public ResponseEntity<?> testSimilaritySearch(
            @RequestParam @Size(min = 1, max = 1000, message = "Query must be between 1 and 1000 characters") String query,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
    ) {
        // Generate embedding for query
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
        
        if (queryEmbedding == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate query embedding"
            ));
        }
        
        String vectorString = embeddingService.embeddingToVectorString(queryEmbedding);
        
        // Search for similar chunks
        List<RagChunk> similarChunks = ragChunkRepository.findSimilarByEmbedding(vectorString, limit);
        
        // Format response
        List<Map<String, Object>> results = similarChunks.stream()
            .map(chunk -> Map.<String, Object>of(
                "chunk_id", chunk.getChunkId(),
                "modpack", chunk.getModpack() != null ? chunk.getModpack() : "N/A",
                "mod_name", chunk.getModName() != null ? chunk.getModName() : "N/A",
                "category", chunk.getCategory() != null ? chunk.getCategory() : "N/A",
                "text_preview", chunk.getText().substring(0, Math.min(200, chunk.getText().length())) + "..."
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "query", query,
            "results_count", results.size(),
            "results", results
        ));
    }
    
    /**
     * Test similarity search with modpack filter
     * GET /api/test/search-modpack?query=dragons&modpack=BetterMC&limit=3
     */
    @GetMapping("/search-modpack")
    public ResponseEntity<?> testSearchByModpack(
            @RequestParam @Size(min = 1, max = 1000, message = "Query must be between 1 and 1000 characters") String query,
            @RequestParam 
            @Size(min = 1, max = 100, message = "Modpack name must be between 1 and 100 characters")
            @Pattern(regexp = "^[a-zA-Z0-9\\s_-]+$", message = "Modpack name can only contain letters, numbers, spaces, underscores and hyphens")
            String modpack,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
    ) {
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
        
        if (queryEmbedding == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate query embedding"
            ));
        }
        
        String vectorString = embeddingService.embeddingToVectorString(queryEmbedding);
        List<RagChunk> similarChunks = ragChunkRepository.findSimilarByEmbeddingAndModpack(
            vectorString, 
            modpack, 
            limit
        );
        
        List<Map<String, Object>> results = similarChunks.stream()
            .map(chunk -> Map.<String, Object>of(
                "chunk_id", chunk.getChunkId(),
                "mod_name", chunk.getModName() != null ? chunk.getModName() : "N/A",
                "category", chunk.getCategory() != null ? chunk.getCategory() : "N/A",
                "text_preview", chunk.getText().substring(0, Math.min(200, chunk.getText().length())) + "..."
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "query", query,
            "modpack", modpack,
            "results_count", results.size(),
            "results", results
        ));
    }
}

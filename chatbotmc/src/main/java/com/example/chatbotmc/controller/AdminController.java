package com.example.chatbotmc.controller;

import com.example.chatbotmc.service.RagDataLoader;
import com.example.chatbotmc.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin endpoints for managing RAG data and system operations
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final RagDataLoader ragDataLoader;
    private final RagService ragService;
    
    public AdminController(RagDataLoader ragDataLoader, RagService ragService) {
        this.ragDataLoader = ragDataLoader;
        this.ragService = ragService;
    }
    
    /**
     * Manually trigger RAG data loading from data.json
     * POST /api/admin/load-rag-data
     */
    @PostMapping("/load-rag-data")
    public ResponseEntity<Map<String, String>> loadRagData() {
        try {
            ragDataLoader.loadRagData();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "RAG data loaded successfully",
                "stats", ragDataLoader.getLoadingStats()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get RAG data loading statistics
     * GET /api/admin/rag-stats
     */
    @GetMapping("/rag-stats")
    public ResponseEntity<?> getRagStats() {
        RagService.RagStats stats = ragService.getStats();
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "loading_stats", ragDataLoader.getLoadingStats(),
            "rag_enabled", stats.enabled(),
            "total_chunks", stats.totalChunks(),
            "chunks_with_embeddings", stats.chunksWithEmbeddings(),
            "default_top_k", stats.defaultTopK(),
            "similarity_threshold", stats.similarityThreshold(),
            "rag_available", ragService.isRagAvailable()
        ));
    }
}

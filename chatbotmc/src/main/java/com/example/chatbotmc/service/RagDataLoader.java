package com.example.chatbotmc.service;

import com.example.chatbotmc.dto.RagChunkDTO;
import com.example.chatbotmc.entity.RagChunk;
import com.example.chatbotmc.repository.RagChunkRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * Service to load RAG chunks from data.json into the database with embeddings
 * Runs automatically on application startup
 */
@Service
public class RagDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RagDataLoader.class);
    
    private final RagChunkRepository ragChunkRepository;
    private final EmbeddingService embeddingService;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    
    @Value("${rag.auto-load:false}")
    private boolean autoLoad;
    
    @Value("${rag.batch-size:10}")
    private int batchSize;
    
    @Value("${rag.delay-ms:1000}")
    private long delayMs;
    
    public RagDataLoader(
            RagChunkRepository ragChunkRepository,
            EmbeddingService embeddingService,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper
    ) {
        this.ragChunkRepository = ragChunkRepository;
        this.embeddingService = embeddingService;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (autoLoad) {
            logger.info("Auto-load is enabled. Starting RAG data loading...");
            loadRagData();
        } else {
            logger.info("Auto-load is disabled. Use /api/admin/load-rag-data endpoint to load data manually.");
            logger.info("To enable auto-load, set rag.auto-load=true in application.properties");
        }
    }
    
    /**
     * Load all RAG chunks from data.json and generate embeddings
     * Can be called manually via API endpoint
     */
    public void loadRagData() {
        try {
            logger.info("Loading RAG chunks from data.json...");
            
            // Check if data already exists
            long existingCount = ragChunkRepository.count();
            if (existingCount > 0) {
                logger.info("Found {} existing chunks in database. Skipping load.", existingCount);
                logger.info("To reload, clear the rag_chunk table first.");
                return;
            }
            
            // Load JSON file
            Resource resource = resourceLoader.getResource("classpath:data.json");
            List<RagChunkDTO> chunkDTOs = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<RagChunkDTO>>() {}
            );
            
            logger.info("Loaded {} chunks from data.json", chunkDTOs.size());
            logger.info("Starting embedding generation (this may take a while)...");
            
            int totalProcessed = 0;
            int totalFailed = 0;
            int batchCount = 0;
            
            // Process in batches to avoid overwhelming the API
            for (int i = 0; i < chunkDTOs.size(); i += batchSize) {
                int end = Math.min(i + batchSize, chunkDTOs.size());
                List<RagChunkDTO> batch = chunkDTOs.subList(i, end);
                
                batchCount++;
                logger.info("Processing batch {}/{} ({}-{} of {})", 
                    batchCount, 
                    (int) Math.ceil((double) chunkDTOs.size() / batchSize),
                    i + 1, 
                    end, 
                    chunkDTOs.size()
                );
                
                for (RagChunkDTO dto : batch) {
                    try {
                        saveChunkWithTransaction(dto);
                        totalProcessed++;
                        
                        // Small delay to avoid rate limiting
                        Thread.sleep(100);
                        
                    } catch (Exception e) {
                        totalFailed++;
                        logger.error("Error processing chunk {}: {}", dto.getId(), e.getMessage(), e);
                    }
                }
                
                // Longer delay between batches
                if (end < chunkDTOs.size()) {
                    logger.info("Waiting {}ms before next batch...", delayMs);
                    Thread.sleep(delayMs);
                }
            }
            
            logger.info("✓ Successfully loaded {} chunks with embeddings", totalProcessed);
            logger.info("✗ Failed to load {} chunks", totalFailed);
            logger.info("✓ Total chunks in database: {}", ragChunkRepository.count());
            logger.info("✓ Chunks with embeddings: {}", ragChunkRepository.countByEmbeddingIsNotNull());
            
        } catch (IOException e) {
            logger.error("Failed to load data.json: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load RAG data", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Loading interrupted: {}", e.getMessage());
            throw new RuntimeException("Loading interrupted", e);
        }
    }
    
    /**
     * Save a single chunk with its own transaction to prevent cascade failures
     */
    @Transactional
    private void saveChunkWithTransaction(RagChunkDTO dto) {
        RagChunk chunk = convertToEntity(dto);
        
        // Generate embedding
        List<Double> embedding = embeddingService.generateEmbedding(dto.getText());
        if (embedding != null) {
            String vectorString = embeddingService.embeddingToVectorString(embedding);
            chunk.setEmbedding(vectorString);
        } else {
            logger.warn("Failed to generate embedding for chunk: {}", dto.getId());
        }
        
        ragChunkRepository.save(chunk);
    }
    
    /**
     * Convert RagChunkDTO to RagChunk entity
     */
    private RagChunk convertToEntity(RagChunkDTO dto) {
        RagChunk chunk = new RagChunk();
        chunk.setChunkId(dto.getId());
        chunk.setText(dto.getText());
        
        if (dto.getMetadata() != null) {
            chunk.setModpack(dto.getMetadata().getModpack());
            chunk.setModName(dto.getMetadata().getModName());
            chunk.setModVersion(dto.getMetadata().getModVersion());
            chunk.setCategory(dto.getMetadata().getCategory());
            chunk.setDocType(dto.getMetadata().getDocType());
            chunk.setLanguage(dto.getMetadata().getLanguage());
        }
        
        return chunk;
    }
    
    /**
     * Get loading statistics
     */
    public String getLoadingStats() {
        long totalChunks = ragChunkRepository.count();
        long chunksWithEmbeddings = ragChunkRepository.countByEmbeddingIsNotNull();
        
        return String.format(
            "Total chunks: %d, Chunks with embeddings: %d (%.1f%%)",
            totalChunks,
            chunksWithEmbeddings,
            totalChunks > 0 ? (chunksWithEmbeddings * 100.0 / totalChunks) : 0
        );
    }
}

package com.example.chatbotmc.repository;

import com.example.chatbotmc.entity.RagChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RagChunkRepository extends JpaRepository<RagChunk, Long> {
    
    Optional<RagChunk> findByChunkId(String chunkId);
    
    List<RagChunk> findByModpack(String modpack);
    
    List<RagChunk> findByCategory(String category);
    
    /**
     * Find similar chunks using cosine similarity with pgvector
     * Returns top K most similar chunks to the query embedding
     */
    @Query(value = """
        SELECT * FROM rag_chunk 
        WHERE embedding IS NOT NULL 
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector) 
        LIMIT :limit
        """, nativeQuery = true)
    List<RagChunk> findSimilarByEmbedding(
        @Param("queryEmbedding") String queryEmbedding, 
        @Param("limit") int limit
    );
    
    /**
     * Find similar chunks filtered by modpack
     */
    @Query(value = """
        SELECT * FROM rag_chunk 
        WHERE embedding IS NOT NULL 
        AND modpack = :modpack
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector) 
        LIMIT :limit
        """, nativeQuery = true)
    List<RagChunk> findSimilarByEmbeddingAndModpack(
        @Param("queryEmbedding") String queryEmbedding,
        @Param("modpack") String modpack,
        @Param("limit") int limit
    );
    
    /**
     * Find similar chunks with similarity score above threshold
     */
    @Query(value = """
        SELECT *, 1 - (embedding <=> CAST(:queryEmbedding AS vector)) as similarity 
        FROM rag_chunk 
        WHERE embedding IS NOT NULL 
        AND 1 - (embedding <=> CAST(:queryEmbedding AS vector)) > :threshold
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector) 
        LIMIT :limit
        """, nativeQuery = true)
    List<RagChunk> findSimilarAboveThreshold(
        @Param("queryEmbedding") String queryEmbedding,
        @Param("threshold") double threshold,
        @Param("limit") int limit
    );
    
    long countByEmbeddingIsNotNull();
}

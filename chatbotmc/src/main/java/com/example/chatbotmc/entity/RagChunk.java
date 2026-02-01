package com.example.chatbotmc.entity;

import com.example.chatbotmc.config.VectorType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "rag_chunk")
public class RagChunk {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String chunkId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
    
    @Type(VectorType.class)
    @Column(columnDefinition = "vector(768)")
    private String embedding;
    
    // Metadata fields
    private String modpack;
    private String modName;
    private String modVersion;
    private String category;
    private String docType;
    private String language;
    
    public RagChunk() {}
    
    public RagChunk(String chunkId, String text) {
        this.chunkId = chunkId;
        this.text = text;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getChunkId() {
        return chunkId;
    }
    
    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getEmbedding() {
        return embedding;
    }
    
    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }
    
    public String getModpack() {
        return modpack;
    }
    
    public void setModpack(String modpack) {
        this.modpack = modpack;
    }
    
    public String getModName() {
        return modName;
    }
    
    public void setModName(String modName) {
        this.modName = modName;
    }
    
    public String getModVersion() {
        return modVersion;
    }
    
    public void setModVersion(String modVersion) {
        this.modVersion = modVersion;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDocType() {
        return docType;
    }
    
    public void setDocType(String docType) {
        this.docType = docType;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
}

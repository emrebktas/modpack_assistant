package com.example.chatbotmc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for reading RAG chunks from data.json
 */
public class RagChunkDTO {
    
    private String id;
    private String text;
    private Metadata metadata;
    
    public RagChunkDTO() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public Metadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    public static class Metadata {
        private String modpack;
        
        @JsonProperty("mod_name")
        private String modName;
        
        @JsonProperty("mod_version")
        private String modVersion;
        
        private String category;
        
        @JsonProperty("doc_type")
        private String docType;
        
        private String language;
        
        public Metadata() {}
        
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
}

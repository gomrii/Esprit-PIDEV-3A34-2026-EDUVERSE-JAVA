package com.elearning.entity;

import java.time.LocalDateTime;

/**
 * GroqGeneratedContent — Stocke le contenu généré par Groq AI.
 * 
 * Utilisée pour :
 * - Tracer les descriptions générées
 * - Stocker les réponses de l'API
 * - Auditer les générations
 */
public class GroqGeneratedContent {
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    
    public static final String ENTITY_TYPE_FORMATION = "FORMATION";
    public static final String ENTITY_TYPE_RESSOURCE = "RESSOURCE";
    public static final String ENTITY_TYPE_USER = "USER";

    private int id;
    private String entityType;           // FORMATION, RESSOURCE, USER
    private int entityId;
    private String originalTitle;
    private String generatedDescription;
    private String language;             // fr, en, etc.
    private float temperature;           // 0.0 - 1.0
    private String status;               // PENDING, COMPLETED, FAILED
    private LocalDateTime createdAt;
    private String groqModel;            // llama-3.3-70b-versatile, etc.

    public GroqGeneratedContent() {
        this.language = "fr";
        this.temperature = 0.7f;
        this.status = STATUS_COMPLETED;
        this.createdAt = LocalDateTime.now();
        this.groqModel = "llama-3.3-70b-versatile";
    }

    public GroqGeneratedContent(String entityType, int entityId, String originalTitle, 
                                String generatedDescription) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.originalTitle = originalTitle;
        this.generatedDescription = generatedDescription;
        this.language = "fr";
        this.temperature = 0.7f;
        this.status = STATUS_COMPLETED;
        this.createdAt = LocalDateTime.now();
        this.groqModel = "llama-3.3-70b-versatile";
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getGeneratedDescription() {
        return generatedDescription;
    }

    public void setGeneratedDescription(String generatedDescription) {
        this.generatedDescription = generatedDescription;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGroqModel() {
        return groqModel;
    }

    public void setGroqModel(String groqModel) {
        this.groqModel = groqModel;
    }

    @Override
    public String toString() {
        return "GroqGeneratedContent{" +
                "id=" + id +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", originalTitle='" + originalTitle + '\'' +
                ", language='" + language + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

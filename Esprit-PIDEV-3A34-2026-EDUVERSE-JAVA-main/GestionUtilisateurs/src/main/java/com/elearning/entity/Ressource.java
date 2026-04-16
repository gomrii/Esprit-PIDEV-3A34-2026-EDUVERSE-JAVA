package com.elearning.entity;

import java.time.LocalDateTime;

/**
 * Entité Ressource — représente une ressource d'une formation.
 * Types disponibles: hardware, documentation, video, course, etc.
 */
public class Ressource {
    private int id;
    private String title;
    private String description;
    private String type;
    private String url;
    private String filePath;
    private Double cost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int formationId;
    private int createdById;
    private Integer updatedById;

    // Constructeur vide
    public Ressource() {}

    // Constructeur avec paramètres
    public Ressource(String title, String description, String type, String url, 
                     String filePath, Double cost, int formationId, int createdById) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.url = url;
        this.filePath = filePath;
        this.cost = cost;
        this.formationId = formationId;
        this.createdById = createdById;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getFormationId() {
        return formationId;
    }

    public void setFormationId(int formationId) {
        this.formationId = formationId;
    }

    public int getCreatedById() {
        return createdById;
    }

    public void setCreatedById(int createdById) {
        this.createdById = createdById;
    }

    public Integer getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Integer updatedById) {
        this.updatedById = updatedById;
    }

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", formationId=" + formationId +
                '}';
    }
}


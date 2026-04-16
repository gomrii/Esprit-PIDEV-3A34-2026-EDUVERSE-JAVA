package com.elearning.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Formation {
    private int id;
    private String title;
    private String description;
    private String content;
    private double price;
    private boolean isApproved;
    private boolean isArchived;
    private LocalDateTime createdAt;
    private String supportFile;
    private int duration;
    private String level;
    private int creatorId;
    private String creatorName;
    private List<Ressource> ressources;  // OneToMany relationship

    public Formation() {
        this.createdAt = LocalDateTime.now();
        this.isApproved = false;
        this.isArchived = false;
        this.ressources = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSupportFile() { return supportFile; }
    public void setSupportFile(String supportFile) { this.supportFile = supportFile; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public List<Ressource> getRessources() { return ressources; }
    public void setRessources(List<Ressource> ressources) { this.ressources = ressources; }
    public void addRessource(Ressource ressource) { this.ressources.add(ressource); }
}

package com.elearning.entity;

import java.time.LocalDateTime;

public class AIRecommendation {
    private int id;
    private String priorite;    // HAUTE, MOYENNE, FAIBLE
    private String type;        // APPROBATION, BLOCAGE, DEBLOCAGE, ALERTE, INFO
    private String titre;
    private String description;
    private String actionSuggeree;
    private boolean lue;        // marquée comme lue par l'admin
    private LocalDateTime createdAt;

    public AIRecommendation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActionSuggeree() { return actionSuggeree; }
    public void setActionSuggeree(String actionSuggeree) { this.actionSuggeree = actionSuggeree; }

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

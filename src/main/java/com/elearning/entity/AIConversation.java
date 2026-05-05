package com.elearning.entity;

import java.time.LocalDateTime;

/**
 * Entité représentant une conversation avec l'assistant IA.
 * Sauvegardée en BDD dans la table `ai_conversation`.
 */
public class AIConversation {

    private int           id;
    private int           adminId;
    private String        question;
    private String        reponse;
    private String        contextData;   // JSON des stats BDD au moment de la question
    private LocalDateTime createdAt;

    // -------------------------------------------------------
    // Constructeur vide
    // -------------------------------------------------------
    public AIConversation() {}

    // -------------------------------------------------------
    // Constructeur complet
    // -------------------------------------------------------
    public AIConversation(int id, int adminId, String question,
                          String reponse, String contextData,
                          LocalDateTime createdAt) {
        this.id          = id;
        this.adminId     = adminId;
        this.question    = question;
        this.reponse     = reponse;
        this.contextData = contextData;
        this.createdAt   = createdAt;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getAdminId()                     { return adminId; }
    public void setAdminId(int adminId)         { this.adminId = adminId; }

    public String getQuestion()                 { return question; }
    public void setQuestion(String question)    { this.question = question; }

    public String getReponse()                  { return reponse; }
    public void setReponse(String reponse)      { this.reponse = reponse; }

    public String getContextData()              { return contextData; }
    public void setContextData(String d)        { this.contextData = d; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)  { this.createdAt = dt; }

    @Override
    public String toString() {
        return "AIConversation{id=" + id + ", adminId=" + adminId
                + ", question='" + question + "'}";
    }
}

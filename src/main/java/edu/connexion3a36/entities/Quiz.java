package edu.connexion3a36.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Quiz {

    private int idQuiz;
    private String titre;
    private String statut;
    private String createdBy;
    private int duree;
    private String level;
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
    }

    public Quiz(String titre, String statut, String createdBy, int duree, String level) {
        this.titre = titre;
        this.statut = statut;
        this.createdBy = createdBy;
        this.duree = duree;
        this.level = level;
    }

    public Quiz(String titre, String statut, String createdBy) {
        this(titre, statut, createdBy, 0, null);
    }

    public Quiz(int idQuiz, String titre, String statut, String createdBy, int duree, String level) {
        this.idQuiz = idQuiz;
        this.titre = titre;
        this.statut = statut;
        this.createdBy = createdBy;
        this.duree = duree;
        this.level = level;
    }

    public Quiz(int idQuiz, String titre, String statut, String createdBy) {
        this(idQuiz, titre, statut, createdBy, 0, null);
    }

    public int getIdQuiz() {
        return idQuiz;
    }

    public void setIdQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    @Override
    public String toString() {
        return titre != null && !titre.isBlank() ? titre : "Quiz #" + idQuiz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quiz quiz = (Quiz) o;
        return idQuiz == quiz.idQuiz;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idQuiz);
    }
}

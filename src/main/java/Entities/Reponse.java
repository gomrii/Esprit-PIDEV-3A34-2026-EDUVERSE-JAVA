package Entities;

import java.util.Objects;

public class Reponse {

    private int idReponse;
    private String reponse;
    private double score;
    private int idQuestion;

    public Reponse() {}

    public Reponse(String reponse, double score, int idQuestion) {
        this.reponse = reponse;
        this.score = score;
        this.idQuestion = idQuestion;
    }

    public Reponse(int idReponse, String reponse, double score, int idQuestion) {
        this.idReponse = idReponse;
        this.reponse = reponse;
        this.score = score;
        this.idQuestion = idQuestion;
    }

    public int getIdReponse() { return idReponse; }
    public void setIdReponse(int idReponse) { this.idReponse = idReponse; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getIdQuestion() { return idQuestion; }
    public void setIdQuestion(int idQuestion) { this.idQuestion = idQuestion; }

    @Override
    public String toString() {
        return "Reponse{" +
                "idReponse=" + idReponse +
                ", reponse='" + reponse + '\'' +
                ", score=" + score +
                ", idQuestion=" + idQuestion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reponse reponse1 = (Reponse) o;
        return idReponse == reponse1.idReponse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReponse);
    }
}
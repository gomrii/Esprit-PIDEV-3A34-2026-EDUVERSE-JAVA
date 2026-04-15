package edu.connexion3a36.entities;

import java.util.Objects;

public class Question {

    private int idQuestion;
    private String question;
    private int idQuiz;

    public Question() {}

    public Question(String question, int idQuiz) {
        this.question = question;
        this.idQuiz = idQuiz;
    }

    public Question(int idQuestion, String question, int idQuiz) {
        this.idQuestion = idQuestion;
        this.question = question;
        this.idQuiz = idQuiz;
    }

    public int getIdQuestion() { return idQuestion; }
    public void setIdQuestion(int idQuestion) { this.idQuestion = idQuestion; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public int getIdQuiz() { return idQuiz; }
    public void setIdQuiz(int idQuiz) { this.idQuiz = idQuiz; }

    @Override
    public String toString() {
        return question != null && !question.isBlank() ? question : "Question #" + idQuestion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question1 = (Question) o;
        return idQuestion == question1.idQuestion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idQuestion);
    }
}

package edu.connexion3a36.services;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    private final Connection cnx;

    public QuizService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouterQuiz(Quiz quiz) throws SQLException {
        String requete = "INSERT INTO quiz (titre, statut, created_by) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, quiz.getTitre());
            pst.setString(2, quiz.getStatut());
            pst.setString(3, quiz.getCreatedBy());
            pst.executeUpdate();
        }
    }

    public List<Quiz> afficherQuiz() throws SQLException {
        String requete = "SELECT id_quiz, titre, statut, created_by FROM quiz";
        List<Quiz> quizzes = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setIdQuiz(rs.getInt("id_quiz"));
                quiz.setTitre(rs.getString("titre"));
                quiz.setStatut(rs.getString("statut"));
                quiz.setCreatedBy(rs.getString("created_by"));
                quizzes.add(quiz);
            }
        }

        return quizzes;
    }

    public void modifierQuiz(Quiz quiz) throws SQLException {
        String requete = "UPDATE quiz SET titre = ?, statut = ?, created_by = ? WHERE id_quiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, quiz.getTitre());
            pst.setString(2, quiz.getStatut());
            pst.setString(3, quiz.getCreatedBy());
            pst.setInt(4, quiz.getIdQuiz());
            pst.executeUpdate();
        }
    }

    public void supprimerQuiz(int id) throws SQLException {
        String requete = "DELETE FROM quiz WHERE id_quiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public void validerQuiz(int id) throws SQLException {
        String requete = "UPDATE quiz SET statut = ? WHERE id_quiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, "validé");
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }

    public void rejeterQuiz(int id) throws SQLException {
        String requete = "UPDATE quiz SET statut = ? WHERE id_quiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, "rejeté");
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }
}
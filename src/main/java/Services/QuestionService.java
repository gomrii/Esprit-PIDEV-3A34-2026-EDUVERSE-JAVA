package Services;

import Entities.Question;
import Utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    private final Connection cnx;

    public QuestionService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouterQuestion(Question question) throws SQLException {
        String requete = "INSERT INTO question (question, idQuiz) VALUES (?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, question.getQuestion());
            pst.setInt(2, question.getIdQuiz());
            pst.executeUpdate();
        }
    }

    public List<Question> afficherQuestion() throws SQLException {
        String requete = "SELECT idQuestion, question, idQuiz FROM question";
        List<Question> questions = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Question question = new Question();
                question.setIdQuestion(rs.getInt("idQuestion"));
                question.setQuestion(rs.getString("question"));
                question.setIdQuiz(rs.getInt("idQuiz"));
                questions.add(question);
            }
        }

        return questions;
    }

    public List<Question> afficherQuestionsByIdQuiz(int idQuiz) throws SQLException {
        String requete = "SELECT idQuestion, question, idQuiz FROM question WHERE idQuiz = ?";
        List<Question> questions = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, idQuiz);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question();
                    question.setIdQuestion(rs.getInt("idQuestion"));
                    question.setQuestion(rs.getString("question"));
                    question.setIdQuiz(rs.getInt("idQuiz"));
                    questions.add(question);
                }
            }
        }

        return questions;
    }

    public void modifierQuestion(Question question) throws SQLException {
        String requete = "UPDATE question SET question = ?, idQuiz = ? WHERE idQuestion = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, question.getQuestion());
            pst.setInt(2, question.getIdQuiz());
            pst.setInt(3, question.getIdQuestion());
            pst.executeUpdate();
        }
    }

    public void supprimerQuestion(int id) throws SQLException {
        String requete = "DELETE FROM question WHERE idQuestion = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
}

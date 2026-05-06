package Services;

import Entities.Reponse;
import Utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {

    private final Connection cnx;

    public ReponseService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouterReponse(Reponse reponse) throws SQLException {
        String requete = "INSERT INTO reponse (reponse, score, idQuestion) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, reponse.getReponse());
            pst.setDouble(2, reponse.getScore());
            pst.setInt(3, reponse.getIdQuestion());
            pst.executeUpdate();
        }
    }

    public List<Reponse> afficherReponse() throws SQLException {
        String requete = "SELECT idReponse, reponse, score, idQuestion FROM reponse";
        List<Reponse> reponses = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setIdReponse(rs.getInt("idReponse"));
                reponse.setReponse(rs.getString("reponse"));
                reponse.setScore(rs.getDouble("score"));
                reponse.setIdQuestion(rs.getInt("idQuestion"));
                reponses.add(reponse);
            }
        }

        return reponses;
    }

    public List<Reponse> afficherReponsesByIdQuestion(int idQuestion) throws SQLException {
        String requete = "SELECT idReponse, reponse, score, idQuestion FROM reponse WHERE idQuestion = ?";
        List<Reponse> reponses = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, idQuestion);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Reponse reponse = new Reponse();
                    reponse.setIdReponse(rs.getInt("idReponse"));
                    reponse.setReponse(rs.getString("reponse"));
                    reponse.setScore(rs.getDouble("score"));
                    reponse.setIdQuestion(rs.getInt("idQuestion"));
                    reponses.add(reponse);
                }
            }
        }

        return reponses;
    }

    public void modifierReponse(Reponse reponse) throws SQLException {
        String requete = "UPDATE reponse SET reponse = ?, score = ?, idQuestion = ? WHERE idReponse = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, reponse.getReponse());
            pst.setDouble(2, reponse.getScore());
            pst.setInt(3, reponse.getIdQuestion());
            pst.setInt(4, reponse.getIdReponse());
            pst.executeUpdate();
        }
    }

    public void supprimerReponse(int id) throws SQLException {
        String requete = "DELETE FROM reponse WHERE idReponse = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
}

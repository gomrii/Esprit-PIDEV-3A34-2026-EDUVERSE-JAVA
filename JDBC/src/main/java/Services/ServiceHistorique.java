package Services;

import Entities.Historique;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHistorique {

    public Connection conn;

    public ServiceHistorique(){
        conn = MyDb.getInstance().getConn();
    }

    public void ajouterAuHistorique(int idCours) {
        String req = "INSERT INTO historique (id_cours, date_consultation) VALUES (?, NOW())";
        try {
            PreparedStatement ps = conn.prepareStatement(req);
            ps.setInt(1, idCours);
            ps.executeUpdate();
            System.out.println("Cours " + idCours + " ajouté à l'historique.");
        } catch (SQLException e) {
            System.err.println("Erreur historique : " + e.getMessage());
        }
    }


    // Ajoute ceci dans ta classe ServiceHistorique
    public List<Historique> getHistorique() {
        List<Historique> list = new ArrayList<>();
        // On fait une jointure pour récupérer le titre du cours en même temps
        String req = "SELECT h.id, h.id_cours, h.date_consultation, c.title " +
                "FROM historique h " +
                "JOIN cours c ON h.id_cours = c.id " +
                "ORDER BY h.date_consultation DESC"; // Le plus récent en premier
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Historique h = new Historique(
                        rs.getInt("id"),
                        rs.getInt("id_cours"),
                        rs.getTimestamp("date_consultation")
                );

                list.add(h);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération historique : " + e.getMessage());
        }
        return list;
    }
}

package Services;

import Entities.Chapitre;
import Interfaces.Iservice;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceChapitre implements Iservice<Chapitre> {

    private Connection conn;

    public ServiceChapitre() {
        // Initialisation de la connexion via le Singleton MyDb
        conn = MyDb.getInstance().getConn();
    }

    /**
     * AJOUTER un chapitre
     */
    @Override
    public void add(Chapitre chapitre) throws SQLException {
        String req = "INSERT INTO chapitre (title, contenu, video, pdf, cours_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement prste = conn.prepareStatement(req);
        prste.setString(1, chapitre.getTitle());
        prste.setString(2, chapitre.getContenu());
        prste.setString(3, chapitre.getVideo());
        prste.setString(4, chapitre.getPdf());
        prste.setInt(5, chapitre.getCours_id());
        prste.executeUpdate();
        System.out.println("Chapitre ajouté avec succès !");
    }

    /**
     * SUPPRIMER un chapitre
     */
    @Override
    public void delete(Chapitre chapitre) throws SQLException {
        String req = "DELETE FROM chapitre WHERE id = ?";
        PreparedStatement prste = conn.prepareStatement(req);
        prste.setInt(1, chapitre.getId());
        prste.executeUpdate();
        System.out.println("Chapitre supprimé !");
    }

    /**
     * MODIFIER un chapitre
     */
    @Override
    public void update(Chapitre chapitre) throws SQLException {
        String req = "UPDATE chapitre SET title=?, contenu=?, video=?, pdf=?, cours_id=? WHERE id=?";
        PreparedStatement prste = conn.prepareStatement(req);
        prste.setString(1, chapitre.getTitle());
        prste.setString(2, chapitre.getContenu());
        prste.setString(3, chapitre.getVideo());
        prste.setString(4, chapitre.getPdf());
        prste.setInt(5, chapitre.getCours_id());
        prste.setInt(6, chapitre.getId());
        prste.executeUpdate();
        System.out.println("Chapitre mis à jour avec succès !");
    }

    /**
     * AFFICHER tous les chapitres
     */
    @Override
    public List<Chapitre> display() throws SQLException {
        String sql = "SELECT * FROM chapitre";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<Chapitre> chapitreList = new ArrayList<>();

        while (rs.next()) {
            Chapitre c = new Chapitre();
            c.setId(rs.getInt("id"));
            c.setTitle(rs.getString("title"));
            c.setContenu(rs.getString("contenu"));
            c.setVideo(rs.getString("video"));
            c.setPdf(rs.getString("pdf"));
            c.setCours_id(rs.getInt("cours_id"));
            chapitreList.add(c);
        }
        return chapitreList;
    }

    /**
     * RÉCUPÉRER les chapitres d'un cours spécifique (Filtre)
     */
    public List<Chapitre> getChapitresByCoursId(int coursId) throws SQLException {
        String sql = "SELECT * FROM chapitre WHERE cours_id = ?";
        PreparedStatement prste = conn.prepareStatement(sql);
        prste.setInt(1, coursId);
        ResultSet rs = prste.executeQuery();
        List<Chapitre> chapitreList = new ArrayList<>();

        while (rs.next()) {
            Chapitre c = new Chapitre();
            c.setId(rs.getInt("id"));
            c.setTitle(rs.getString("title"));
            c.setContenu(rs.getString("contenu"));
            c.setVideo(rs.getString("video"));
            c.setPdf(rs.getString("pdf"));
            c.setCours_id(rs.getInt("cours_id"));
            chapitreList.add(c);
        }
        return chapitreList;
    }
}
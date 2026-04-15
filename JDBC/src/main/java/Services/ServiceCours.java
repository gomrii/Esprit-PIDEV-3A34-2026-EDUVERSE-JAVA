package Services;

import Entities.Cours;
import Entities.Chapitre;
import Interfaces.Iservice;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCours implements Iservice <Cours> {

    public Connection conn;

    public ServiceCours(){
        conn = MyDb.getInstance().getConn();
    }

    @Override
    public void add(Cours cours) throws SQLException {
        String req = "INSERT into cours(title,category,descrption,image,level,status,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement prste = conn.prepareStatement(req);
        prste.setString(1, cours.getTitle());
        prste.setString(2, cours.getCategory());
        prste.setString(3, cours.getDescrption());
        prste.setString(4, cours.getImage());
        prste.setString(5, cours.getLevel());
        prste.setString(6, cours.getStatus());
        prste.setDate(7, new java.sql.Date(cours.getCreated_at().getTime()));
        prste.setDate(8, new java.sql.Date(cours.getUpdated_at().getTime()));
        prste.executeUpdate();
    }

    @Override
    public void delete(Cours cours) throws SQLException {
        String req = "DELETE from cours WHERE id = ?";
        PreparedStatement prste = conn.prepareStatement(req);
        prste.setInt(1, cours.getId());
        prste.executeUpdate();
    }

    @Override
    public void update(Cours cours) throws SQLException {
        String req = "UPDATE cours SET title=?,category=?,descrption=?,image=?,level=?,status=?,created_at=?,updated_at=? WHERE id=?";
        PreparedStatement prste = conn.prepareStatement(req);
        prste.setString(1, cours.getTitle());
        prste.setString(2, cours.getCategory());
        prste.setString(3, cours.getDescrption());
        prste.setString(4, cours.getImage());
        prste.setString(5, cours.getLevel());
        prste.setString(6, cours.getStatus());
        prste.setDate(7, new java.sql.Date(cours.getCreated_at().getTime()));
        prste.setDate(8, new java.sql.Date(cours.getUpdated_at().getTime()));
        prste.setInt(9, cours.getId());
        prste.executeUpdate();
    }

    @Override
    public List<Cours> display() throws SQLException {
        String sql = "select * from cours";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<Cours> coursList = new ArrayList<>();
        while (rs.next()) {
            Cours c = new Cours();
            c.setId(rs.getInt("id"));
            c.setTitle(rs.getString("title"));
            c.setCategory(rs.getString("category"));
            c.setDescrption(rs.getString("descrption"));
            c.setImage(rs.getString("image"));
            c.setLevel(rs.getString("level"));
            c.setStatus(rs.getString("status"));
            c.setCreated_at(rs.getDate("created_at"));
            c.setUpdated_at(rs.getDate("updated_at"));
            coursList.add(c);
        }
        return coursList;
    }

    // Méthode pour récupérer les chapitres d'un cours
    public List<Chapitre> getChapitresByCours(Cours cours) throws SQLException {
        ServiceChapitre serviceChapitre = new ServiceChapitre();
        return serviceChapitre.getChapitresByCoursId(cours.getId());
    }
    public void updateStatus(int id, String newStatus) throws SQLException {
        String req = "UPDATE cours SET status = ?, updated_at = ? WHERE id = ?";
        PreparedStatement prste = conn.prepareStatement(req);
        prste.setString(1, newStatus);
        // On met à jour la date de modification par la même occasion
        prste.setDate(2, new java.sql.Date(System.currentTimeMillis()));
        prste.setInt(3, id);
        prste.executeUpdate();
    }
    public List<Cours> search(String query) throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE title LIKE ? OR category LIKE ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        String searchPattern = "%" + query + "%";
        pstmt.setString(1, searchPattern);
        pstmt.setString(2, searchPattern);

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            Cours c = new Cours();
            c.setId(rs.getInt("id"));
            c.setTitle(rs.getString("title"));
            c.setCategory(rs.getString("category"));
            c.setDescrption(rs.getString("descrption"));
            c.setImage(rs.getString("image"));
            c.setLevel(rs.getString("level"));
            c.setStatus(rs.getString("status"));
            c.setCreated_at(rs.getDate("created_at"));
            c.setUpdated_at(rs.getDate("updated_at"));
            coursList.add(c);
        }
        return coursList;
    }
}

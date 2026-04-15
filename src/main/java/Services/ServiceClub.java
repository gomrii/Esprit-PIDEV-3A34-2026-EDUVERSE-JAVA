package Services;

import Entities.Club;
import Interfaces.IService;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceClub implements IService<Club> {

    private Connection conn;

    public ServiceClub() {
        conn = MyDb.getInstance().getConn();
    }

    @Override
    public void add(Club club) throws SQLException {
        String req = "INSERT INTO club (name, description, status, created_at, updated_at, creator_id) VALUES (?, ?, ?, NOW(), NOW(), ?)";
        PreparedStatement ps = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, club.getName());
        ps.setString(2, club.getDescription());
        ps.setString(3, club.getStatus());
        ps.setInt(4, club.getCreatorId());
        ps.executeUpdate();
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            club.setId(generatedKeys.getInt(1));
        }
        System.out.println("Club ajouté avec succès ! (id=" + club.getId() + ")");
    }

    @Override
    public void update(Club club) throws SQLException {
        String req = "UPDATE club SET name=?, description=?, status=?, updated_at=NOW() WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, club.getName());
        ps.setString(2, club.getDescription());
        ps.setString(3, club.getStatus());
        ps.setInt(4, club.getId());
        ps.executeUpdate();
        System.out.println("Club mis à jour avec succès !");
    }

    @Override
    public void delete(Club club) throws SQLException {
        String req = "DELETE FROM club WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, club.getId());
        ps.executeUpdate();
        System.out.println("Club supprimé avec succès !");
    }

    @Override
    public List<Club> display() throws SQLException {
        String req = "SELECT * FROM club";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Club> clubs = new ArrayList<>();
        while (rs.next()) {
            Club c = new Club();
            c.setId(rs.getInt("id"));
            c.setName(rs.getString("name"));
            c.setDescription(rs.getString("description"));
            c.setStatus(rs.getString("status"));
            c.setCreatedAt(rs.getDate("created_at"));
            c.setUpdatedAt(rs.getDate("updated_at"));
            c.setCreatorId(rs.getInt("creator_id"));
            clubs.add(c);
        }
        return clubs;
    }
}

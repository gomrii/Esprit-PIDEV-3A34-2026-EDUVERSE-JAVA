package Services;

import Entities.Club;
import Interfaces.IService;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceClub implements IService<Club> {

    public ServiceClub() {
    }

    @Override
    public void add(Club club) throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
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
        Connection conn = MyDb.getInstance().getConn();
        if (conn == null) throw new SQLException("Connexion BDD perdue");
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
        Connection conn = MyDb.getInstance().getConn();
        if (conn == null) throw new SQLException("Connexion BDD perdue");
        String req = "DELETE FROM club WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, club.getId());
        ps.executeUpdate();
        System.out.println("Club supprimé avec succès !");
    }

    @Override
    public List<Club> display() throws SQLException {
        String req = "SELECT * FROM club";
        return getClubsFromQuery(req);
    }

    public List<Club> displayApproved() throws SQLException {
        String req = "SELECT * FROM club WHERE status = 'APPROVED'";
        return getClubsFromQuery(req);
    }

    public List<Club> getPendingClubs() throws SQLException {
        String req = "SELECT * FROM club WHERE status = 'PENDING'";
        return getClubsFromQuery(req);
    }

    public List<Club> getClubsByCreator(int creatorId) throws SQLException {
        String req = "SELECT * FROM club WHERE creator_id = " + creatorId;
        return getClubsFromQuery(req);
    }

    public void validateClub(int id, String status) throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
        if (conn == null) throw new SQLException("Connexion BDD perdue");
        String req = "UPDATE club SET status=?, updated_at=NOW() WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public List<String> getClubMembers(int clubId) throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
        String req = "SELECT u.full_name as username FROM user u JOIN club_membership m ON u.id = m.user_id WHERE m.club_id = ?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, clubId);
        ResultSet rs = ps.executeQuery();
        List<String> members = new ArrayList<>();
        while (rs.next()) {
            members.add(rs.getString("username"));
        }
        return members;
    }

    public List<Integer> getClubMemberIds(int clubId) throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
        // Requête simple SANS JOIN pour récupérer uniquement les IDs (uniquement les membres approuvés)
        String req = "SELECT user_id FROM club_membership WHERE club_id = ? AND status = 'APPROVED'";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, clubId);
        ResultSet rs = ps.executeQuery();
        List<Integer> memberIds = new ArrayList<>();
        while (rs.next()) {
            memberIds.add(rs.getInt("user_id"));
        }
        return memberIds;
    }

    public void requestJoinClub(int userId, int clubId) throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
        String req = "INSERT INTO club_membership (user_id, club_id, status, joined_at) VALUES (?, ?, 'PENDING', NOW())";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, userId);
        ps.setInt(2, clubId);
        ps.executeUpdate();
    }

    public List<String[]> getPendingJoinRequests() throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
        // Jointure pour avoir le nom de l'utilisateur et du club depuis la vraie table "user"
        String req = "SELECT m.user_id, m.club_id, u.full_name as username, c.name as club_name " +
                     "FROM club_membership m " +
                     "JOIN user u ON m.user_id = u.id " +
                     "JOIN club c ON m.club_id = c.id " +
                     "WHERE m.status = 'PENDING'";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<String[]> requests = new ArrayList<>();
        while (rs.next()) {
            requests.add(new String[]{
                rs.getString("user_id"), 
                rs.getString("club_id"), 
                rs.getString("username"), 
                rs.getString("club_name")
            });
        }
        return requests;
    }

    public void updateMembershipStatus(int userId, int clubId, String status) throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
        String req = "UPDATE club_membership SET status = ? WHERE user_id = ? AND club_id = ?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, status);
        ps.setInt(2, userId);
        ps.setInt(3, clubId);
        ps.executeUpdate();
    }

    private List<Club> getClubsFromQuery(String query) throws SQLException {
        Connection conn = MyDb.getInstance().getConn();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
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

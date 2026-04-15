package Services;

import Entities.Event;
import Interfaces.IService;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvent implements IService<Event> {

    private Connection conn;

    public ServiceEvent() {
        conn = MyDb.getInstance().getConn();
    }

    @Override
    public void add(Event event) throws SQLException {
        String req = "INSERT INTO event (title, description, event_date, location, status, created_at, updated_at, club_id, creator_id) VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setDate(3, new java.sql.Date(event.getEventDate().getTime()));
        ps.setString(4, event.getLocation());
        ps.setString(5, event.getStatus());
        ps.setInt(6, event.getClubId());
        ps.setInt(7, event.getCreatorId());
        ps.executeUpdate();
        System.out.println("Événement ajouté avec succès !");
    }

    @Override
    public void update(Event event) throws SQLException {
        String req = "UPDATE event SET title=?, description=?, event_date=?, location=?, status=?, updated_at=NOW() WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setDate(3, new java.sql.Date(event.getEventDate().getTime()));
        ps.setString(4, event.getLocation());
        ps.setString(5, event.getStatus());
        ps.setInt(6, event.getId());
        ps.executeUpdate();
        System.out.println("Événement mis à jour avec succès !");
    }

    @Override
    public void delete(Event event) throws SQLException {
        String req = "DELETE FROM event WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, event.getId());
        ps.executeUpdate();
        System.out.println("Événement supprimé avec succès !");
    }

    @Override
    public List<Event> display() throws SQLException {
        String req = "SELECT * FROM event";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Event> events = new ArrayList<>();
        while (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setTitle(rs.getString("title"));
            e.setDescription(rs.getString("description"));
            e.setEventDate(rs.getDate("event_date"));
            e.setLocation(rs.getString("location"));
            e.setStatus(rs.getString("status"));
            e.setCreatedAt(rs.getDate("created_at"));
            e.setUpdatedAt(rs.getDate("updated_at"));
            e.setClubId(rs.getInt("club_id"));
            e.setCreatorId(rs.getInt("creator_id"));
            events.add(e);
        }
        return events;
    }
}

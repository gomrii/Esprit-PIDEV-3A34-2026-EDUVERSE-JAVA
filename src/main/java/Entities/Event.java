package Entities;

import java.util.Date;

public class Event {
    private int id;
    private String title;
    private String description;
    private Date eventDate;
    private String location;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private int clubId;
    private int creatorId;

    public Event() {}

    public Event(String title, String description, Date eventDate, String location, String status, int clubId, int creatorId) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.status = status;
        this.clubId = clubId;
        this.creatorId = creatorId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public int getClubId() { return clubId; }
    public void setClubId(int clubId) { this.clubId = clubId; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", eventDate=" + eventDate +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", clubId=" + clubId +
                ", creatorId=" + creatorId +
                '}';
    }
}

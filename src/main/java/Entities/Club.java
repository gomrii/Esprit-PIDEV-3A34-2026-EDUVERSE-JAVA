package Entities;

import java.util.Date;

public class Club {
    private int id;
    private String name;
    private String description;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private int creatorId;

    public Club() {}

    public Club(String name, String description, String status, int creatorId) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.creatorId = creatorId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    @Override
    public String toString() {
        return "Club{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", creatorId=" + creatorId +
                '}';
    }
}

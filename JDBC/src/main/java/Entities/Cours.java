package Entities;

import java.util.Date;
import java.util.Objects;

public class Cours {

    private Integer id;
    private String title;
    private String category;
    private String descrption;
    private String image;
    private String level;
    private String status;
    private Date created_at;
    private Date updated_at;

    public Cours() {
    }
    public Cours(String title, String category, String descrption,
                 String image, String level, String status,
                 Date created_at, Date updated_at) {

        this.title = title;
        this.category = category;
        this.descrption = descrption;
        this.image = image;
        this.level = level;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
    public Cours(String title, String category, String descrption,
                 String image, String level) {

        this.title = title;
        this.category = category;
        this.descrption = descrption;
        this.image = image;
        this.level = level;

    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescrption() {
        return descrption;
    }

    public void setDescrption(String descrption) {
        this.descrption = descrption;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cours cours)) return false;
        return id == cours.id && Objects.equals(title, cours.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", descrption='" + descrption + '\'' +
                ", image='" + image + '\'' +
                ", level='" + level + '\'' +
                ", status='" + status + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}

package Entities;

public class Chapitre {
    private int id;
    private String title;
    private String contenu;
    private String video;
    private String pdf;
    private int cours_id;

    // Constructeur vide
    public Chapitre() {
    }
    // Constructeur avec paramètres
    public Chapitre(int id, String title, String contenu, String video, String pdf, int cours_id) {
        this.id = id;
        this.title = title;
        this.contenu = contenu;
        this.video = video;
        this.pdf = pdf;
        this.cours_id = cours_id;
    }

    // Getters & Setters
    public int getId() {
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

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public int getCours_id() {
        return cours_id;
    }

    public void setCours_id(int cours_id) {
        this.cours_id = cours_id;
    }


}

package Entities;

import java.sql.Timestamp;

public class Historique {
    private int id;
    private int id_cours;
    private Timestamp date;
    private String titreCours; // Utile pour l'affichage plus tard

    public Historique(int id, int id_cours, Timestamp date) {
        this.id = id;
        this.id_cours = id_cours;
        this.date = date;
    }

    // Getters et Setters
    public int getId() { return id; }
    public int getId_cours() { return id_cours; }
    public Timestamp getDate() { return date; }
}
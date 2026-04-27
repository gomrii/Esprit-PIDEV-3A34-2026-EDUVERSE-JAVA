package Entities;

import java.sql.Timestamp;

public class Historique {
    private int id;
    private int id_cours;
    private Timestamp date_consultation;
    private String titreCours; // AJOUT : pour stocker le nom du cours récupéré par JOIN

    public Historique(int id, int id_cours, Timestamp date_consultation) {
        this.id = id;
        this.id_cours = id_cours;
        this.date_consultation = date_consultation;
    }

    // --- AJOUT DES GETTERS/SETTERS POUR LE TITRE ---
    public String getTitreCours() { return titreCours; }
    public void setTitreCours(String titreCours) { this.titreCours = titreCours; }

    // Garde tes autres getters/setters existants
    public int getId() { return id; }
    public int getId_cours() { return id_cours; }
    public Timestamp getDate_consultation() { return date_consultation; }
}
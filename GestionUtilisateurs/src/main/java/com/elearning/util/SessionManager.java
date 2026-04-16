package com.elearning.util;

import com.elearning.entity.User;

/**
 * SessionManager — Gère l'utilisateur connecté.
 *
 *
 * Singleton, car il n'y a qu'une seule session utilisateur à la fois.
 * Stocke l'utilisateur connecté pendant toute la durée de l'application.
 */
public class SessionManager {

    private static SessionManager instance;
    private User utilisateurConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void connecter(User user) {
        this.utilisateurConnecte = user;
    }

    public void deconnecter() {
        this.utilisateurConnecte = null;
    }

    public User getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public boolean estAdmin() {
        return estConnecte() && User.ROLE_ADMIN.equals(utilisateurConnecte.getRole());
    }

    public boolean estEnseignant() {
        return estConnecte() && User.ROLE_ENSEIGNANT.equals(utilisateurConnecte.getRole());
    }

    public boolean estEtudiant() {
        return estConnecte() && User.ROLE_ETUDIANT.equals(utilisateurConnecte.getRole());
    }
}


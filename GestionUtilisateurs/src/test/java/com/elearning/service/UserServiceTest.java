package com.elearning.service;

import com.elearning.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour UserService.
 *
 * On teste ici UNIQUEMENT la logique de validation (pas la BDD).
 * Les tests de BDD nécessiteraient une base de test dédiée (H2 in-memory).
 *
 * Pourquoi tester le Service et pas le DAO ?
 *   → Le DAO ne contient que des requêtes SQL, difficiles à tester sans BDD.
 *   → Le Service contient la logique métier : c'est ce qui a de la valeur à tester.
 *
 */
class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        // On instancie le service sans connexion BDD
        // (les méthodes de validation ne touchent pas la BDD)
        service = new UserService() {
            // On surcharge emailExiste() pour éviter l'appel BDD
            // (technique du "stub" manuel)
        };
    }

    // ================================================================
    //  Tests de validation du nom
    // ================================================================

    @Test
    @DisplayName("Nom vide → erreur 'obligatoire'")
    void nomVide_doitRetournerErreur() {
        List<String> erreurs = service.validerCreation("", "test@test.tn", "Valid1!", "ETUDIANT");
        assertTrue(erreurs.stream().anyMatch(e -> e.contains("nom complet est obligatoire")),
                "Devrait signaler que le nom est obligatoire");
    }

    @Test
    @DisplayName("Nom trop court → erreur '3 caractères'")
    void nomTropCourt_doitRetournerErreur() {
        List<String> erreurs = service.validerCreation("Ab", "test@test.tn", "Valid1!", "ETUDIANT");
        assertTrue(erreurs.stream().anyMatch(e -> e.contains("3 caractères")));
    }

    // ================================================================
    //  Tests de validation de l'email
    // ================================================================

    @Test
    @DisplayName("Email invalide → erreur de format")
    void emailInvalide_doitRetournerErreur() {
        List<String> erreurs = service.validerCreation("Ahmed Ben Ali", "pas-un-email", "Valid1!", "ETUDIANT");
        assertTrue(erreurs.stream().anyMatch(e -> e.contains("n'est pas valide")));
    }

    @Test
    @DisplayName("Email vide → erreur 'obligatoire'")
    void emailVide_doitRetournerErreur() {
        List<String> erreurs = service.validerCreation("Ahmed Ben Ali", "", "Valid1!", "ETUDIANT");
        assertTrue(erreurs.stream().anyMatch(e -> e.contains("e-mail est obligatoire")));
    }

    @Test
    @DisplayName("Email valide → pas d'erreur de format")
    void emailValide_pasDErreurFormat() {
        List<String> erreurs = service.validerCreation("Ahmed Ben Ali", "ahmed@elearning.tn",
                "Valid@1abc", "ETUDIANT");
        assertFalse(erreurs.stream().anyMatch(e -> e.contains("n'est pas valide")));
    }

    // ================================================================
    //  Tests de validation du mot de passe
    // ================================================================

    @Test
    @DisplayName("Mot de passe faible → erreur de sécurité")
    void motDePasseFaible_doitRetournerErreur() {
        // Pas de majuscule
        List<String> err1 = service.validerCreation("Ahmed", "a@b.tn", "password1!", "ETUDIANT");
        assertTrue(err1.stream().anyMatch(e -> e.contains("majuscule")));

        // Pas de chiffre
        List<String> err2 = service.validerCreation("Ahmed", "a@b.tn", "Password!", "ETUDIANT");
        assertTrue(err2.stream().anyMatch(e -> e.contains("chiffre")));

        // Trop court
        List<String> err3 = service.validerCreation("Ahmed", "a@b.tn", "Ab1!", "ETUDIANT");
        assertTrue(err3.stream().anyMatch(e -> e.contains("8 caractères")));
    }

    @Test
    @DisplayName("Mot de passe fort → pas d'erreur")
    void motDePasseFort_pasDErreur() {
        // Respecte : 8+ chars, majuscule, chiffre, spécial
        List<String> erreurs = service.validerCreation("Ahmed Ben Ali", "ahmed@test.tn",
                "Secure@123", "ETUDIANT");
        assertFalse(erreurs.stream().anyMatch(e -> e.contains("mot de passe")));
    }

    // ================================================================
    //  Tests de validation du rôle
    // ================================================================

    @Test
    @DisplayName("Rôle invalide → erreur")
    void roleInvalide_doitRetournerErreur() {
        List<String> erreurs = service.validerCreation("Ahmed", "a@b.tn", "Secure@1", "CHEF");
        assertTrue(erreurs.stream().anyMatch(e -> e.contains("ADMIN, ENSEIGNANT ou ETUDIANT")));
    }

    @Test
    @DisplayName("Rôle ADMIN valide → pas d'erreur de rôle")
    void roleAdmin_estValide() {
        List<String> erreurs = service.validerCreation("Ahmed", "a@b.tn", "Secure@1", "ADMIN");
        assertFalse(erreurs.stream().anyMatch(e -> e.contains("rôle")));
    }

    // ================================================================
    //  Tests du hashage de mot de passe
    // ================================================================

    @Test
    @DisplayName("Hash + vérification : doit fonctionner")
    void hashEtVerification_doiventCorrespondre() {
        String plain  = "MonMotDePasse@1";
        String hashed = service.hasherMotDePasse(plain);

        assertNotNull(hashed);
        assertNotEquals(plain, hashed);                         // Le hash ≠ le texte clair
        assertTrue(hashed.contains(":"));                       // Format "sel:hash"
        assertTrue(service.verifierMotDePasse(plain, hashed));  // Vérification correcte
        assertFalse(service.verifierMotDePasse("mauvais", hashed)); // Mauvais mdp rejeté
    }

    @Test
    @DisplayName("Deux hash du même mot de passe sont différents (sel aléatoire)")
    void deuxHashMemeMotDePasse_doiventEtreDifferents() {
        String plain  = "MonMotDePasse@1";
        String hash1  = service.hasherMotDePasse(plain);
        String hash2  = service.hasherMotDePasse(plain);
        assertNotEquals(hash1, hash2, "Deux hash du même mot de passe doivent différer (sel aléatoire)");
    }

    // ================================================================
    //  Tests de validation du téléphone
    // ================================================================

    @Test
    @DisplayName("Téléphone tunisien valide → pas d'erreur")
    void telephoneTunisienValide_pasDErreur() {
        assertNull(service.validerTelephone("+21620123456"));
        assertNull(service.validerTelephone("20123456"));
        assertNull(service.validerTelephone(null));    // optionnel → null accepté
        assertNull(service.validerTelephone(""));      // optionnel → vide accepté
    }

    @Test
    @DisplayName("Téléphone invalide → erreur")
    void telephoneInvalide_doitRetournerErreur() {
        assertNotNull(service.validerTelephone("0612345678")); // format FR
        assertNotNull(service.validerTelephone("123"));         // trop court
    }
}


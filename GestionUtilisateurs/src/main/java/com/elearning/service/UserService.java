package com.elearning.service;

import com.elearning.dao.UserDAO;
import com.elearning.entity.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * UserService — Logique Métier.
 *
 * RÔLE : Valider les données, appliquer les règles métier,
 *        puis déléguer au DAO pour la persistance.
 *
 *                    + logique dans les Form Types + UserChecker.
 *
 * Le Service NE CONNAÎT PAS JavaFX (pas d'import javafx.*).
 * Il reste testable indépendamment de l'interface graphique.
 */
public class UserService {

    private final UserDAO userDAO;

    // Expression régulière pour valider un email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Expression régulière pour valider un numéro de téléphone tunisien (optionnel)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(\\+216)?[259][0-9]{7}$");

    // Mot de passe : au moins 8 cars, 1 majuscule, 1 chiffre, 1 caractère spécial
    private static final Pattern PASSWORD_STRONG =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,}$");

    public UserService() {
        this.userDAO = new UserDAO();
    }

    // ================================================================
    // ================================================================

    /**
     * Valide tous les champs pour la CRÉATION d'un utilisateur.
     * @return liste d'erreurs (vide = tout est valide)
     */
    public List<String> validerCreation(String fullName, String email,
                                         String password, String role) {
        List<String> erreurs = new ArrayList<>();

        // --- Champs obligatoires ---
        if (fullName == null || fullName.isBlank()) {
            erreurs.add("Le nom complet est obligatoire.");
        } else if (fullName.trim().length() < 3) {
            erreurs.add("Le nom doit contenir au moins 3 caractères.");
        } else if (fullName.trim().length() > 100) {
            erreurs.add("Le nom ne peut pas dépasser 100 caractères.");
        }

        // --- Validation email ---
        erreurs.addAll(validerEmail(email, 0));

        // --- Validation mot de passe ---
        if (password == null || password.isBlank()) {
            erreurs.add("Le mot de passe est obligatoire.");
        } else if (!PASSWORD_STRONG.matcher(password).matches()) {
            erreurs.add("Le mot de passe doit contenir au moins 8 caractères, " +
                        "une majuscule, un chiffre et un caractère spécial (!@#$...).");
        }

        // --- Validation rôle ---
        if (role == null || role.isBlank()) {
            erreurs.add("Le rôle est obligatoire.");
        } else if (!role.equals(User.ROLE_ADMIN) &&
                   !role.equals(User.ROLE_ENSEIGNANT) &&
                   !role.equals(User.ROLE_ETUDIANT)) {
            erreurs.add("Le rôle doit être ADMIN, ENSEIGNANT ou ETUDIANT.");
        }

        return erreurs;
    }

    /**
     * Valide pour la MODIFICATION (sans mot de passe obligatoire).
     */
    public List<String> validerModification(String fullName, String email, int userId, String role) {
        List<String> erreurs = new ArrayList<>();

        if (fullName == null || fullName.isBlank()) {
            erreurs.add("Le nom complet est obligatoire.");
        } else if (fullName.trim().length() < 3) {
            erreurs.add("Le nom doit contenir au moins 3 caractères.");
        }

        erreurs.addAll(validerEmail(email, userId));

        if (role == null || role.isBlank()) {
            erreurs.add("Le rôle est obligatoire.");
        }

        return erreurs;
    }

    /**
     * Valide l'email et vérifie l'unicité.
     * @param excludeId  0 pour création, userId pour modification
     */
    private List<String> validerEmail(String email, int excludeId) {
        List<String> erreurs = new ArrayList<>();

        if (email == null || email.isBlank()) {
            erreurs.add("L'adresse e-mail est obligatoire.");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            erreurs.add("L'adresse e-mail n'est pas valide (ex: nom@domaine.tn).");
        } else if (userDAO.emailExiste(email.trim().toLowerCase(), excludeId)) {
            erreurs.add("Cet e-mail est déjà utilisé par un autre compte.");
        }

        return erreurs;
    }

    /**
     * Valide le numéro de téléphone (si fourni).
     */
    public String validerTelephone(String phone) {
        if (phone == null || phone.isBlank()) return null; // optionnel
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Le numéro de téléphone n'est pas valide (ex: +21620123456 ou 20123456).";
        }
        return null;
    }

    // ================================================================
    //  CRUD — Délègue au DAO après validation
    // ================================================================

    /**
     * Inscription d'un nouvel utilisateur (par lui-même).
     * Différence avec creerUser : le compte est par défaut EN_ATTENTE d'approbation.
     */
    public User inscrireUser(String fullName, String email, String password, String role) throws ValidationException {
        // 1. Validation
        List<String> erreurs = validerCreation(fullName, email, password, role);
        if (!erreurs.isEmpty()) {
            throw new ValidationException(erreurs);
        }

        // 2. Créer l'entité
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(hasherMotDePasse(password));
        user.setRole(role);

        user.setApproved(false);
        user.setStatut(User.STATUT_EN_ATTENTE);

        // 4. Persister en BDD
        int newId = userDAO.ajouterUser(user);
        if (newId == -1) {
            throw new RuntimeException("Erreur lors de la sauvegarde en base de données.");
        }

        return user;
    }

    /**
     * Crée un utilisateur avec validation + hashage du mot de passe.
     *
     * @throws ValidationException si les données sont invalides
     */
    public User creerUser(String fullName, String email, String password,
                          String role, String phone, String bio) throws ValidationException {

        // 1. Validation
        List<String> erreurs = validerCreation(fullName, email, password, role);
        if (phone != null && !phone.isBlank()) {
            String errPhone = validerTelephone(phone);
            if (errPhone != null) erreurs.add(errPhone);
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationException(erreurs);
        }

        // 2. Créer l'entité
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(hasherMotDePasse(password));   // JAMAIS stocker en clair
        user.setRole(role);
        user.setPhoneNumber(phone != null ? phone.trim() : null);
        user.setBio(bio != null ? bio.trim() : null);

        // 3. Règle métier : un Admin crée un compte directement approuvé
        user.setApproved(true);
        user.setStatut(User.STATUT_ACTIF);

        // 4. Persister en BDD
        int newId = userDAO.ajouterUser(user);
        if (newId == -1) {
            throw new RuntimeException("Erreur lors de la sauvegarde en base de données.");
        }

        return user;
    }

    /**
     * Modifie un utilisateur existant.
     */
    public void modifierUser(User user, String fullName, String email,
                              String role, String phone, String bio) throws ValidationException {

        List<String> erreurs = validerModification(fullName, email, user.getId(), role);
        if (phone != null && !phone.isBlank()) {
            String errPhone = validerTelephone(phone);
            if (errPhone != null) erreurs.add(errPhone);
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationException(erreurs);
        }

        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setRole(role);
        user.setPhoneNumber(phone != null ? phone.trim() : null);
        user.setBio(bio != null ? bio.trim() : null);

        // Synchroniser le statut avec le flag isBlocked
        if (user.isBlocked()) {
            user.setStatut(User.STATUT_BLOQUE);
        } else if (user.isApproved()) {
            user.setStatut(User.STATUT_ACTIF);
        }

        if (!userDAO.modifierUser(user)) {
            throw new RuntimeException("Erreur lors de la mise à jour en base de données.");
        }
    }

    /**
     * Supprime un utilisateur.
     * Règle métier : un Admin ne peut pas se supprimer lui-même.
     */
    public void supprimerUser(int userId, int currentAdminId) throws ValidationException {
        if (userId == currentAdminId) {
            throw new ValidationException(List.of("Vous ne pouvez pas supprimer votre propre compte."));
        }
        if (!userDAO.supprimerUser(userId)) {
            throw new RuntimeException("Impossible de supprimer cet utilisateur.");
        }
    }

    /**
     * Délègue les autres opérations au DAO.
     */
    public List<User> rechercherUsers(String search, String roleFiltre,
                                       String sortCol, String sortDir) {
        return userDAO.rechercherUsers(search, roleFiltre, sortCol, sortDir);
    }

    public boolean approuverUser(int id)    { return userDAO.approuverUser(id); }
    public boolean toggleBloquer(int id)    { return userDAO.toggleBloquer(id); }
    public User trouverParId(int id)        { return userDAO.trouverParId(id); }

    // ================================================================
    //  AUTHENTIFICATION
    // ================================================================

    /**
     * Vérifie les identifiants de connexion.
     *
     * @return le User si les identifiants sont corrects, null sinon
     * @throws ValidationException avec un message explicite selon le cas
     */
    public User authentifier(String email, String motDePasse) throws ValidationException {

        // 1. Validation basique des champs
        if (email == null || email.isBlank()) {
            throw new ValidationException(List.of("L'e-mail est obligatoire."));
        }
        if (motDePasse == null || motDePasse.isBlank()) {
            throw new ValidationException(List.of("Le mot de passe est obligatoire."));
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException(List.of("Format d'e-mail invalide."));
        }

        // 2. Chercher l'utilisateur
        User user = userDAO.trouverParEmail(email.trim().toLowerCase());
        if (user == null) {
            throw new ValidationException(List.of("Aucun compte n'existe avec cet e-mail."));
        }

        // 3. Vérifier le mot de passe
        if (!verifierMotDePasse(motDePasse, user.getPassword())) {
            throw new ValidationException(List.of("Mot de passe incorrect."));
        }

        if (user.isBlocked()) {
            throw new ValidationException(List.of("Votre compte est bloqué. Contactez l'administrateur."));
        }
        if (!user.isApproved()) {
            throw new ValidationException(List.of("Votre compte est en attente d'approbation."));
        }

        return user;
    }

    // ================================================================
    //  STATISTIQUES
    // ================================================================

    public int compterEtudiants()    { return userDAO.compterParRole(User.ROLE_ETUDIANT); }
    public int compterEnseignants()  { return userDAO.compterParRole(User.ROLE_ENSEIGNANT); }
    public int compterAdmins()       { return userDAO.compterParRole(User.ROLE_ADMIN); }
    public int compterActifs()       { return userDAO.compterParStatut(User.STATUT_ACTIF); }
    public int compterBloques()      { return userDAO.compterParStatut(User.STATUT_BLOQUE); }
    public int compterEnAttente()    { return userDAO.compterParStatut(User.STATUT_EN_ATTENTE); }
    public int compterTotal()        { return userDAO.compterTotal(); }

    // ================================================================
    //  HASHAGE DU MOT DE PASSE (SHA-256 + sel)
    // ================================================================
    // Note : En production, utilisez BCrypt (jBCrypt library).
    // SHA-256 + sel est utilisé ici pour éviter une dépendance externe.

    /**
     * Hash le mot de passe avec SHA-256 + sel aléatoire.
     * Format stocké : "SEL:HASH" (le sel est stocké avec le hash)
     */
    public String hasherMotDePasse(String plainPassword) {
        try {
            // Générer un sel aléatoire (16 octets)
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            String saltStr = Base64.getEncoder().encodeToString(salt);

            // Hash = SHA-256(sel + mot_de_passe)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((saltStr + plainPassword).getBytes());
            String hash = Base64.getEncoder().encodeToString(md.digest());

            return saltStr + ":" + hash;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme SHA-256 non disponible.", e);
        }
    }

    /**
     * Vérifie si un mot de passe en clair correspond au hash stocké.
     */
    public boolean verifierMotDePasse(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) return false;

        try {
            String[] parts = storedHash.split(":", 2);
            String saltStr = parts[0];
            String expectedHash = parts[1];

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((saltStr + plainPassword).getBytes());
            String actualHash = Base64.getEncoder().encodeToString(md.digest());

            return actualHash.equals(expectedHash);

        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    // ================================================================
    //  Exception interne pour la validation
    // ================================================================

    /**
     * Exception levée quand les données ne respectent pas les règles.
     * Contient la liste de toutes les erreurs pour les afficher dans l'UI.
     */
    public static class ValidationException extends Exception {
        private final List<String> erreurs;

        public ValidationException(List<String> erreurs) {
            super(String.join("; ", erreurs));
            this.erreurs = erreurs;
        }

        public List<String> getErreurs() { return erreurs; }

        public String getMessageFormate() {
            return String.join("\n• ", erreurs);
        }
    }
}


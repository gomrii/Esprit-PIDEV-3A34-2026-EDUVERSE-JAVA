package com.elearning.dao;

import com.elearning.entity.User;
import com.elearning.util.DatabaseConnection;
import com.elearning.util.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * UserDAO — Data Access Object.
 *
 * RÔLE : Toute la communication avec la BDD pour la table `user`.
 *        Aucune logique métier ici, juste des requêtes SQL.
 *
 *
 * Pourquoi PreparedStatement ?
 *   → Protège contre les injections SQL (jamais de concaténation de chaînes dans une requête).
 *   → Plus performant car la requête est pré-compilée par MySQL.
 */
public class UserDAO {

    /** Raccourci pour obtenir la connexion partout dans ce DAO */
    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ================================================================
    //  CREATE — Ajouter un utilisateur
    // ================================================================

    /**
     * Insère un nouvel utilisateur en BDD.
     * @return l'ID auto-généré par MySQL, ou -1 en cas d'erreur.
     *
     */
    public int ajouterUser(User user) {
        String sql = "INSERT INTO user (full_name, email, password, role, statut, " +
                     "is_approved, is_blocked, phone_number, bio, picture, created_at, " +
                     "roles, is_rejected, is_verified, updated_at, created_by_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());          // déjà haché par UserService
            ps.setString(4, roleJavaVersSymfony(user.getRole()));
            ps.setString(5, user.getStatut());
            ps.setBoolean(6, user.isApproved());
            ps.setBoolean(7, user.isBlocked());
            ps.setString(8, user.getPhoneNumber());       // peut être null
            ps.setString(9, user.getBio());               // peut être null
            ps.setString(10, user.getPicture());           // peut être null

            Timestamp now = Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now());
            ps.setTimestamp(11, now);

            String roleJson = switch (user.getRole() != null ? user.getRole() : "") {
                case User.ROLE_ADMIN      -> "[\"ROLE_ADMIN\"]";
                case User.ROLE_ENSEIGNANT -> "[\"ROLE_INSTRUCTOR\"]";
                default                   -> "[\"ROLE_STUDENT\"]";
            };
            ps.setString(12, roleJson);      // roles
            ps.setBoolean(13, false);        // is_rejected
            ps.setBoolean(14, false);        // is_verified
            ps.setTimestamp(15, now);        // updated_at

            // Fix 1 : utiliser l'ID de l'admin connecte au lieu d'un entier hardcodé
            int adminId = SessionManager.getInstance().getUtilisateurConnecte() != null
                          ? SessionManager.getInstance().getUtilisateurConnecte().getId()
                          : 1;
            ps.setInt(16, adminId);          // created_by_id

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID auto-généré par MySQL
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        user.setId(newId);
                        return newId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajouterUser : " + e.getMessage());
        }
        return -1;
    }

    // ================================================================
    //  READ — Lire les utilisateurs
    // ================================================================

    /**
     * Retourne TOUS les utilisateurs (sans filtre).
     */
    public List<User> afficherTousLesUsers() {
        return rechercherUsers("", "", "id", "ASC");
    }

    /**
     * Retourne un utilisateur par son ID.
     */
    public User trouverParId(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur trouverParId : " + e.getMessage());
        }
        return null;
    }

    /**
     * Retourne un utilisateur par son email (utile pour le login).
     */
    public User trouverParEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur trouverParEmail : " + e.getMessage());
        }
        return null;
    }

    /**
     * Recherche + filtre + tri — méthode principale pour l'affichage.
     *
     * @param searchText  texte libre (cherche dans nom et email)
     * @param roleFiltre  filtre par rôle ("" = tous)
     * @param sortColumn  colonne de tri (whitelist appliquée)
     * @param sortDir     "ASC" ou "DESC"
     *
     *
     * SÉCURITÉ : la colonne de tri est whitelistée pour éviter
     * les injections SQL via noms de colonnes (on ne peut pas binder
     * un nom de colonne avec PreparedStatement).
     */
    public List<User> rechercherUsers(String searchText, String roleFiltre,
                                       String sortColumn, String sortDir) {

        // --- Whitelist des colonnes triables (protection injection SQL) ---
        Map<String, String> allowedSorts = new LinkedHashMap<>();
        allowedSorts.put("id",         "id");
        allowedSorts.put("full_name",  "full_name");
        allowedSorts.put("email",      "email");
        allowedSorts.put("role",       "role");
        allowedSorts.put("statut",     "statut");
        allowedSorts.put("created_at", "created_at");

        String safeColumn = allowedSorts.getOrDefault(sortColumn, "id");
        String safeDir    = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        // --- Construction dynamique de la requête ---
        StringBuilder sql = new StringBuilder("SELECT * FROM user WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.isBlank()) {
            sql.append(" AND (full_name LIKE ? OR email LIKE ?)");
            params.add("%" + searchText + "%");
            params.add("%" + searchText + "%");
        }

        if (roleFiltre != null && !roleFiltre.isBlank()) {
            sql.append(" AND role = ?");
            params.add(roleFiltre);
        }

        sql.append(" ORDER BY ").append(safeColumn).append(" ").append(safeDir);

        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            // Injecter les paramètres dynamiquement
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur rechercherUsers : " + e.getMessage());
        }
        return users;
    }

    // ================================================================
    //  UPDATE — Modifier un utilisateur
    // ================================================================

    /**
     * Met à jour les informations d'un utilisateur existant.
     *
     * @return true si la mise à jour a réussi.
     */
    public boolean modifierUser(User user) {
        String sql = "UPDATE user SET full_name=?, email=?, role=?, statut=?, " +
                     "is_approved=?, is_blocked=?, phone_number=?, bio=?, picture=?, roles=? " +
                     "WHERE id=?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, roleJavaVersSymfony(user.getRole()));
            ps.setString(4, user.getStatut());
            ps.setBoolean(5, user.isApproved());
            ps.setBoolean(6, user.isBlocked());
            ps.setString(7, user.getPhoneNumber());
            ps.setString(8, user.getBio());
            ps.setString(9, user.getPicture());
            
            String roleJson = switch (user.getRole() != null ? user.getRole() : "") {
                case User.ROLE_ADMIN      -> "[\"ROLE_ADMIN\"]";
                case User.ROLE_ENSEIGNANT -> "[\"ROLE_INSTRUCTOR\"]";
                default                   -> "[\"ROLE_STUDENT\"]";
            };
            ps.setString(10, roleJson);
            ps.setInt(11, user.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur modifierUser : " + e.getMessage());
            return false;
        }
    }

    /**
     * Change le mot de passe uniquement (opération séparée pour la sécurité).
     */
    public boolean changerMotDePasse(int userId, String hashedPassword) {
        String sql = "UPDATE user SET password=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur changerMotDePasse : " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggle bloquer/débloquer un utilisateur.
     */
    public boolean toggleBloquer(int userId) {
        String sql = "UPDATE user SET is_blocked = NOT is_blocked, " +
                     "statut = IF(is_blocked = 1, 'BLOQUE', 'ACTIF'), " +
                     "login_attempts = 0, locked_until = NULL WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur toggleBloquer : " + e.getMessage());
            return false;
        }
    }

    /**
     * Approuver un utilisateur en attente.
     */
    public boolean approuverUser(int userId) {
        String sql = "UPDATE user SET is_approved=1, statut='ACTIF' WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur approuverUser : " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    //  DELETE — Supprimer un utilisateur
    // ================================================================

    /**
     * Supprime un utilisateur par son ID.
     */
    public boolean supprimerUser(int id) {
        String sql = "DELETE FROM user WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur supprimerUser : " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    //  STATISTIQUES — Pour le dashboard
    // ================================================================

    /**
     * Compte le total d'utilisateurs par rôle.
     */
    public int compterParRole(String role) {
        String sql = "SELECT COUNT(*) FROM user WHERE role=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur compterParRole : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Compte par statut (ACTIF / BLOQUE / EN_ATTENTE).
     */
    public int compterParStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM user WHERE statut=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur compterParStatut : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Total général.
     */
    public int compterTotal() {
        String sql = "SELECT COUNT(*) FROM user";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ Erreur compterTotal : " + e.getMessage());
        }
        return 0;
    }

    // ================================================================
    //  VÉRIFICATIONS UNICITÉ
    // ================================================================

    /**
     * Vérifie si un email est déjà utilisé (test d'unicité).
     * Utilisé par UserService avant d'ajouter ou modifier.
     *
     * @param email   l'email à vérifier
     * @param excludeId  l'ID à exclure (utile lors d'une modification, pour ne pas bloquer l'utilisateur lui-même)
     */
    public boolean emailExiste(String email, int excludeId) {
        String sql = "SELECT COUNT(*) FROM user WHERE email=? AND id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur emailExiste : " + e.getMessage());
        }
        return false;
    }

    // ================================================================
    //  2FA — Codes OTP
    // ================================================================

    /**
     * Sauvegarde le code OTP généré et sa date d'expiration en BDD.
     */
    public boolean sauvegarderOTP(int userId, String code, LocalDateTime expiration) {
        String sql = "UPDATE user SET two_factor_code=?, two_factor_expires_at=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setTimestamp(2, Timestamp.valueOf(expiration));
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarderOTP : " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie le code OTP : bon code, non expiré.
     * Si OK, efface le code en BDD (usage unique).
     */
    public boolean verifierEtConsommerOTP(int userId, String code) {
        String sqlSelect = "SELECT two_factor_code, two_factor_expires_at FROM user WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sqlSelect)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored  = rs.getString("two_factor_code");
                    Timestamp exp  = rs.getTimestamp("two_factor_expires_at");
                    boolean valid  = code != null && code.equals(stored)
                                  && exp != null
                                  && exp.toLocalDateTime().isAfter(LocalDateTime.now());
                    if (valid) {
                        // Effacer le code (usage unique)
                        String sqlClear = "UPDATE user SET two_factor_code=NULL, two_factor_expires_at=NULL WHERE id=?";
                        try (PreparedStatement ps2 = getConn().prepareStatement(sqlClear)) {
                            ps2.setInt(1, userId);
                            ps2.executeUpdate();
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur verifierEtConsommerOTP : " + e.getMessage());
        }
        return false;
    }

    /**
     * Active ou désactive la 2FA pour un utilisateur.
     */
    public boolean toggleTwoFactor(int userId, boolean enabled) {
        String sql = "UPDATE user SET is_two_factor_enabled=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, enabled);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur toggleTwoFactor : " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    //  VERROUILLAGE DE COMPTE
    // ================================================================

    /**
     * Incrémente le compteur d'échecs et verrouille le compte si nécessaire.
     *
     * @param userId       ID de l'utilisateur
     * @param maxAttempts  seuil de verrouillage (ex: 5)
     * @param lockMinutes  durée du verrouillage en minutes (ex: 15)
     * @return nombre de tentatives restantes avant verrouillage (ou 0 si verrouillé)
     */
    public int incrementerEchecEtVerrouiller(int userId, int maxAttempts, int lockMinutes) {
        // Incrémenter
        String sqlInc = "UPDATE user SET login_attempts = login_attempts + 1 WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sqlInc)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur incrementérEchec : " + e.getMessage());
        }

        // Lire le compteur mis à jour
        String sqlRead = "SELECT login_attempts FROM user WHERE id=?";
        int attempts = 0;
        try (PreparedStatement ps = getConn().prepareStatement(sqlRead)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) attempts = rs.getInt("login_attempts");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture login_attempts : " + e.getMessage());
        }

        // Verrouiller si seuil atteint
        if (attempts >= maxAttempts) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(lockMinutes);
            String sqlLock = "UPDATE user SET locked_until=? WHERE id=?";
            try (PreparedStatement ps = getConn().prepareStatement(sqlLock)) {
                ps.setTimestamp(1, Timestamp.valueOf(lockUntil));
                ps.setInt(2, userId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("❌ Erreur verrouillage : " + e.getMessage());
            }
            return 0;
        }
        return maxAttempts - attempts;
    }

    /**
     * Réinitialise le compteur d'échecs et déverrouille le compte après connexion réussie.
     */
    public void reinitialiserVerrouillage(int userId) {
        String sql = "UPDATE user SET login_attempts=0, locked_until=NULL WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur reinitialiserVerrouillage : " + e.getMessage());
        }
    }

    // ================================================================
    //  MÉTHODE PRIVÉE — Mapper un ResultSet vers un objet User
    // ================================================================

    /**
     * Convertit une ligne de ResultSet en objet User.
     * Factorisation : utilisée dans toutes les méthodes de lecture.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(roleSymfonyVersJava(rs.getString("role")));
        u.setStatut(rs.getString("statut"));
        u.setApproved(rs.getBoolean("is_approved"));
        u.setBlocked(rs.getBoolean("is_blocked"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setBio(rs.getString("bio"));
        u.setPicture(rs.getString("picture"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());

        // Nouveaux champs 2FA (peuvent être absents si colonne pas encore ajoutée)
        try {
            u.setTwoFactorEnabled(rs.getBoolean("is_two_factor_enabled"));
            u.setTwoFactorCode(rs.getString("two_factor_code"));
            Timestamp tfExp = rs.getTimestamp("two_factor_expires_at");
            if (tfExp != null) u.setTwoFactorExpiresAt(tfExp.toLocalDateTime());
        } catch (SQLException ignored) { /* colonne pas encore migrée */ }

        // Champs verrouillage
        try {
            u.setLoginAttempts(rs.getInt("login_attempts"));
            Timestamp locked = rs.getTimestamp("locked_until");
            if (locked != null) u.setLockedUntil(locked.toLocalDateTime());
        } catch (SQLException ignored) { /* colonne pas encore migrée */ }

        return u;
    }

    // ================================================================
    //  MÉTHODES DE MAPPING RÔLES (Compatibilité Symfony)
    // ================================================================

    // Java → Symfony (pour INSERT et UPDATE)
    private String roleJavaVersSymfony(String roleJava) {
        return switch (roleJava) {
            case User.ROLE_ADMIN      -> "Admin";
            case User.ROLE_ENSEIGNANT -> "Instructor";
            case User.ROLE_ETUDIANT   -> "Student";
            default                   -> roleJava;
        };
    }

    // Symfony → Java (pour SELECT dans mapResultSetToUser)
    private String roleSymfonyVersJava(String roleSymfony) {
        return switch (roleSymfony != null ? roleSymfony : "") {
            case "Admin"      -> User.ROLE_ADMIN;
            case "Instructor" -> User.ROLE_ENSEIGNANT;
            case "Student"    -> User.ROLE_ETUDIANT;
            default           -> roleSymfony;
        };
    }
}


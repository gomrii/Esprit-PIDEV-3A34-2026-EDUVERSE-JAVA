package com.elearning.dao;

import com.elearning.entity.User;
import com.elearning.util.DatabaseConnection;

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
 * Équivalent Symfony : UserRepository (qui étend ServiceEntityRepository).
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
     * Équivalent Symfony : $em->persist($user); $em->flush();
     */
    public int ajouterUser(User user) {
        // Ajout des colonnes Symfony obligatoires : roles, is_rejected, is_verified, updated_at, created_by_id
        String sql = "INSERT INTO user (full_name, email, password, role, statut, " +
                     "is_approved, is_blocked, phone_number, bio, picture, created_at, " +
                     "roles, is_rejected, is_verified, updated_at, created_by_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());          // déjà haché par UserService
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatut());
            ps.setBoolean(6, user.isApproved());
            ps.setBoolean(7, user.isBlocked());
            ps.setString(8, user.getPhoneNumber());       // peut être null
            ps.setString(9, user.getBio());               // peut être null
            ps.setString(10, user.getPicture());           // peut être null

            Timestamp now = Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now());
            ps.setTimestamp(11, now);

            // Remplissage des champs Symfony manquants pour ne pas avoir d'erreur SQL
            String roleJson = user.getRole() != null && user.getRole().equals(User.ROLE_ENSEIGNANT) ? "[\"ROLE_INSTRUCTOR\"]" : "[\"ROLE_STUDENT\"]";
            ps.setString(12, roleJson);      // roles
            ps.setBoolean(13, false);        // is_rejected
            ps.setBoolean(14, false);        // is_verified
            ps.setTimestamp(15, now);        // updated_at
            ps.setInt(16, 1);                // created_by_id (admin par defaut 1)

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
     * Équivalent Symfony : $userRepository->findAll()
     */
    public List<User> afficherTousLesUsers() {
        return rechercherUsers("", "", "id", "ASC");
    }

    /**
     * Retourne un utilisateur par son ID.
     * Équivalent Symfony : $userRepository->find($id)  ou  User $user (ParamConverter)
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
     * Équivalent Symfony : $userRepository->findOneByEmail($email)
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
     * Équivalent Symfony : UserRepository::findUsersByRoleAndStatus() avec QueryBuilder
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
     * Équivalent Symfony : $em->flush() (l'entité est déjà "managed")
     *
     * @return true si la mise à jour a réussi.
     */
    public boolean modifierUser(User user) {
        String sql = "UPDATE user SET full_name=?, email=?, role=?, statut=?, " +
                     "is_approved=?, is_blocked=?, phone_number=?, bio=?, picture=? " +
                     "WHERE id=?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getStatut());
            ps.setBoolean(5, user.isApproved());
            ps.setBoolean(6, user.isBlocked());
            ps.setString(7, user.getPhoneNumber());
            ps.setString(8, user.getBio());
            ps.setString(9, user.getPicture());
            ps.setInt(10, user.getId());

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
     * Équivalent Symfony : AdminController::toggleBlock()
     */
    public boolean toggleBloquer(int userId) {
        String sql = "UPDATE user SET is_blocked = NOT is_blocked, " +
                     "statut = IF(is_blocked = 1, 'BLOQUE', 'ACTIF') WHERE id=?";
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
     * Équivalent Symfony : $em->remove($user); $em->flush();
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
     * Équivalent Symfony : UserRepository::countByRole()
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
    //  MÉTHODE PRIVÉE — Mapper un ResultSet vers un objet User
    // ================================================================

    /**
     * Convertit une ligne de ResultSet en objet User.
     * Factorisation : utilisée dans toutes les méthodes de lecture.
     *
     * Équivalent Symfony : Doctrine le fait automatiquement (hydratation).
     * En JDBC on doit le faire manuellement.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));
        u.setApproved(rs.getBoolean("is_approved"));
        u.setBlocked(rs.getBoolean("is_blocked"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setBio(rs.getString("bio"));
        u.setPicture(rs.getString("picture"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());

        return u;
    }
}

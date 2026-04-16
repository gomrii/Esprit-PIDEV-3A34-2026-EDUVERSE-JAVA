package com.elearning.service;

import com.elearning.dao.FormationDAO;
import com.elearning.entity.Formation;
import com.elearning.entity.User;
import com.elearning.util.SessionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormationService {
    private final FormationDAO formationDAO = new FormationDAO();

    public List<Formation> findAll() {
        return formationDAO.findAll();
    }

    public List<Formation> rechercherFormations(String searchText, String levelFilter, String sortColumn, String sortDir) {
        return formationDAO.rechercherFormations(searchText, levelFilter, sortColumn, sortDir);
    }

    public List<Formation> findAvailableForStudent() {
        return formationDAO.findAvailableForStudent();
    }

    public List<Formation> findForTeacherView() {
        return formationDAO.findForTeacherView();
    }

    public List<Formation> searchAvailableForStudent(String searchText, String levelFilter, String sortColumn, String sortDir) {
        return formationDAO.searchAvailableForStudent(searchText, levelFilter, sortColumn, sortDir);
    }

    public List<Formation> searchForTeacherView(String searchText, String levelFilter, String sortColumn, String sortDir) {
        return formationDAO.searchForTeacherView(searchText, levelFilter, sortColumn, sortDir);
    }

    public List<Formation> searchEnrolledForStudent(int studentId, String searchText, String levelFilter, String sortColumn, String sortDir) {
        return formationDAO.searchEnrolledForStudent(studentId, searchText, levelFilter, sortColumn, sortDir);
    }

    public boolean approveFormation(int formationId) {
        return formationDAO.setApprovalState(formationId, true, false);
    }

    public boolean disapproveFormation(int formationId) {
        return formationDAO.setApprovalState(formationId, false, true);
    }

    public boolean enrollStudent(int formationId, int studentId) {
        return formationDAO.enrollStudent(formationId, studentId);
    }

    public boolean isStudentEnrolled(int formationId, int studentId) {
        return formationDAO.isStudentEnrolled(formationId, studentId);
    }

    public List<Map<String, String>> findEnrolledStudentsForTeacher(int teacherId) {
        return formationDAO.findEnrolledStudentsForTeacher(teacherId);
    }

    public List<Map<String, String>> getEnrolledStudentsForFormation(int formationId) {
        return formationDAO.getEnrolledStudentsForFormation(formationId);
    }

    public Formation create(String title, String description, String content, String priceStr,
                            String durationStr, String level, String supportFile, boolean approved,
                            boolean archived) throws ValidationException {
        List<String> errors = validate(title, description, content, priceStr, durationStr, level, supportFile);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Formation formation = new Formation();
        formation.setTitle(title.trim());
        formation.setDescription(description.trim());
        formation.setContent(content.trim());
        formation.setPrice(Double.parseDouble(priceStr.trim()));
        formation.setDuration(Integer.parseInt(durationStr.trim()));
        formation.setLevel(level.trim());
        formation.setSupportFile(supportFile != null ? supportFile.trim() : null);
        User current = SessionManager.getInstance().getUtilisateurConnecte();
        if (current != null && User.ROLE_ENSEIGNANT.equals(current.getRole())) {
            // Toute creation enseignant doit rester en attente de validation admin.
            formation.setApproved(false);
            formation.setArchived(false);
        } else {
            formation.setApproved(approved);
            formation.setArchived(archived);
        }
        formation.setCreatedAt(LocalDateTime.now());

        int id = formationDAO.create(formation);
        if (id == -1) {
            throw new RuntimeException("Creation de formation echouee.");
        }
        formation.setId(id);
        return formation;
    }

    public void update(Formation formation, String title, String description, String content, String priceStr,
                       String durationStr, String level, String supportFile, boolean approved,
                       boolean archived) throws ValidationException {
        List<String> errors = validate(title, description, content, priceStr, durationStr, level, supportFile);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        formation.setTitle(title.trim());
        formation.setDescription(description.trim());
        formation.setContent(content.trim());
        formation.setPrice(Double.parseDouble(priceStr.trim()));
        formation.setDuration(Integer.parseInt(durationStr.trim()));
        formation.setLevel(level.trim());
        formation.setSupportFile(supportFile != null ? supportFile.trim() : null);
        formation.setApproved(approved);
        formation.setArchived(archived);

        if (!formationDAO.update(formation)) {
            throw new RuntimeException("Modification de formation echouee.");
        }
    }

    public boolean delete(int id) {
        return formationDAO.delete(id);
    }

    private List<String> validate(String title, String description, String content, String priceStr,
                                  String durationStr, String level, String supportFile) {
        List<String> errors = new ArrayList<>();

        if (title == null || title.isBlank()) {
            errors.add("Le titre est obligatoire.");
        }
        if (description == null || description.isBlank()) {
            errors.add("La description est obligatoire.");
        }
        if (content == null || content.isBlank()) {
            errors.add("Le contenu est obligatoire.");
        }
        if (level == null || level.isBlank()) {
            errors.add("Le niveau est obligatoire.");
        }

        try {
            double price = Double.parseDouble(priceStr.trim());
            if (price < 0) {
                errors.add("Le prix doit etre >= 0.");
            }
        } catch (Exception e) {
            errors.add("Le prix doit etre numerique.");
        }

        try {
            int duration = Integer.parseInt(durationStr.trim());
            if (duration <= 0) {
                errors.add("La duree doit etre > 0.");
            }
        } catch (Exception e) {
            errors.add("La duree doit etre un entier.");
        }

        if (supportFile != null && !supportFile.isBlank()) {
            String lower = supportFile.trim().toLowerCase();
            if (!lower.endsWith(".pdf")) {
                errors.add("Le support doit etre un fichier PDF (.pdf).");
            }
        }

        return errors;
    }

    // ==================== RESSOURCES METHODS ====================

    private final RessourceService ressourceService = new RessourceService();

    /**
     * Create a ressource for a formation
     */
    public int createRessource(String title, String description, String url, String type, 
                               String filePath, Double cost, int formationId)
            throws RessourceService.ValidationException {
        return ressourceService.create(title, description, url, type, filePath, cost, formationId);
    }

    /**
     * Get all ressources for a formation
     */
    public List<com.elearning.entity.Ressource> getRessourcesForFormation(int formationId) {
        return ressourceService.findByFormationId(formationId);
    }

    /**
     * Update a ressource
     */
    public boolean updateRessource(int id, String title, String description, String url, String type, 
                                   String filePath, Double cost)
            throws RessourceService.ValidationException {
        return ressourceService.update(id, title, description, url, type, filePath, cost);
    }

    /**
     * Delete a ressource
     */
    public boolean deleteRessource(int id) {
        return ressourceService.delete(id);
    }

    /**
     * Delete all ressources for a formation (called when deleting formation)
     */
    public boolean deleteRessourcesForFormation(int formationId) {
        return ressourceService.deleteByFormationId(formationId);
    }

    public static class ValidationException extends Exception {
        private final List<String> errors;

        public ValidationException(List<String> errors) {
            super(String.join("; ", errors));
            this.errors = errors;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}

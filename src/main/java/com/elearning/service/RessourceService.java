package com.elearning.service;

import com.elearning.dao.RessourceDAO;
import com.elearning.entity.Ressource;
import com.elearning.util.SessionManager;
import com.elearning.entity.User;

import java.util.ArrayList;
import java.util.List;

public class RessourceService {
    private final RessourceDAO ressourceDAO = new RessourceDAO();

    /**
     * Create a new ressource with validation
     */
    public int create(String title, String description, String url, String type, 
                      String filePath, Double cost, int formationId)
            throws ValidationException {
        List<String> errors = validate(title, description, url, type);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        User current = SessionManager.getInstance().getUtilisateurConnecte();
        int createdById = (current != null && current.getId() > 0) ? current.getId() : 1;

        Ressource ressource = new Ressource(title.trim(), description.trim(), type.trim(), 
                                            url.trim(), filePath != null ? filePath.trim() : null,
                                            cost, formationId, createdById);

        return ressourceDAO.create(ressource);
    }

    /**
     * Get all ressources for a formation
     */
    public List<Ressource> findByFormationId(int formationId) {
        return ressourceDAO.findByFormationId(formationId);
    }

    /**
     * Get a specific ressource
     */
    public Ressource findById(int id) {
        return ressourceDAO.findById(id);
    }

    /**
     * Update a ressource
     */
    public boolean update(int id, String title, String description, String url, String type, 
                         String filePath, Double cost)
            throws ValidationException {
        List<String> errors = validate(title, description, url, type);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Ressource ressource = new Ressource();
        ressource.setId(id);
        ressource.setTitle(title.trim());
        ressource.setDescription(description.trim());
        ressource.setUrl(url.trim());
        ressource.setType(type.trim());
        ressource.setFilePath(filePath != null ? filePath.trim() : null);
        ressource.setCost(cost);

        return ressourceDAO.update(ressource);
    }

    /**
     * Delete a ressource
     */
    public boolean delete(int id) {
        return ressourceDAO.delete(id);
    }

    /**
     * Delete all ressources for a formation
     */
    public boolean deleteByFormationId(int formationId) {
        return ressourceDAO.deleteByFormationId(formationId);
    }

    /**
     * Validate ressource data
     */
    private List<String> validate(String title, String description, String url, String type) {
        List<String> errors = new ArrayList<>();

        if (title == null || title.trim().isEmpty()) {
            errors.add("Title is required");
        }
        if (description == null || description.trim().isEmpty()) {
            errors.add("Description is required");
        }
        if (url == null || url.trim().isEmpty()) {
            errors.add("URL is required");
        }
        if (type == null || type.trim().isEmpty()) {
            errors.add("Type is required");
        }

        return errors;
    }

    /**
     * Custom exception for validation errors
     */
    public static class ValidationException extends Exception {
        private final List<String> errors;

        public ValidationException(List<String> errors) {
            super(String.join("\n", errors));
            this.errors = errors;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}

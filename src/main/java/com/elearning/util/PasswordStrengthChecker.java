package com.elearning.util;

/**
 * Analyse la force d'un mot de passe en temps réel.
 *
 * Utilisé dans : RegisterController, UserFormController, ResetPasswordController.
 *
 * Critères évalués (1 point chacun) :
 *   1. Longueur ≥ 8 caractères
 *   2. Longueur ≥ 12 caractères (bonus)
 *   3. Contient au moins une MAJUSCULE
 *   4. Contient au moins un chiffre
 *   5. Contient au moins un caractère spécial (!@#$%^&*...)
 *   6. Ne contient aucun mot courant (password, 123456, azerty…)
 *
 * Score 0-1 → Très faible  (rouge   #e74c3c)
 * Score 2   → Faible       (orange  #e67e22)
 * Score 3   → Moyen        (jaune   #f39c12)
 * Score 4   → Fort         (vert    #2ecc71)
 * Score 5-6 → Très fort    (vert foncé #27ae60)
 */
public class PasswordStrengthChecker {

    // -------------------------------------------------------
    // Classe résultat
    // -------------------------------------------------------

    public static class PasswordStrengthResult {
        private final int    score;
        private final double progress;  // 0.0 → 1.0  (pour JavaFX ProgressBar)
        private final String libelle;   // Très faible / Faible / Moyen / Fort / Très fort
        private final String couleur;   // code hex CSS pour -fx-text-fill et -fx-accent

        public PasswordStrengthResult(int score, double progress,
                                      String libelle, String couleur) {
            this.score    = score;
            this.progress = progress;
            this.libelle  = libelle;
            this.couleur  = couleur;
        }

        public int    getScore()    { return score;    }
        public double getProgress() { return progress; }
        public String getLibelle()  { return libelle;  }
        public String getCouleur()  { return couleur;  }
    }

    // -------------------------------------------------------
    // Mots interdits (trop courants)
    // -------------------------------------------------------
    private static final String[] MOTS_INTERDITS = {
        "password", "123456", "azerty", "qwerty", "admin",
        "eduverse", "motdepasse", "abcdef", "111111", "letmein"
    };

    // -------------------------------------------------------
    // Méthode principale
    // -------------------------------------------------------

    /**
     * Analyse le mot de passe et retourne un résultat complet.
     *
     * @param password le mot de passe à analyser (peut être null ou vide)
     * @return PasswordStrengthResult avec score, progress, libellé et couleur
     */
    public static PasswordStrengthResult analyser(String password) {

        if (password == null || password.isEmpty()) {
            return new PasswordStrengthResult(0, 0.0, "", "transparent");
        }

        int score = 0;

        // Critère 1 : longueur ≥ 8
        if (password.length() >= 8)  score++;

        // Critère 2 : longueur ≥ 12 (bonus)
        if (password.length() >= 12) score++;

        // Critère 3 : au moins une majuscule
        if (password.matches(".*[A-Z].*")) score++;

        // Critère 4 : au moins un chiffre
        if (password.matches(".*\\d.*")) score++;

        // Critère 5 : au moins un caractère spécial
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?/~`].*")) score++;

        // Critère 6 : ne contient pas de mot courant
        String lower = password.toLowerCase();
        boolean motCourant = false;
        for (String mot : MOTS_INTERDITS) {
            if (lower.contains(mot)) {
                motCourant = true;
                break;
            }
        }
        if (!motCourant) score++;

        return switch (score) {
            case 0, 1 -> new PasswordStrengthResult(score, 0.15, "Très faible 🔴", "#e74c3c");
            case 2    -> new PasswordStrengthResult(score, 0.35, "Faible 🟠",      "#e67e22");
            case 3    -> new PasswordStrengthResult(score, 0.55, "Moyen 🟡",       "#f39c12");
            case 4    -> new PasswordStrengthResult(score, 0.75, "Fort 🟢",        "#2ecc71");
            default   -> new PasswordStrengthResult(score, 1.0,  "Très fort 💪",   "#27ae60");
        };
    }
}

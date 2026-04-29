package com.elearning.service;

import com.elearning.dao.UserDAO;
import com.elearning.entity.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

/**
 * Gère le flux complet de réinitialisation de mot de passe.
 *
 * Étape 1 : demanderReinitialisation(email) → génère token + envoie email
 * Étape 2 : reinitialiserMotDePasse(token, mdp, confirm) → valide + met à jour BDD
 *
 * Configuration SMTP : Mailtrap (sandbox) — remplacer MAILTRAP_USER / MAILTRAP_PASSWORD.
 */
public class PasswordResetService {

    // -------------------------------------------------------
    // Configuration SMTP Mailtrap
    // -------------------------------------------------------
    private static final String SMTP_HOST      = "sandbox.smtp.mailtrap.io";
    private static final String SMTP_PORT      = "2525";
    private static final String SMTP_USER      = "2f97723e1ccb24";
    private static final String SMTP_PASS      = "1fec793af3ff10";
    private static final String FROM_EMAIL     = "no-reply@eduverse.tn";
    private static final int    EXPIRY_MINUTES = 15;

    private final com.elearning.dao.PasswordResetDAO resetDAO = new com.elearning.dao.PasswordResetDAO();
    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService();

    // -------------------------------------------------------
    // ÉTAPE 1 : Demande de réinitialisation
    // -------------------------------------------------------

    /**
     * Génère un token sécurisé, le sauvegarde et l'envoie par email.
     *
     * @param email l'adresse email du compte
     * @throws UserService.ValidationException si email introuvable
     * @throws Exception                       si l'envoi SMTP échoue
     */
    public void demanderReinitialisation(String email) throws Exception {
        if (email == null || email.isBlank()) {
            throw new UserService.ValidationException(List.of("L'adresse email est obligatoire."));
        }

        User user = userDAO.trouverParEmail(email.trim().toLowerCase());
        if (user == null) {
            throw new UserService.ValidationException(
                    List.of("Aucun compte n'est associé à cette adresse email."));
        }

        // Générer un token sécurisé
        String token     = genererToken();
        LocalDateTime exp = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        // Sauvegarder en BDD
        resetDAO.sauvegarderToken(user.getId(), token, exp);

        // Envoyer l'email
        envoyerEmailReset(user.getEmail(), user.getFullName(), token);
    }

    // -------------------------------------------------------
    // ÉTAPE 2 : Validation et changement de mot de passe
    // -------------------------------------------------------

    /**
     * Valide le token, hash le nouveau mot de passe et met à jour la BDD.
     *
     * @param token         token reçu par email
     * @param nouveauMdp    nouveau mot de passe (en clair)
     * @param confirmation  confirmation du nouveau mot de passe
     * @throws UserService.ValidationException si token invalide ou mots de passe non conformes
     */
    public void reinitialiserMotDePasse(String token, String nouveauMdp,
                                         String confirmation) throws Exception {
        if (token == null || token.isBlank()) {
            throw new UserService.ValidationException(List.of("Le code de réinitialisation est obligatoire."));
        }

        // Valider le token en BDD
        int userId = resetDAO.validerToken(token.trim());
        if (userId == -1) {
            throw new UserService.ValidationException(
                    List.of("Code invalide ou expiré. Demandez un nouveau code."));
        }

        // Vérifier que les mots de passe correspondent
        if (nouveauMdp == null || nouveauMdp.isBlank()) {
            throw new UserService.ValidationException(List.of("Le nouveau mot de passe est obligatoire."));
        }
        if (!nouveauMdp.equals(confirmation)) {
            throw new UserService.ValidationException(List.of("Les mots de passe ne correspondent pas."));
        }

        // Vérifier la force du mot de passe
        com.elearning.util.PasswordStrengthChecker.PasswordStrengthResult force =
                com.elearning.util.PasswordStrengthChecker.analyser(nouveauMdp);
        if (force.getScore() < 3) {
            throw new UserService.ValidationException(
                    List.of("Le mot de passe est trop faible. Utilisez au moins 8 caractères, "
                          + "une majuscule, un chiffre et un caractère spécial."));
        }

        // Hash + mise à jour BDD
        String hashed = userService.hasherMotDePasse(nouveauMdp);
        new com.elearning.dao.UserDAO().changerMotDePasse(userId, hashed);

        // Consommer le token (usage unique)
        resetDAO.consommerToken(token.trim());
    }

    // -------------------------------------------------------
    // Génération du token
    // -------------------------------------------------------

    /** Génère un token hexadécimal sécurisé de 32 caractères. */
    private String genererToken() {
        byte[] bytes = new byte[16];
        new java.security.SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // -------------------------------------------------------
    // Envoi email SMTP (Mailtrap)
    // -------------------------------------------------------

    /**
     * Envoie l'email de réinitialisation contenant le token.
     */
    private void envoyerEmailReset(String destinataire, String nomUtilisateur,
                                    String token) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
        message.setSubject("🔑 Réinitialisation de votre mot de passe Eduverse");
        message.setContent(construireCorpsEmail(nomUtilisateur, token), "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("✅ Email de réinitialisation envoyé à : " + destinataire);
    }

    private String construireCorpsEmail(String nom, String token) {
        return """
            <html><body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 30px;">
              <div style="max-width: 500px; margin: auto; background: white;
                          border-radius: 12px; padding: 30px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                <h2 style="color: #2c3e50;">🔑 Réinitialisation de mot de passe</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Vous avez demandé la réinitialisation de votre mot de passe Eduverse.</p>
                <p>Votre code de réinitialisation est :</p>
                <div style="font-size: 28px; font-weight: bold; letter-spacing: 4px;
                            color: #3498db; text-align: center; padding: 15px;
                            background: #eef4fb; border-radius: 8px; margin: 20px 0;">
                  %s
                </div>
                <p>⏱ Ce code est valable <strong>15 minutes</strong>.</p>
                <p style="color: #7f8c8d; font-size: 13px;">
                  Si vous n'avez pas fait cette demande, ignorez cet email.
                  Votre mot de passe ne sera pas modifié.
                </p>
                <hr style="border: none; border-top: 1px solid #ecf0f1; margin: 20px 0;"/>
                <p style="color: #bdc3c7; font-size: 12px; text-align: center;">
                  Eduverse e-Learning Platform
                </p>
              </div>
            </body></html>
            """.formatted(nom, token);
    }
}

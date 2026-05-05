package com.elearning.service;

import com.elearning.dao.UserDAO;
import com.elearning.entity.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Génère, envoie et vérifie les codes OTP pour l'authentification à deux facteurs (2FA).
 *
 * Flux :
 *   1. Après login réussi, si user.isTwoFactorEnabled() : envoyerEtSauvegarder(user)
 *   2. L'utilisateur saisit le code reçu par email
 *   3. verifierCode(userId, code) → true = accès autorisé
 *
 * Config SMTP : Mailtrap (sandbox) — remplacer MAILTRAP_USER / MAILTRAP_PASSWORD.
 */
public class TwoFactorService {

    // -------------------------------------------------------
    // Configuration SMTP
    // -------------------------------------------------------
    private static final String SMTP_HOST      = "sandbox.smtp.mailtrap.io";
    private static final String SMTP_PORT      = "2525";
    private static final String SMTP_USER      = "2f97723e1ccb24";
    private static final String SMTP_PASS      = "1fec793af3ff10";
    private static final String FROM_EMAIL     = "no-reply@eduverse.tn";
    private static final int    EXPIRY_MINUTES = 5;

    private final UserDAO userDAO = new UserDAO();

    // -------------------------------------------------------
    // Génération du code OTP
    // -------------------------------------------------------

    /**
     * Génère un code OTP de 6 chiffres aléatoire.
     * Formaté avec zéros de tête si nécessaire.
     */
    public String genererCode() {
        return String.format("%06d", new java.util.Random().nextInt(1_000_000));
    }

    // -------------------------------------------------------
    // Envoi + sauvegarde
    // -------------------------------------------------------

    /**
     * Génère un code OTP, le sauvegarde en BDD et l'envoie par email.
     *
     * @param user l'utilisateur qui se connecte
     * @throws Exception si l'envoi SMTP échoue (l'application continue sans crash)
     */
    public void envoyerEtSauvegarder(User user) throws Exception {
        String code          = genererCode();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        // Sauvegarder le code en BDD
        userDAO.sauvegarderOTP(user.getId(), code, expiration);

        // Envoyer l'email
        envoyerEmail(user.getEmail(), user.getFullName(), code);
    }

    // -------------------------------------------------------
    // Vérification
    // -------------------------------------------------------

    /**
     * Vérifie si le code saisi est correct et non expiré.
     * Si valide, efface le code en BDD (usage unique).
     *
     * @param userId    ID de l'utilisateur
     * @param codeSaisi code saisi par l'utilisateur
     * @return true si le code est valide
     */
    public boolean verifierCode(int userId, String codeSaisi) {
        if (codeSaisi == null || codeSaisi.isBlank()) return false;
        return userDAO.verifierEtConsommerOTP(userId, codeSaisi.trim());
    }

    // -------------------------------------------------------
    // Toggle 2FA
    // -------------------------------------------------------

    /**
     * Active ou désactive la 2FA pour un utilisateur.
     *
     * @param userId  ID de l'utilisateur
     * @param enabled true pour activer, false pour désactiver
     * @return true si la mise à jour a réussi
     */
    public boolean toggleDeuxFacteurs(int userId, boolean enabled) {
        return userDAO.toggleTwoFactor(userId, enabled);
    }

    // -------------------------------------------------------
    // Envoi email SMTP
    // -------------------------------------------------------

    private void envoyerEmail(String destinataire, String nomUtilisateur,
                               String code) throws Exception {
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
        message.setSubject("🔐 Code de vérification Eduverse — " + code);
        message.setContent(construireCorpsEmail(nomUtilisateur, code), "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("✅ Code 2FA envoyé à : " + destinataire);
    }

    private String construireCorpsEmail(String nom, String code) {
        return """
            <html><body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 30px;">
              <div style="max-width: 480px; margin: auto; background: white;
                          border-radius: 12px; padding: 30px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                <h2 style="color: #2c3e50;">🔐 Vérification en deux étapes</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Votre code de connexion Eduverse est :</p>
                <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                            color: #3498db; text-align: center; padding: 18px;
                            background: #eef4fb; border-radius: 10px; margin: 20px 0;">
                  %s
                </div>
                <p>⏱ Ce code expire dans <strong>5 minutes</strong>.</p>
                <p>Ne partagez jamais ce code avec qui que ce soit.</p>
                <hr style="border: none; border-top: 1px solid #ecf0f1; margin: 20px 0;"/>
                <p style="color: #bdc3c7; font-size: 12px; text-align: center;">
                  Eduverse e-Learning Platform
                </p>
              </div>
            </body></html>
            """.formatted(nom, code);
    }
}

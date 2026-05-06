package com.elearning.service;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation d'email robuste — DNS MX + détection de patterns aléatoires.
 *
 * Étapes :
 *  1. Vérification du format strict (regex avancée)
 *  2. Détection d'adresse aléatoire (entropie élevée, consonnes répétées)
 *  3. Vérification domaine jetable
 *  4. Vérification enregistrement MX via DNS (le domaine peut-il recevoir des emails ?)
 */
public class EmailValidationService {

    public enum ResultatValidation { VALIDE, RISQUE, INVALIDE, ERREUR_API }

    // Format strict : pas de tiret au début/fin, pas de double point, TLD >= 2 chars
    private static final Pattern FORMAT_STRICT = Pattern.compile(
            "^[a-zA-Z0-9]([a-zA-Z0-9._%+\\-]{0,62}[a-zA-Z0-9])?@[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z]{2,})+$"
    );

    // Séquences de touches aléatoires typiques (QWERTY)
    private static final Pattern PATTERNS_ALEATOIRES = Pattern.compile(
            "(?i)(qwerty|asdf|zxcv|qazwsx|azerty|fdsa|hjkl|lkjh|poiuy|nbvcx|" +
            "aaaaaa|zzzzzz|xxxxxx|qqqqq|1234|abcd|abcde|test123|([a-z])\\2{3,})"
    );

    // Domaines jetables connus
    private static final Set<String> DOMAINES_JETABLES = Set.of(
            "mailinator.com", "guerrillamail.com", "tempmail.com", "throwaway.email",
            "yopmail.com", "sharklasers.com", "grr.la", "spam4.me", "trashmail.com",
            "trashmail.net", "dispostable.com", "maildrop.cc", "fakeinbox.com",
            "getairmail.com", "discard.email", "mailnull.com", "spamgourmet.com",
            "mintemail.com", "mailnesia.com", "trashmail.io", "getnada.com"
    );

    public ResultatValidation validerEmail(String email) {
        if (email == null || email.isBlank()) return ResultatValidation.INVALIDE;

        String emailTrim = email.trim().toLowerCase();

        // 1. Format strict
        if (!FORMAT_STRICT.matcher(emailTrim).matches()) {
            System.out.println("📧 Format invalide : " + emailTrim);
            return ResultatValidation.INVALIDE;
        }

        String localPart  = emailTrim.substring(0, emailTrim.indexOf('@'));
        String domain     = emailTrim.substring(emailTrim.indexOf('@') + 1);

        // 2. Détection de saisie aléatoire dans la partie locale
        if (semblerAleatoire(localPart)) {
            System.out.println("⚠ Adresse suspecte / aléatoire : " + localPart);
            return ResultatValidation.INVALIDE;
        }

        // 3. Domaine jetable
        if (DOMAINES_JETABLES.contains(domain)) {
            System.out.println("⚠ Domaine jetable : " + domain);
            return ResultatValidation.RISQUE;
        }

        // 4. Vérification MX via DNS
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "4000");
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            InitialDirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes("dns:/" + domain, new String[]{"MX", "A"});
            ctx.close();

            boolean mxExiste = attrs.get("MX") != null;
            boolean aExiste  = attrs.get("A")  != null;

            if (mxExiste) {
                System.out.println("✅ MX trouvé pour : " + domain);
                return ResultatValidation.VALIDE;
            } else if (aExiste) {
                System.out.println("⚠ Pas de MX mais domaine existe (A record) : " + domain);
                return ResultatValidation.RISQUE;
            } else {
                System.out.println("❌ Domaine inexistant : " + domain);
                return ResultatValidation.INVALIDE;
            }
        } catch (javax.naming.NameNotFoundException e) {
            System.out.println("❌ Domaine introuvable DNS : " + domain);
            return ResultatValidation.INVALIDE;
        } catch (Exception e) {
            System.out.println("⚠ DNS inaccessible pour " + domain + " : " + e.getMessage());
            return ResultatValidation.RISQUE; // Incertain, pas invalide
        }
    }

    /**
     * Détecte si la partie locale d'un email semble aléatoire/insensée :
     *  - Patterns QWERTY connus
     *  - Entropie très élevée (beaucoup de consonnes consécutives)
     *  - Trop courte ou caractères répétés
     */
    private boolean semblerAleatoire(String local) {
        if (local.length() < 2) return true;

        // Patterns claviers QWERTY connus
        if (PATTERNS_ALEATOIRES.matcher(local).find()) return true;

        // Compter les séquences de consonnes sans voyelles (ex: "qwrtp", "zxcvb")
        String consonnes = local.replaceAll("[^bcdfghjklmnpqrstvwxyz]", "");
        String voyelles  = local.replaceAll("[^aeiou]", "");
        double ratioVoyelles = voyelles.length() / (double) local.length();

        // Un email normal a au moins 20% de voyelles dans la partie locale
        if (ratioVoyelles < 0.15 && local.length() > 4) {
            System.out.println("⚠ Ratio voyelles trop faible (" + String.format("%.0f%%", ratioVoyelles * 100) + ") : " + local);
            return true;
        }

        // Détecter les séquences de 4+ consonnes identiques ou consécutives
        if (local.matches(".*([bcdfghjklmnpqrstvwxyz])\\1{3,}.*")) return true;

        return false;
    }
}

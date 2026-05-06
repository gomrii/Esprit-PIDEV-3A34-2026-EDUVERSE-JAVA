package com.elearning.entity;

import java.time.LocalDateTime;

/**
 * Entité User — POJO (Plain Old Java Object).
 *
 *
 * Contient UNIQUEMENT les données + getters/setters.
 * Aucune logique métier ici (c'est le rôle du Service).
 */
public class User {

    // -------------------------------------------------------
    // Rôles disponibles (constantes pour éviter les fautes de frappe)
    // -------------------------------------------------------
    public static final String ROLE_ADMIN      = "ADMIN";
    public static final String ROLE_ENSEIGNANT = "ENSEIGNANT";
    public static final String ROLE_ETUDIANT   = "ETUDIANT";

    // Statuts disponibles
    public static final String STATUT_ACTIF      = "ACTIF";
    public static final String STATUT_BLOQUE     = "BLOQUE";
    public static final String STATUT_EN_ATTENTE = "EN_ATTENTE";

    // -------------------------------------------------------
    // Attributs (correspondent aux colonnes de la table `user`)
    // -------------------------------------------------------
    private int id;
    private String fullName;       // full_name en BDD
    private String email;          // UNIQUE
    private String password;       // haché avec BCrypt
    private String role;           // ADMIN | ENSEIGNANT | ETUDIANT
    private String statut;         // ACTIF | BLOQUE | EN_ATTENTE
    private boolean isApproved;    // is_approved
    private boolean isBlocked;     // is_blocked
    private String phoneNumber;    // phone_number (nullable)
    private String bio;            // nullable
    private String picture;        // chemin ou URL photo (nullable)
    private LocalDateTime createdAt; // created_at

    // -------------------------------------------------------
    // Champs 2FA (two-factor authentication)
    // -------------------------------------------------------
    private boolean       isTwoFactorEnabled;    // is_two_factor_enabled
    private String        twoFactorCode;         // code OTP 6 chiffres (nullable)
    private LocalDateTime twoFactorExpiresAt;    // expiration du code OTP (nullable)

    // -------------------------------------------------------
    // Champs sécurité / verrouillage de compte
    // -------------------------------------------------------
    private int           loginAttempts;         // login_attempts
    private LocalDateTime lockedUntil;           // locked_until (nullable)

    // -------------------------------------------------------
    // Crédits / Paiements (Stripe integration)
    // -------------------------------------------------------
    private double credits = 0.0;               // credits balance for purchases

    // -------------------------------------------------------
    // Constructeur VIDE — nécessaire pour JavaFX TableView
    // et pour les lectures JDBC (on set les champs un par un)
    // -------------------------------------------------------
    public User() {
        this.statut             = STATUT_EN_ATTENTE;
        this.role               = ROLE_ETUDIANT;
        this.isApproved         = false;
        this.isBlocked          = false;
        this.createdAt          = LocalDateTime.now();
        this.isTwoFactorEnabled = false;
        this.loginAttempts      = 0;
    }

    /**
     * Constructeur COMPLET — utilisé lors de la lecture depuis la BDD.
     */
    public User(int id, String fullName, String email, String password,
                String role, String statut, boolean isApproved, boolean isBlocked,
                String phoneNumber, String bio, String picture, LocalDateTime createdAt) {
        this.id          = id;
        this.fullName    = fullName;
        this.email       = email;
        this.password    = password;
        this.role        = role;
        this.statut      = statut;
        this.isApproved  = isApproved;
        this.isBlocked   = isBlocked;
        this.phoneNumber = phoneNumber;
        this.bio         = bio;
        this.picture     = picture;
        this.createdAt   = createdAt;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getFullName()                 { return fullName; }
    public void setFullName(String fullName)    { this.fullName = fullName; }

    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }

    public String getPassword()                 { return password; }
    public void setPassword(String password)    { this.password = password; }

    public String getRole()                     { return role; }
    public void setRole(String role)            { this.role = role; }

    public String getStatut()                   { return statut; }
    public void setStatut(String statut)        { this.statut = statut; }

    public boolean isApproved()                 { return isApproved; }
    public void setApproved(boolean approved)   { isApproved = approved; }

    public boolean isBlocked()                  { return isBlocked; }
    public void setBlocked(boolean blocked)     { isBlocked = blocked; }

    public String getPhoneNumber()              { return phoneNumber; }
    public void setPhoneNumber(String p)        { this.phoneNumber = p; }

    public String getBio()                      { return bio; }
    public void setBio(String bio)              { this.bio = bio; }

    public String getPicture()                  { return picture; }
    public void setPicture(String picture)      { this.picture = picture; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)  { this.createdAt = dt; }

    // 2FA
    public boolean isTwoFactorEnabled()                          { return isTwoFactorEnabled; }
    public void setTwoFactorEnabled(boolean enabled)             { this.isTwoFactorEnabled = enabled; }

    public String getTwoFactorCode()                             { return twoFactorCode; }
    public void setTwoFactorCode(String code)                    { this.twoFactorCode = code; }

    public LocalDateTime getTwoFactorExpiresAt()                 { return twoFactorExpiresAt; }
    public void setTwoFactorExpiresAt(LocalDateTime dt)          { this.twoFactorExpiresAt = dt; }

    // Verrouillage
    public int getLoginAttempts()                                { return loginAttempts; }
    public void setLoginAttempts(int attempts)                   { this.loginAttempts = attempts; }

    public LocalDateTime getLockedUntil()                        { return lockedUntil; }
    public void setLockedUntil(LocalDateTime dt)                 { this.lockedUntil = dt; }

    // Credits / Paiements (Stripe integration)
    public double getCredits()                                   { return credits; }
    public void setCredits(double credits)                       { this.credits = credits; }
    
    /**
     * Ajoute des crédits au compte utilisateur (Stripe purchase)
     */
    public void addCredits(double amount) {
        if (amount > 0) {
            this.credits += amount;
        }
    }

    // -------------------------------------------------------
    // toString() — utile pour le débogage
    // -------------------------------------------------------
    @Override
    public String toString() {
        return "User{id=" + id + ", fullName='" + fullName + "', email='" + email
                + "', role='" + role + "', statut='" + statut + "'}";
    }
}


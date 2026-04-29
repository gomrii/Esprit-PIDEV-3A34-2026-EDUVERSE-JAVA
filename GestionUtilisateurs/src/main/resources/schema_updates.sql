-- ============================================================
-- EDUVERSE — Mises à jour BDD (Phase Avancée)
-- Exécuter dans l'ordre sur la base : eduverse-java
-- ============================================================

-- ------------------------------------------------------------
-- FONCTIONNALITÉ 4 — 2FA : Colonnes dans la table user
-- ------------------------------------------------------------
ALTER TABLE user
    ADD COLUMN IF NOT EXISTS is_two_factor_enabled TINYINT(1)   NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS two_factor_code       VARCHAR(6)       NULL,
    ADD COLUMN IF NOT EXISTS two_factor_expires_at DATETIME         NULL;

-- ------------------------------------------------------------
-- FONCTIONNALITÉ 8 — Détection connexions suspectes
-- ------------------------------------------------------------
ALTER TABLE user
    ADD COLUMN IF NOT EXISTS login_attempts INT      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until   DATETIME     NULL;

-- Table des tentatives de connexion (log complet)
CREATE TABLE IF NOT EXISTS login_attempt (
    id           INT          NOT NULL AUTO_INCREMENT,
    email        VARCHAR(180) NOT NULL,
    success      TINYINT(1)   NOT NULL DEFAULT 0,
    attempted_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_email_date (email, attempted_at)
);

-- ------------------------------------------------------------
-- FONCTIONNALITÉ 2 — Réinitialisation de mot de passe
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_token (
    id          INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    token       VARCHAR(64)  NOT NULL,
    expires_at  DATETIME     NOT NULL,
    used        TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_token (token),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- FONCTIONNALITÉ 7 — Historique des conversations IA
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_conversation (
    id           INT  NOT NULL AUTO_INCREMENT,
    admin_id     INT  NOT NULL,
    question     TEXT NOT NULL,
    reponse      TEXT NOT NULL,
    context_data TEXT     NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (admin_id) REFERENCES user(id) ON DELETE CASCADE
);

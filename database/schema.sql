CREATE DATABASE IF NOT EXISTS `eduverse_java`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `eduverse_java`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `full_name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `role` VARCHAR(30) NOT NULL,
  `statut` VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
  `is_approved` TINYINT(1) NOT NULL DEFAULT 0,
  `is_blocked` TINYINT(1) NOT NULL DEFAULT 0,
  `phone_number` VARCHAR(30) DEFAULT NULL,
  `bio` TEXT DEFAULT NULL,
  `picture` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `roles` VARCHAR(255) DEFAULT NULL,
  `is_rejected` TINYINT(1) NOT NULL DEFAULT 0,
  `is_verified` TINYINT(1) NOT NULL DEFAULT 0,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by_id` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user` (
  `full_name`, `email`, `password`, `role`, `statut`,
  `is_approved`, `is_blocked`, `phone_number`, `bio`, `picture`,
  `created_at`, `roles`, `is_rejected`, `is_verified`, `updated_at`, `created_by_id`
) VALUES (
  'Admin Demo',
  'admin@elearning.tn',
  'GFvqsBYDpobBDF/iSFVCeg==:z0Ia52t1EVOD43qjds3g5rOYeoTNHdTtPPp329RIWq0=',
  'ADMIN',
  'ACTIF',
  1,
  0,
  NULL,
  NULL,
  NULL,
  NOW(),
  '[\"ROLE_ADMIN\"]',
  0,
  1,
  NOW(),
  NULL
)
ON DUPLICATE KEY UPDATE
  `full_name` = VALUES(`full_name`),
  `role` = VALUES(`role`),
  `statut` = VALUES(`statut`),
  `is_approved` = VALUES(`is_approved`),
  `is_blocked` = VALUES(`is_blocked`),
  `updated_at` = NOW();

CREATE TABLE IF NOT EXISTS `formation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(150) NOT NULL,
  `description` TEXT NOT NULL,
  `content` LONGTEXT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `is_approved` TINYINT(1) NOT NULL DEFAULT 0,
  `is_archived` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `support_file` VARCHAR(255) DEFAULT NULL,
  `duration` INT NOT NULL,
  `level` VARCHAR(50) NOT NULL,
  `creator_id` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `formation_enrollment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `formation_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `enrolled_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_formation_student` (`formation_id`, `student_id`),
  KEY `idx_enroll_student` (`student_id`),
  CONSTRAINT `fk_enroll_formation` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_enroll_student` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ressource` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT NOT NULL,
  `url` VARCHAR(500) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `formation_id` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ressource_formation` (`formation_id`),
  CONSTRAINT `fk_ressource_formation` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Stripe Payment & Wallet Management
-- ====================================================================

-- Alter user table to add wallet balance (if not exists)
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `wallet_balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `wallet_updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Store Stripe transactions
CREATE TABLE IF NOT EXISTS `stripe_transaction` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `stripe_session_id` VARCHAR(255) NOT NULL UNIQUE,
  `stripe_payment_intent_id` VARCHAR(255) DEFAULT NULL,
  `amount_cents` INT NOT NULL COMMENT 'Amount in cents',
  `credits` INT NOT NULL COMMENT 'Number of credits to purchase',
  `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED, FAILED, CANCELED',
  `payment_method` VARCHAR(50) DEFAULT NULL COMMENT 'card, apple_pay, etc',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` DATETIME DEFAULT NULL,
  `metadata` JSON DEFAULT NULL COMMENT 'Additional metadata from Stripe',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stripe_session_id` (`stripe_session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_stripe_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Store webhook events for audit trail
CREATE TABLE IF NOT EXISTS `stripe_webhook_event` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `event_id` VARCHAR(255) NOT NULL UNIQUE,
  `event_type` VARCHAR(100) NOT NULL COMMENT 'checkout.session.completed, etc',
  `payload` JSON NOT NULL,
  `processed` TINYINT(1) NOT NULL DEFAULT 0,
  `processed_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_id` (`event_id`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- AI-Generated Descriptions (Groq)
-- ====================================================================

ALTER TABLE `formation` ADD COLUMN IF NOT EXISTS `ai_generated_description` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `formation` ADD COLUMN IF NOT EXISTS `groq_generation_status` VARCHAR(50) DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED, FAILED';

CREATE TABLE IF NOT EXISTS `groq_generated_content` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `entity_type` VARCHAR(50) NOT NULL COMMENT 'FORMATION, RESSOURCE, USER, etc',
  `entity_id` INT NOT NULL,
  `original_title` VARCHAR(255) NOT NULL,
  `generated_description` TEXT NOT NULL,
  `language` VARCHAR(10) NOT NULL DEFAULT 'fr',
  `temperature` FLOAT NOT NULL DEFAULT 0.7,
  `status` VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `groq_model` VARCHAR(100) DEFAULT 'llama-3.3-70b-versatile',
  PRIMARY KEY (`id`),
  KEY `idx_entity` (`entity_type`, `entity_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

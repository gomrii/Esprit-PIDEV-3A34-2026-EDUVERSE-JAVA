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

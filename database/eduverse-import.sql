-- Clean import script for eduverse database
-- This script drops all existing tables before creating new ones

USE `eduverse`;
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all existing tables
DROP TABLE IF EXISTS `messenger_messages`;
DROP TABLE IF EXISTS `reset_password_request`;
DROP TABLE IF EXISTS `user_formation`;
DROP TABLE IF EXISTS `wallet_transaction`;
DROP TABLE IF EXISTS `wallet`;
DROP TABLE IF EXISTS `stripe_transaction`;
DROP TABLE IF EXISTS `quiz_resultat`;
DROP TABLE IF EXISTS `quiz_for`;
DROP TABLE IF EXISTS `quiz_assessment`;
DROP TABLE IF EXISTS `question_quiz`;
DROP TABLE IF EXISTS `question`;
DROP TABLE IF EXISTS `reponse`;
DROP TABLE IF EXISTS `join_request`;
DROP TABLE IF EXISTS `login_attempt`;
DROP TABLE IF EXISTS `historique`;
DROP TABLE IF EXISTS `evaluation`;
DROP TABLE IF EXISTS `certificate`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `cours`;
DROP TABLE IF EXISTS `cour`;
DROP TABLE IF EXISTS `chapitre`;
DROP TABLE IF EXISTS `club_membership`;
DROP TABLE IF EXISTS `club_members`;
DROP TABLE IF EXISTS `club`;
DROP TABLE IF EXISTS `event`;
DROP TABLE IF EXISTS `ressource`;
DROP TABLE IF EXISTS `groq_generated_content`;
DROP TABLE IF EXISTS `ai_recommendation`;
DROP TABLE IF EXISTS `doctrine_migration_versions`;
DROP TABLE IF EXISTS `formation`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- Now run the main SQL file to import all tables
-- ============================================

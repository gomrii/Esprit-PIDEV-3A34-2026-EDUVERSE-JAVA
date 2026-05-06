-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mer. 06 mai 2026 à 02:25
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `eduverse`
--

-- --------------------------------------------------------

--
-- Structure de la table `ai_recommendation`
--

CREATE TABLE `ai_recommendation` (
  `id` int(11) NOT NULL,
  `priorite` varchar(10) NOT NULL,
  `type` varchar(20) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `action_suggeree` text NOT NULL,
  `lue` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `ai_recommendation`
--

INSERT INTO `ai_recommendation` (`id`, `priorite`, `type`, `titre`, `description`, `action_suggeree`, `lue`, `created_at`) VALUES
(28, 'FAIBLE', 'INFO', 'Comptes bloqués', 'Il n\'y a aucun compte bloqué. Vous pouvez ignorer cette information.', 'Ignorer cette information', 0, '2026-04-30 00:08:41'),
(29, 'HAUTE', 'APPROBATION', 'Vérifier la validité des comptes en attente', 'Vérifiez la validité des comptes en attente d\'approbation et approuvez-les si nécessaire.', 'Vérifier la validité des comptes en attente d\'approbation et approuver ceux qui sont valides', 0, '2026-04-30 00:08:41'),
(30, 'HAUTE', 'APPROBATION', 'Vérifier les comptes en attente d\'approbation', 'Il y a 2 comptes en attente d\'approbation depuis plusieurs jours. Il est important de vérifier leurs informations pour éviter les problèmes de sécurité.', 'Vérifier les comptes en attente d\'approbation et les approuver si nécessaire', 0, '2026-04-30 00:15:04'),
(31, 'MOYENNE', 'ALERT', 'Comptes en attente depuis plusieurs jours', 'Il y a 2 comptes en attente depuis plus de 3 jours. Il est important de prendre en compte ces utilisateurs pour éviter les problèmes de sécurité.', 'Vérifier les comptes en attente depuis plus de 3 jours et prendre les mesures nécessaires', 0, '2026-04-30 00:15:04'),
(32, 'FAIBLE', 'INFO', 'Mise à jour des statistiques', 'Il est important de mettre à jour les statistiques de l\'application pour avoir une vue d\'ensemble de l\'utilisation.', 'Mettre à jour les statistiques de l\'application', 0, '2026-04-30 00:15:04'),
(33, 'HAUTE', 'APPROBATION', 'Vérifier les comptes en attente d\'approbation', 'Il y a 2 comptes en attente d\'approbation depuis plusieurs jours. Il est important de vérifier ces comptes pour éviter toute perte de temps ou de données.', 'Vérifier les comptes en attente d\'approbation et prendre une décision de validation ou de refus', 0, '2026-04-30 00:16:17'),
(34, 'MOYENNE', 'INFO', 'Comptes en attente depuis plus de 3 jours', 'Il y a 2 comptes en attente depuis plus de 3 jours. Il est important de prendre en compte ces comptes pour éviter toute perte de données ou de temps.', 'Vérifier les comptes en attente depuis plus de 3 jours et prendre une décision de validation ou de refus', 0, '2026-04-30 00:16:17'),
(35, 'FAIBLE', 'INFO', 'Comptes bloqués', 'Il n\'y a aucun compte bloqué. Cependant, il est important de vérifier régulièrement les comptes bloqués pour éviter toute perte de données ou de temps.', 'Vérifier régulièrement les comptes bloqués', 0, '2026-04-30 00:16:17'),
(36, 'HAUTE', 'APPROBATION', 'Approuver les comptes en attente', 'Il y a 2 comptes en attente d\'approbation. Il est important de les approuver rapidement pour éviter tout retard dans l\'utilisation de la plateforme.', 'Approuver les comptes de Salim Gmidene et Test', 0, '2026-04-30 00:16:56'),
(37, 'MOYENNE', 'ALERTE', 'Utilisateurs en attente depuis plus de 3 jours', 'Il y a 2 utilisateurs en attente depuis plus de 3 jours. Il est important de les contacter pour savoir si tout va bien et si ils ont besoin d\'aide.', 'Contacter Salim Gmidene et Test pour savoir si tout va bien', 0, '2026-04-30 00:16:56'),
(38, 'MOYENNE', 'INFO', 'Comptes bloqués', 'Il n\'y a aucun compte bloqué actuellement. Cela est à prendre en compte pour les prochaines mises à jour de sécurité.', 'Vérifier régulièrement les comptes bloqués pour s\'assurer que tout est en ordre', 0, '2026-04-30 00:16:56'),
(39, 'HAUTE', 'APPROBATION', 'Vérifier les comptes en attente d\'approbation', 'Il y a 2 comptes en attente d\'approbation depuis plus de 3 jours. Il est important de les vérifier pour éviter tout retard ou problème.', 'Vérifier les comptes en attente d\'approbation et les approuver ou refuser selon les cas.', 0, '2026-04-30 00:21:47'),
(40, 'MOYENNE', 'INFO', 'Comptes en attente depuis plus de 3 jours', 'Il y a 2 comptes en attente depuis plus de 3 jours. Il est important de suivre ces comptes pour éviter tout problème.', 'Suivre les comptes en attente depuis plus de 3 jours et prendre les mesures nécessaires pour les résoudre.', 0, '2026-04-30 00:21:47'),
(41, 'FAIBLE', 'INFO', 'Mise à jour de la base de données', 'Il est important de mettre à jour régulièrement la base de données pour garantir la sécurité et la stabilité du système.', 'Mettre à jour la base de données et effectuer les vérifications nécessaires pour garantir la sécurité et la stabilité du système.', 0, '2026-04-30 00:21:47'),
(42, 'HAUTE', 'APPROBATION', 'Vérifier les comptes en attente d\'approbation', 'Il y a 2 comptes en attente d\'approbation depuis plus de 3 jours, il est important de les vérifier pour éviter les retards dans l\'accès aux ressources.', 'Vérifier les comptes de Salim Gmidene et Test et prendre une décision d\'approbation ou de blocage', 0, '2026-04-30 09:17:58'),
(43, 'MOYENNE', 'INFO', 'Rappel de la gestion des comptes bloqués', 'Il n\'y a aucun compte bloqué, mais il est important de rappeler aux administrateurs la procédure de blocage des comptes pour éviter les abus.', 'Rappeler aux administrateurs la procédure de blocage des comptes', 0, '2026-04-30 09:17:58'),
(44, 'MOYENNE', 'INFO', 'Vérifier les comptes en attente depuis plus de 3 jours', 'Il y a 2 comptes en attente depuis plus de 3 jours, il est important de les vérifier pour éviter les retards dans l\'accès aux ressources.', 'Vérifier les comptes de Salim Gmidene et Test et prendre une décision d\'approbation ou de blocage', 0, '2026-04-30 09:17:58'),
(45, 'HAUTE', 'APPROBATION', 'Approuver les comptes en attente', 'Il y a 2 comptes en attente d\'approbation depuis plus de 3 jours. Il est important de les approuver pour éviter tout blocage supplémentaire.', 'Approuver les comptes de Salim Gmidene et Test', 0, '2026-04-30 09:19:17'),
(46, 'MOYENNE', 'INFO', 'Vérifier les comptes bloqués', 'Il n\'y a aucun compte bloqué actuellement, mais il est important de vérifier régulièrement pour s\'assurer que tout est en ordre.', 'Vérifier les paramètres de blocage pour s\'assurer qu\'ils sont corrects', 0, '2026-04-30 09:19:17'),
(47, 'FAIBLE', 'INFO', 'Vérifier les paramètres d\'approbation', 'Il est important de vérifier régulièrement les paramètres d\'approbation pour s\'assurer qu\'ils sont corrects et qu\'ils ne nécessitent pas de modifications.', 'Vérifier les paramètres d\'approbation pour s\'assurer qu\'ils sont corrects', 0, '2026-04-30 09:19:17'),
(48, 'HAUTE', 'APPROBATION', 'Vérifier les comptes en attente d\'approbation', 'Il y a 2 comptes en attente d\'approbation depuis plusieurs jours, il est important de vérifier leur validité pour éviter des problèmes de sécurité.', 'Vérifier les comptes en attente d\'approbation et les approuver ou les bloquer si nécessaire', 0, '2026-04-30 09:22:14'),
(49, 'MOYENNE', 'INFO', 'Informations sur les comptes bloqués', 'Il n\'y a aucun compte bloqué actuellement, mais il est important de surveiller cette situation pour éviter des problèmes de sécurité.', 'Surveiller les comptes bloqués et prendre des mesures si nécessaire', 0, '2026-04-30 09:22:14'),
(50, 'MOYENNE', 'ALERTE', 'Comptes en attente depuis plus de 3 jours', 'Il y a 2 comptes en attente depuis plus de 3 jours, il est important de prendre des mesures pour résoudre ce problème.', 'Contacter les utilisateurs en attente pour résoudre le problème', 0, '2026-04-30 09:22:14'),
(51, 'FAIBLE', 'INFO', 'Informations sur le nombre d\'utilisateurs', 'Il y a actuellement 8 utilisateurs sur la plateforme, il est important de surveiller ce nombre pour éviter des problèmes de sécurité.', 'Surveiller le nombre d\'utilisateurs et prendre des mesures si nécessaire', 0, '2026-04-30 09:22:14');

-- --------------------------------------------------------

--
-- Structure de la table `certificate`
--

CREATE TABLE `certificate` (
  `id` int(11) NOT NULL,
  `score` double NOT NULL,
  `awarded_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `formation_id` int(11) NOT NULL,
  `quiz_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `chapitre`
--

CREATE TABLE `chapitre` (
  `id` int(11) NOT NULL,
  `title` varchar(60) NOT NULL,
  `contenu` varchar(4000) NOT NULL,
  `cours_id` int(11) NOT NULL,
  `pdf` varchar(150) DEFAULT NULL,
  `video` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `chapitre`
--

INSERT INTO `chapitre` (`id`, `title`, `contenu`, `cours_id`, `pdf`, `video`) VALUES
(5, 'La Formule Classique', 'Le Vocabulaire de baseExpérience aléatoire : Une expérience dont on connaît tous les résultats possibles, mais on ne sait pas lequel va se produire (ex: lancer un dé).Univers ($\\Omega$) : L\'ensemble de tous les résultats possibles. Pour un dé à 6 faces, $\\Omega = \\{1, 2, 3, 4, 5, 6\\}$.Événement : Un sous-ensemble de l\'univers (ex: \"Obtenir un chiffre pair\" $\\rightarrow \\{2, 4, 6\\}$).2. La Formule ClassiqueDans une situation d\'équiprobabilité (chaque résultat a la même chance de sortir), la probabilité d\'un événement $A$ se calcule ainsi :$$P(A) = \\frac{\\text{Nombre de cas favorables}}{\\text{Nombre de cas possibles}}$$Exemple : Quelle est la probabilité d\'obtenir un \"4\" en lançant un dé ?Cas favorable : $\\{4\\}$ (il y en a 1)Cas possibles : $\\{1, 2, 3, 4, 5, 6\\}$ (il y en a 6)$P(4) = \\frac{1}{6}$3. Les Règles d\'OrUne probabilité est toujours comprise entre 0 (événement impossible) et 1 (événement certain).La somme des probabilités de tous les résultats de l\'univers est toujours égale à 1.Événement contraire ($\\bar{A}$) : La probabilité que $A$ ne se produise pas est $P(\\bar{A}) = 1 - P(A)$.', 14, 'C:\\Users\\Sahar\\Downloads\\Variables_et_Types.pdf', ''),
(6, 'Sécurité et Base de Données (PDO)', 'Développer un site web ne se limite pas à afficher du texte, il s\'agit avant tout de gérer des données utilisateur de manière dynamique et sécurisée. Ce module se concentre sur l\'utilisation de l\'extension PDO (PHP Data Objects) pour interagir avec MySQL, offrant une couche d\'abstraction robuste et protégeant votre application contre les injections SQL grâce aux requêtes préparées. Nous verrons comment structurer vos scripts pour séparer la logique de traitement des données de l\'affichage HTML, posant ainsi les bases du design pattern MVC', 17, '', ''),
(8, 'ghjkl', 'qsdfghjk;,nbvcxw', 14, '', ''),
(10, 'javafx', 'JavaFX est une technologie qui permet de créer des applications graphiques avec Java, un peu comme si on construisait des interfaces utilisateur interactives avec des boutons, des champs de texte et des images.  Cela signifie que vous pouvez rendre vos programmes plus jolis et plus faciles à utiliser en ajoutant des éléments visuels plutôt que de simplement afficher du texte dans une console.  Pour commencer, vous allez découvrir comment placer des formes simples comme des rectangles ou des cercles à l\'écran, et comment les remplir de couleurs différentes.  Ensuite, vous apprendrez à ajouter des contrôles courants comme des boutons sur lesquels les utilisateurs peuvent cliquer pour déclencher des actions.  Un exemple serait de créer un bouton qui, lorsqu\'on clique dessus, change le texte affiché dans une étiquette.  Avec un peu de pratique, JavaFX vous permettra de construire des applications de bureau complètes et attrayantes.', 22, '', ''),
(11, 'sql', 'Dans notre module sur les bases de données NoSQL, nous abordons le chapitre consacré à SQL, qui peut sembler un peu étonnant au premier abord puisque NoSQL est censé être différent de SQL.  Cependant, comprendre les bases de SQL nous aide énormément à saisir les raisons pour lesquelles les bases NoSQL ont été créées et comment elles fonctionnent en contraste.  SQL, ou Structured Query Language, est le langage standard pour interagir avec les bases de données relationnelles, où les données sont organisées en tableaux avec des lignes et des colonnes bien définies, un peu comme des feuilles de calcul connectées.  Pensez à une base de données SQL pour gérer des informations sur des clients, où chaque client a une ligne dans un tableau et chaque information comme le nom, l\'adresse ou le numéro de téléphone est une colonne.  Apprendre à écrire des requêtes SQL simples, comme sélectionner des informations spécifiques ou insérer de nouvelles données, nous donne une perspective essentielle sur la façon dont les données sont structurées et récupérées traditionnellement.  Cette compréhension fondamentale des bases de données relationnelles et de leur langage est une étape importante avant de plonger dans les spécificités des différentes familles de bases de données NoSQL.', 27, '', ''),
(12, 'dfghjk', 'sdfghjk', 17, '', '');

-- --------------------------------------------------------

--
-- Structure de la table `club`
--

CREATE TABLE `club` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `creator_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `club_members`
--

CREATE TABLE `club_members` (
  `club_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `club_membership`
--

CREATE TABLE `club_membership` (
  `user_id` int(11) NOT NULL,
  `club_id` int(11) NOT NULL,
  `status` varchar(50) DEFAULT 'PENDING',
  `joined_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `cour`
--

CREATE TABLE `cour` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `content` longtext NOT NULL,
  `level` varchar(50) NOT NULL,
  `course_file` varchar(255) DEFAULT NULL,
  `course_video` varchar(255) DEFAULT NULL,
  `course_image` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `creator_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `cours`
--

CREATE TABLE `cours` (
  `id` int(11) NOT NULL,
  `title` varchar(100) NOT NULL,
  `category` varchar(100) NOT NULL,
  `descrption` mediumtext NOT NULL,
  `image` varchar(600) NOT NULL,
  `level` varchar(100) NOT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` date NOT NULL DEFAULT current_timestamp(),
  `updated_at` date NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `cours`
--

INSERT INTO `cours` (`id`, `title`, `category`, `descrption`, `image`, `level`, `status`, `created_at`, `updated_at`) VALUES
(14, 'probaa', 'Mathématiques', 'La théorie des probabilités est la branche des mathématiques qui étudie les phénomènes aléatoires (dont on ne peut pas prédire l\'issue avec certitude).', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/07.jpg', 'Intermédiaire', 'approuved', '2026-04-15', '2026-04-16'),
(15, 'qsdfghjk', 'Reseaux', 'dfghjkl', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/02.jpg', 'Avancé', 'approuved', '2026-04-15', '2026-05-06'),
(16, 'business', 'Business', 'azertyuioplkjhgfdsq', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/22.jpg', 'Débutant', 'approuved', '2026-04-15', '2026-04-15'),
(17, 'javascript', 'Programmation', 'sdfghjklmù', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/09.jpg', 'Intermédiaire', 'approuved', '2026-04-15', '2026-04-15'),
(18, 'ofps', 'Reseaux', 'azdfghjk', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/08.jpg', 'Avancé', 'approuved', '2026-04-16', '2026-04-30'),
(19, 'php', 'Programmation', 'azertyuiopmlkjhgfdsqwxcvbn,;kjuy(\'edfgh', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/13.jpg', 'Intermédiaire', 'approuved', '2026-04-16', '2026-04-16'),
(20, 'ccna', 'Mathématiques', 'azertyuiolkjhgfcd', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/10.jpg', 'Intermédiaire', 'pending', '2026-04-16', '2026-04-27'),
(21, 'analyse', 'Mathématiques', 'azertyuikjbvc', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/06.jpg', 'Intermédiaire', 'pending', '2026-04-16', '2026-04-27'),
(22, 'java', 'Physique', 'azertyuiojhgf', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/09.jpg', 'Intermédiaire', 'approuved', '2026-04-16', '2026-04-16'),
(24, 'hjkl', 'Design', 'hjkl', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/04.jpg', 'Avancé', 'approuved', '2026-04-29', '2026-04-30'),
(25, 'Développement Web Avancé', 'Programmation', 'Maîtrisez les frameworks modernes.', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/13.jpg', 'Intermédiaire', 'approuved', '2026-04-29', '2026-04-29'),
(26, 'Algorithmes Complexes', 'Programmation', 'Optimisation et structures de données.', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/13.jpg', 'Intermédiaire', 'approuved', '2026-04-29', '2026-04-29'),
(27, 'Base de données NoSQL', 'Programmation', 'Introduction à MongoDB et Firebase.', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/13.jpg', 'Intermédiaire', 'approuved', '2026-04-29', '2026-04-29'),
(28, 'Sécurité Informatique', 'Programmation', 'Les bases de la cybersécurité.', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/13.jpg', 'Intermédiaire', 'approuved', '2026-04-29', '2026-04-29'),
(29, 'Intelligence Artificielle', 'Programmation', 'Premiers pas en Machine Learning.', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/13.jpg', 'Intermédiaire', 'approuved', '2026-04-29', '2026-04-29'),
(31, 'jjjjxj', 'Design', 'dfghjk', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/01.jpg', 'Intermédiaire', 'approuved', '2026-05-06', '2026-05-06'),
(32, 'sd', 'Mathématiques', 'qsd', 'file:/C:/Users/Sahar/Images/Captures%20d’écran/03.jpg', 'Avancé', 'approuved', '2026-05-06', '2026-05-06');

-- --------------------------------------------------------

--
-- Structure de la table `course`
--

CREATE TABLE `course` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `is_approved` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20260209210655', NULL, NULL),
('DoctrineMigrations\\Version20260209212825', NULL, NULL),
('DoctrineMigrations\\Version20260209220615', NULL, NULL),
('DoctrineMigrations\\Version20260209221312', NULL, NULL),
('DoctrineMigrations\\Version20260209223849', NULL, NULL),
('DoctrineMigrations\\Version20260210000001', NULL, NULL),
('DoctrineMigrations\\Version20260210213116', NULL, NULL),
('DoctrineMigrations\\Version20260210215653', NULL, NULL),
('DoctrineMigrations\\Version20260210220000', NULL, NULL),
('DoctrineMigrations\\Version20260210224756', NULL, NULL),
('DoctrineMigrations\\Version20260211000000', NULL, NULL),
('DoctrineMigrations\\Version20260211000001', NULL, NULL),
('DoctrineMigrations\\Version20260211000002', NULL, NULL),
('DoctrineMigrations\\Version20260211000003', NULL, NULL),
('DoctrineMigrations\\Version20260211165229', NULL, NULL),
('DoctrineMigrations\\Version20260211213620', NULL, NULL),
('DoctrineMigrations\\Version20260212000000', NULL, NULL),
('DoctrineMigrations\\Version20260212051255', NULL, NULL),
('DoctrineMigrations\\Version20260212051308', NULL, NULL),
('DoctrineMigrations\\Version20260212140000', NULL, NULL),
('DoctrineMigrations\\Version20260222224618', NULL, NULL),
('DoctrineMigrations\\Version20260222230640', NULL, NULL),
('DoctrineMigrations\\Version20260226033615', NULL, NULL),
('DoctrineMigrations\\Version20260226083051', NULL, NULL),
('DoctrineMigrations\\Version20260226100000', NULL, NULL),
('DoctrineMigrations\\Version20260305013322', NULL, NULL),
('DoctrineMigrations\\Version20260305030240', '2026-03-05 04:04:14', 1249),
('DoctrineMigrations\\Version20260305031825', '2026-03-05 04:20:19', 377);

-- --------------------------------------------------------

--
-- Structure de la table `evaluation`
--

CREATE TABLE `evaluation` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `is_approved` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `event`
--

CREATE TABLE `event` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `event_date` datetime NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `club_id` int(11) NOT NULL,
  `creator_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `formation`
--

CREATE TABLE `formation` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `content` longtext NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `is_approved` tinyint(1) NOT NULL,
  `created_at` datetime NOT NULL,
  `support_file` varchar(255) DEFAULT NULL,
  `is_archived` tinyint(1) NOT NULL,
  `duration` int(11) DEFAULT NULL,
  `level` varchar(255) NOT NULL,
  `creator_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `historique`
--

CREATE TABLE `historique` (
  `id` int(11) NOT NULL,
  `id_cours` int(11) DEFAULT NULL,
  `date_consultation` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `historique`
--

INSERT INTO `historique` (`id`, `id_cours`, `date_consultation`) VALUES
(15, 25, '2026-04-29 23:16:29'),
(16, 28, '2026-04-29 23:17:19'),
(17, 14, '2026-05-05 21:15:54'),
(18, 29, '2026-05-05 21:16:04'),
(19, 29, '2026-05-05 21:16:04'),
(20, 29, '2026-05-05 21:16:04'),
(21, 29, '2026-05-05 21:16:04'),
(22, 29, '2026-05-05 21:16:05'),
(23, 14, '2026-05-05 21:26:50'),
(24, 15, '2026-05-05 21:27:10'),
(25, 14, '2026-05-05 21:27:23'),
(26, 14, '2026-05-05 22:00:07'),
(27, 15, '2026-05-05 22:00:18'),
(28, 16, '2026-05-05 22:00:24'),
(29, 14, '2026-05-05 22:00:30'),
(30, 27, '2026-05-05 22:00:39'),
(31, 14, '2026-05-05 22:04:35'),
(32, 14, '2026-05-05 22:04:42'),
(33, 14, '2026-05-05 22:07:45');

-- --------------------------------------------------------

--
-- Structure de la table `join_request`
--

CREATE TABLE `join_request` (
  `id` int(11) NOT NULL,
  `status` varchar(50) NOT NULL,
  `requested_at` datetime NOT NULL,
  `responded_at` datetime DEFAULT NULL,
  `club_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `login_attempt`
--

CREATE TABLE `login_attempt` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `success` tinyint(1) NOT NULL DEFAULT 0,
  `attempted_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `login_attempt`
--

INSERT INTO `login_attempt` (`id`, `email`, `success`, `attempted_at`) VALUES
(1, 'kamel@test.tn', 0, '2026-04-29 01:25:43'),
(2, 'kamel@test.tn', 0, '2026-04-29 01:26:09'),
(3, 'kamel@test.tn', 0, '2026-04-29 01:26:19'),
(4, 'kamel@test.tn', 0, '2026-04-29 01:26:35'),
(5, 'kamel@test.tn', 0, '2026-04-29 01:26:45'),
(6, 'admin@test.tn', 0, '2026-04-29 01:27:13'),
(7, 'admin@eduverse.tn', 0, '2026-04-29 01:27:34'),
(8, 'admin@eduverse.tn', 1, '2026-04-29 01:27:51'),
(9, 'kamel@test.tn', 0, '2026-04-29 01:34:27'),
(10, 'admin@eduverse.tn', 1, '2026-04-29 01:34:56'),
(11, 'kamel@test.tn', 0, '2026-04-29 01:35:23'),
(12, 'kamel@test.tn', 0, '2026-04-29 01:35:41'),
(13, 'kamel@test.tn', 0, '2026-04-29 01:36:02'),
(14, 'kamel@test.tn', 0, '2026-04-29 01:36:44'),
(15, 'youssef@gmail.com', 1, '2026-04-29 01:37:44'),
(16, 'youssef@gmail.com', 1, '2026-04-29 02:23:02'),
(17, 'yassmine@gmail.com', 1, '2026-04-29 02:23:22'),
(18, 'admin@eduverse.tn', 1, '2026-04-29 02:24:30'),
(19, 'admin@eduverse.tn', 1, '2026-04-29 02:24:37'),
(20, 'admin@eduverse.tn', 1, '2026-04-29 02:24:48'),
(21, 'admin@eduverse.tn', 1, '2026-04-29 02:29:35'),
(22, 'admin@eduverse.tn', 1, '2026-04-29 02:33:42'),
(23, 'admin@eduverse.tn', 1, '2026-04-29 02:35:26'),
(24, 'admin@eduverse.tn', 1, '2026-04-29 02:37:13'),
(25, 'youssef@gmail.com', 1, '2026-04-29 02:38:25'),
(26, 'admin@eduverse.tn', 1, '2026-04-29 02:38:50'),
(27, 'admin@eduverse.tn', 1, '2026-04-30 00:21:18'),
(28, 'admin@eduverse.tn', 1, '2026-04-30 00:26:10'),
(29, 'admin@eduverse.tn', 1, '2026-04-30 00:44:05'),
(30, 'admin@eduverse.tn', 1, '2026-04-30 00:49:49'),
(31, 'admin@eduverse.tn', 1, '2026-04-30 00:52:07'),
(32, 'admin@eduverse.tn', 1, '2026-04-30 01:04:23'),
(33, 'admin@eduverse.tn', 1, '2026-04-30 01:08:40'),
(34, 'admin@eduverse.tn', 0, '2026-04-30 01:14:46'),
(35, 'admin@eduverse.tn', 1, '2026-04-30 01:15:03'),
(36, 'admin@eduverse.tn', 1, '2026-04-30 01:16:16'),
(37, 'admin@eduverse.tn', 1, '2026-04-30 08:48:43'),
(38, 'admin@eduverse.tn', 1, '2026-04-30 10:17:57'),
(39, 'admin@eduverse.tn', 1, '2026-04-30 10:19:17'),
(40, 'admin@eduverse.tn', 1, '2026-04-30 10:22:13'),
(41, 'admin@eduverse.tn', 1, '2026-05-05 17:48:30'),
(42, 'admin@eduverse.tn', 1, '2026-05-05 18:17:23'),
(43, 'admin@eduverse.tn', 1, '2026-05-05 19:29:23'),
(44, 'admin@eduverse.tn', 1, '2026-05-05 20:01:10'),
(45, 'admin@eduverse.tn', 1, '2026-05-05 20:27:20'),
(46, 'admin@eduverse.tn', 1, '2026-05-05 20:28:27'),
(47, 'admin@eduverse.tn', 1, '2026-05-05 20:37:00'),
(48, 'admin@eduverse.tn', 1, '2026-05-05 20:53:46'),
(49, 'admin@eduverse.tn', 1, '2026-05-05 21:12:56'),
(50, 'yassmine@gmail.com', 1, '2026-05-05 21:21:27'),
(51, 'youssef@gmail.com', 1, '2026-05-05 21:23:35'),
(52, 'admin@eduverse.tn', 1, '2026-05-05 21:40:23'),
(53, 'admin@eduverse.tn', 1, '2026-05-05 22:03:18'),
(54, 'youssef@gmail.com', 1, '2026-05-05 22:06:22'),
(55, 'admin@eduverse.tn', 1, '2026-05-05 23:18:13'),
(56, 'youssef@gmail.com', 1, '2026-05-05 23:20:17'),
(57, 'admin@eduverse.tn', 1, '2026-05-05 23:27:05'),
(58, 'admin@eduverse.tn', 1, '2026-05-05 23:44:14'),
(59, 'admin@eduverse.tn', 1, '2026-05-05 23:48:40'),
(60, 'youssef@gmail.com', 1, '2026-05-05 23:49:26'),
(61, 'admin@eduverse.tn', 1, '2026-05-05 23:54:54'),
(62, 'admin@eduverse.tn', 1, '2026-05-06 00:03:18'),
(63, 'admin@eduverse.tn', 1, '2026-05-06 00:06:25'),
(64, 'admin@eduverse.tn', 1, '2026-05-06 00:09:28'),
(65, 'admin@eduverse.tn', 1, '2026-05-06 00:47:56'),
(66, 'youssef@gmail.com', 1, '2026-05-06 00:54:55'),
(67, 'admin@eduverse.tn', 1, '2026-05-06 00:56:32'),
(68, 'admin@eduverse.tn', 1, '2026-05-06 01:03:19'),
(69, 'admin@eduverse.tn', 1, '2026-05-06 01:09:26'),
(70, 'admin@eduverse.tn', 1, '2026-05-06 01:19:22');

-- --------------------------------------------------------

--
-- Structure de la table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `question`
--

CREATE TABLE `question` (
  `idQuestion` int(11) NOT NULL,
  `question` text NOT NULL,
  `idQuiz` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `question`
--

INSERT INTO `question` (`idQuestion`, `question`, `idQuiz`) VALUES
(1, 'Qu\'est-ce que la dérivée d\'une fonction en un point, géométriquement ?', 2),
(2, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 1);

-- --------------------------------------------------------

--
-- Structure de la table `question_quiz`
--

CREATE TABLE `question_quiz` (
  `id` int(11) NOT NULL,
  `question` varchar(255) NOT NULL,
  `correct_answer` varchar(255) NOT NULL,
  `score` decimal(10,2) NOT NULL,
  `type` varchar(50) NOT NULL,
  `choices` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`choices`)),
  `quiz_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `quiz`
--

CREATE TABLE `quiz` (
  `idQuiz` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `statut` varchar(100) DEFAULT NULL,
  `createdBy` varchar(100) DEFAULT NULL,
  `duree` int(11) NOT NULL,
  `level` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `quiz`
--

INSERT INTO `quiz` (`idQuiz`, `titre`, `statut`, `createdBy`, `duree`, `level`) VALUES
(1, 'java', 'valide', 'ADMIN#1', 1, 'moyen'),
(2, 'analyse', 'valide', 'ADMIN#1', 1, 'moyen'),
(3, 'symfony', 'valide', 'ADMIN#1', 1, 'moyen');

-- --------------------------------------------------------

--
-- Structure de la table `quiz_assessment`
--

CREATE TABLE `quiz_assessment` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `is_approved` tinyint(1) NOT NULL,
  `state` varchar(100) DEFAULT NULL,
  `level` varchar(100) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_archived` tinyint(1) NOT NULL,
  `creator_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `quiz_for`
--

CREATE TABLE `quiz_for` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `category` varchar(255) NOT NULL,
  `total_score` decimal(10,2) NOT NULL,
  `pass_score` decimal(5,2) NOT NULL,
  `total_questions` int(11) NOT NULL,
  `created_on` date NOT NULL,
  `difficulty` varchar(255) NOT NULL,
  `formation_id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `statut` varchar(100) DEFAULT NULL,
  `createdBy` varchar(100) DEFAULT NULL,
  `duree` int(11) NOT NULL DEFAULT 0,
  `level` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `quiz_resultat`
--

CREATE TABLE `quiz_resultat` (
  `id` int(11) NOT NULL,
  `score` decimal(10,2) NOT NULL,
  `answers` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`answers`)),
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `student_id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL,
  `created_by_id` int(11) NOT NULL,
  `updated_by_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `reponse`
--

CREATE TABLE `reponse` (
  `idReponse` int(11) NOT NULL,
  `reponse` text NOT NULL,
  `score` double NOT NULL,
  `idQuestion` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `reponse`
--

INSERT INTO `reponse` (`idReponse`, `reponse`, `score`, `idQuestion`) VALUES
(1, 'L\'aire sous la courbe de la fonction jusqu\'à ce point.', 0, 1),
(2, 'La pente de la tangente à la courbe de la fonction en ce point.', 1, 1),
(3, 'La valeur de la fonction en ce point.', 0, 1),
(4, 'Le point d\'inflexion de la courbe.', 0, 1),
(5, 'aaaaaaaaaaaaaaaaaaaaaaaa', 0, 2);

-- --------------------------------------------------------

--
-- Structure de la table `reset_password_request`
--

CREATE TABLE `reset_password_request` (
  `id` int(11) NOT NULL,
  `selector` varchar(20) NOT NULL,
  `hashed_token` varchar(100) NOT NULL,
  `requested_at` datetime NOT NULL,
  `expires_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `ressource`
--

CREATE TABLE `ressource` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `type` varchar(255) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `cost` decimal(10,2) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `formation_id` int(11) NOT NULL,
  `created_by_id` int(11) NOT NULL,
  `updated_by_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `roles` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`roles`)),
  `google_id` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `is_approved` tinyint(1) NOT NULL,
  `is_two_factor_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `phone_number` varchar(20) DEFAULT NULL,
  `two_factor_code` varchar(6) DEFAULT NULL,
  `two_factor_expires_at` datetime DEFAULT NULL,
  `face_descriptor` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`face_descriptor`)),
  `is_blocked` tinyint(1) NOT NULL,
  `is_rejected` tinyint(1) NOT NULL,
  `is_verified` tinyint(1) NOT NULL,
  `picture` varchar(255) DEFAULT NULL,
  `job_title` varchar(255) DEFAULT NULL,
  `bio` longtext DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `created_by_id` int(11) NOT NULL,
  `updated_by_id` int(11) DEFAULT NULL,
  `statut` varchar(50) DEFAULT 'ACTIF',
  `login_attempts` int(11) NOT NULL DEFAULT 0,
  `locked_until` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `email`, `roles`, `google_id`, `password`, `full_name`, `role`, `is_approved`, `is_two_factor_enabled`, `phone_number`, `two_factor_code`, `two_factor_expires_at`, `face_descriptor`, `is_blocked`, `is_rejected`, `is_verified`, `picture`, `job_title`, `bio`, `created_at`, `updated_at`, `created_by_id`, `updated_by_id`, `statut`, `login_attempts`, `locked_until`) VALUES
(1, 'admin@eduverse.tn', '[\"ROLE_ADMIN\"]', NULL, '6Vg9XiugWDyyXuYhv6x3Ng==:PafMhlkx2bbNll54UTd222Jg2PJV79LuMn8ZM8sONRo=', 'Administrateur', 'ADMIN', 1, 0, NULL, NULL, NULL, NULL, 0, 0, 1, NULL, NULL, NULL, '2026-04-13 23:55:18', '2026-04-13 23:55:18', 1, NULL, 'ACTIF', 0, NULL),
(5, 'youssef@gmail.com', '[\"ROLE_STUDENT\"]', NULL, 'GbpSlnaK8ildSRFZ2EK64w==:x3J/lFROYViqPrWhVXNi5B+my+VSqXWmts7XBvSaMKI=', 'youssef', 'Student', 1, 0, '', NULL, NULL, NULL, 0, 0, 0, 'C:\\Users\\Youssef\\Downloads\\GestionUtilisateurs_JavaFX_JDBC\\GestionUtilisateurs\\avatars\\avatar_5.png', NULL, '', '2026-04-15 13:26:44', '2026-04-15 13:26:44', 1, NULL, 'ACTIF', 0, NULL),
(6, 'yassmine@gmail.com', '[\"ROLE_INSTRUCTOR\"]', NULL, 'mmIl72iyzy0fIUHluzSI9Q==:X+8PGdYZoY44r6+CSqcwuGbdkrmi30Wa3XY0iVRgYLc=', 'yassmine', 'ENSEIGNANT', 1, 1, '', '225462', '2026-05-05 19:27:01', NULL, 0, 0, 0, NULL, NULL, '', '2026-04-15 13:49:49', '2026-04-15 13:49:49', 1, NULL, 'ACTIF', 0, NULL),
(7, 'badii@gmail.com', '[\"ROLE_INSTRUCTOR\"]', NULL, 'doW8xW55ayBofB+3u6T5aw==:jSstWHx5nwg4I5n3DIScEmAti7pyzS+h6fg7EiAk06Y=', 'badiiiii', 'ENSEIGNANT', 1, 0, '', NULL, NULL, NULL, 0, 0, 0, NULL, NULL, '', '2026-04-15 13:59:02', '2026-04-15 13:59:02', 1, NULL, 'ACTIF', 0, NULL),
(9, 'salim@eduverse.tn', '[\"ROLE_INSTRUCTOR\"]', NULL, 'ie+KvfSbGmMIj/IPTfav/A==:cCKCLiKzAt1g4UL0ltrN5wmuwmBhx7LYRMQXvMHkotE=', 'salim gmidene', 'ENSEIGNANT', 0, 0, NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, NULL, '2026-04-16 08:16:29', '2026-04-16 08:16:29', 1, NULL, 'EN_ATTENTE', 0, NULL),
(10, 'test@gmail.com', '[\"ROLE_STUDENT\"]', NULL, 'bD6GGoa2WJCJEEMNVLQ7lA==:wuHLPzqZf05wWKPZkAfkPtvdlEeX7ME9y6fL1116W4c=', 'hamma', 'ENSEIGNANT', 1, 0, '', NULL, NULL, NULL, 0, 0, 0, NULL, NULL, '', '2026-04-16 09:03:04', '2026-04-16 09:03:04', 1, NULL, 'ACTIF', 0, NULL),
(11, 'test@eduverse.tn', '[\"ROLE_STUDENT\"]', NULL, '$2a$12$cpKe1jiU0EPgFmeQHYqYS.R5jYj.PKrA.LlyqG4Samm3L/ehh07E6', 'Test', 'Student', 0, 0, NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, NULL, '2026-04-26 14:15:47', '2026-04-26 14:15:47', 1, NULL, 'EN_ATTENTE', 0, NULL),
(12, 'kamel@test.tn', '[\"ROLE_STUDENT\"]', NULL, '$2a$12$MOC/vQt1WaEGnM29Zrl7v.E75tTLIyie2/iztNVEuuZ89bIbMshI6', 'kamel bouzidi', 'Student', 1, 0, '', NULL, NULL, NULL, 0, 0, 0, 'C:\\Users\\Youssef\\Downloads\\GestionUtilisateurs_JavaFX_JDBC\\GestionUtilisateurs\\src\\main\\resources\\com\\elearning\\avatars\\avatar_kamel_bouzidi_.svg', NULL, '', '2026-04-26 16:45:02', '2026-04-26 16:45:02', 1, NULL, 'ACTIF', 0, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `username`, `role`) VALUES
(1, 'Admin', 'ADMIN'),
(2, 'Etudiant', 'ETUDIANT'),
(3, 'Enseignant', 'ENSEIGNANT');

-- --------------------------------------------------------

--
-- Structure de la table `user_formation`
--

CREATE TABLE `user_formation` (
  `user_id` int(11) NOT NULL,
  `formation_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `wallet`
--

CREATE TABLE `wallet` (
  `id` int(11) NOT NULL,
  `balance` decimal(10,2) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_by_id` int(11) NOT NULL,
  `updated_by_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `wallet_transaction`
--

CREATE TABLE `wallet_transaction` (
  `id` int(11) NOT NULL,
  `credits` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` varchar(50) NOT NULL,
  `stripe_session_id` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `completed_at` datetime DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `updated_at` datetime NOT NULL,
  `created_by_id` int(11) NOT NULL,
  `updated_by_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `ai_recommendation`
--
ALTER TABLE `ai_recommendation`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `certificate`
--
ALTER TABLE `certificate`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_219CDA4AA76ED395` (`user_id`),
  ADD KEY `IDX_219CDA4A5200282E` (`formation_id`),
  ADD KEY `IDX_219CDA4A853CD175` (`quiz_id`);

--
-- Index pour la table `chapitre`
--
ALTER TABLE `chapitre`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_chapitre` (`cours_id`);

--
-- Index pour la table `club`
--
ALTER TABLE `club`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_B8EE387261220EA6` (`creator_id`);

--
-- Index pour la table `club_members`
--
ALTER TABLE `club_members`
  ADD PRIMARY KEY (`club_id`,`user_id`),
  ADD KEY `IDX_48E8777D61190A32` (`club_id`),
  ADD KEY `IDX_48E8777DA76ED395` (`user_id`);

--
-- Index pour la table `club_membership`
--
ALTER TABLE `club_membership`
  ADD PRIMARY KEY (`user_id`,`club_id`);

--
-- Index pour la table `cour`
--
ALTER TABLE `cour`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_A71F964F61220EA6` (`creator_id`);

--
-- Index pour la table `cours`
--
ALTER TABLE `cours`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `course`
--
ALTER TABLE `course`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `evaluation`
--
ALTER TABLE `evaluation`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `event`
--
ALTER TABLE `event`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_3BAE0AA761190A32` (`club_id`),
  ADD KEY `IDX_3BAE0AA761220EA6` (`creator_id`);

--
-- Index pour la table `formation`
--
ALTER TABLE `formation`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_404021BF61220EA6` (`creator_id`);

--
-- Index pour la table `historique`
--
ALTER TABLE `historique`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_cours` (`id_cours`);

--
-- Index pour la table `join_request`
--
ALTER TABLE `join_request`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_E932E4FF61190A32` (`club_id`),
  ADD KEY `IDX_E932E4FFA76ED395` (`user_id`);

--
-- Index pour la table `login_attempt`
--
ALTER TABLE `login_attempt`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750` (`queue_name`,`available_at`,`delivered_at`,`id`);

--
-- Index pour la table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`idQuestion`),
  ADD KEY `fk_question_quiz` (`idQuiz`);

--
-- Index pour la table `question_quiz`
--
ALTER TABLE `question_quiz`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_FAFC177D853CD175` (`quiz_id`);

--
-- Index pour la table `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`idQuiz`);

--
-- Index pour la table `quiz_assessment`
--
ALTER TABLE `quiz_assessment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_BE3D1A4C61220EA6` (`creator_id`);

--
-- Index pour la table `quiz_for`
--
ALTER TABLE `quiz_for`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_A412FA925200282E` (`formation_id`);

--
-- Index pour la table `quiz_resultat`
--
ALTER TABLE `quiz_resultat`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_311FA4A7CB944F1A` (`student_id`),
  ADD KEY `IDX_311FA4A7853CD175` (`quiz_id`),
  ADD KEY `IDX_311FA4A7B03A8386` (`created_by_id`),
  ADD KEY `IDX_311FA4A7896DBBDE` (`updated_by_id`);

--
-- Index pour la table `reponse`
--
ALTER TABLE `reponse`
  ADD PRIMARY KEY (`idReponse`),
  ADD KEY `fk_reponse_question` (`idQuestion`);

--
-- Index pour la table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_7CE748AA76ED395` (`user_id`);

--
-- Index pour la table `ressource`
--
ALTER TABLE `ressource`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_939F45445200282E` (`formation_id`),
  ADD KEY `IDX_939F4544B03A8386` (`created_by_id`),
  ADD KEY `IDX_939F4544896DBBDE` (`updated_by_id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_IDENTIFIER_EMAIL` (`email`),
  ADD KEY `IDX_8D93D649B03A8386` (`created_by_id`),
  ADD KEY `IDX_8D93D649896DBBDE` (`updated_by_id`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `user_formation`
--
ALTER TABLE `user_formation`
  ADD PRIMARY KEY (`user_id`,`formation_id`),
  ADD KEY `IDX_40A0AC5BA76ED395` (`user_id`),
  ADD KEY `IDX_40A0AC5B5200282E` (`formation_id`);

--
-- Index pour la table `wallet`
--
ALTER TABLE `wallet`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_7C68921FA76ED395` (`user_id`),
  ADD KEY `IDX_7C68921FB03A8386` (`created_by_id`),
  ADD KEY `IDX_7C68921F896DBBDE` (`updated_by_id`);

--
-- Index pour la table `wallet_transaction`
--
ALTER TABLE `wallet_transaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_7DAF972A76ED395` (`user_id`),
  ADD KEY `IDX_7DAF972B03A8386` (`created_by_id`),
  ADD KEY `IDX_7DAF972896DBBDE` (`updated_by_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `ai_recommendation`
--
ALTER TABLE `ai_recommendation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=52;

--
-- AUTO_INCREMENT pour la table `certificate`
--
ALTER TABLE `certificate`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `chapitre`
--
ALTER TABLE `chapitre`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `club`
--
ALTER TABLE `club`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `cour`
--
ALTER TABLE `cour`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `cours`
--
ALTER TABLE `cours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT pour la table `course`
--
ALTER TABLE `course`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `evaluation`
--
ALTER TABLE `evaluation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `event`
--
ALTER TABLE `event`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `formation`
--
ALTER TABLE `formation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `historique`
--
ALTER TABLE `historique`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT pour la table `join_request`
--
ALTER TABLE `join_request`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `login_attempt`
--
ALTER TABLE `login_attempt`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=71;

--
-- AUTO_INCREMENT pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `question`
--
ALTER TABLE `question`
  MODIFY `idQuestion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `question_quiz`
--
ALTER TABLE `question_quiz`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `quiz`
--
ALTER TABLE `quiz`
  MODIFY `idQuiz` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `quiz_assessment`
--
ALTER TABLE `quiz_assessment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `quiz_for`
--
ALTER TABLE `quiz_for`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `quiz_resultat`
--
ALTER TABLE `quiz_resultat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `reponse`
--
ALTER TABLE `reponse`
  MODIFY `idReponse` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `ressource`
--
ALTER TABLE `ressource`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `wallet`
--
ALTER TABLE `wallet`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `wallet_transaction`
--
ALTER TABLE `wallet_transaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `certificate`
--
ALTER TABLE `certificate`
  ADD CONSTRAINT `FK_219CDA4A5200282E` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`),
  ADD CONSTRAINT `FK_219CDA4A853CD175` FOREIGN KEY (`quiz_id`) REFERENCES `quiz_for` (`id`),
  ADD CONSTRAINT `FK_219CDA4AA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `club`
--
ALTER TABLE `club`
  ADD CONSTRAINT `FK_B8EE387261220EA6` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `club_members`
--
ALTER TABLE `club_members`
  ADD CONSTRAINT `FK_48E8777D61190A32` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_48E8777DA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `cour`
--
ALTER TABLE `cour`
  ADD CONSTRAINT `FK_A71F964F61220EA6` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `event`
--
ALTER TABLE `event`
  ADD CONSTRAINT `FK_3BAE0AA761190A32` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_3BAE0AA761220EA6` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `formation`
--
ALTER TABLE `formation`
  ADD CONSTRAINT `FK_404021BF61220EA6` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `historique`
--
ALTER TABLE `historique`
  ADD CONSTRAINT `historique_ibfk_1` FOREIGN KEY (`id_cours`) REFERENCES `cours` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `join_request`
--
ALTER TABLE `join_request`
  ADD CONSTRAINT `FK_E932E4FF61190A32` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_E932E4FFA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `fk_question_quiz` FOREIGN KEY (`idQuiz`) REFERENCES `quiz` (`idQuiz`) ON DELETE CASCADE;

--
-- Contraintes pour la table `question_quiz`
--
ALTER TABLE `question_quiz`
  ADD CONSTRAINT `FK_FAFC177D853CD175` FOREIGN KEY (`quiz_id`) REFERENCES `quiz_assessment` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `quiz_assessment`
--
ALTER TABLE `quiz_assessment`
  ADD CONSTRAINT `FK_BE3D1A4C61220EA6` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `quiz_for`
--
ALTER TABLE `quiz_for`
  ADD CONSTRAINT `FK_A412FA925200282E` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `quiz_resultat`
--
ALTER TABLE `quiz_resultat`
  ADD CONSTRAINT `FK_311FA4A7853CD175` FOREIGN KEY (`quiz_id`) REFERENCES `quiz_assessment` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_311FA4A7896DBBDE` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_311FA4A7B03A8386` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_311FA4A7CB944F1A` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `reponse`
--
ALTER TABLE `reponse`
  ADD CONSTRAINT `fk_reponse_question` FOREIGN KEY (`idQuestion`) REFERENCES `question` (`idQuestion`) ON DELETE CASCADE;

--
-- Contraintes pour la table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  ADD CONSTRAINT `FK_7CE748AA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `ressource`
--
ALTER TABLE `ressource`
  ADD CONSTRAINT `FK_939F45445200282E` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_939F4544896DBBDE` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_939F4544B03A8386` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `FK_8D93D649896DBBDE` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_8D93D649B03A8386` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `user_formation`
--
ALTER TABLE `user_formation`
  ADD CONSTRAINT `FK_40A0AC5B5200282E` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_40A0AC5BA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `wallet`
--
ALTER TABLE `wallet`
  ADD CONSTRAINT `FK_7C68921F896DBBDE` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_7C68921FA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_7C68921FB03A8386` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `wallet_transaction`
--
ALTER TABLE `wallet_transaction`
  ADD CONSTRAINT `FK_7DAF972896DBBDE` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_7DAF972A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_7DAF972B03A8386` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

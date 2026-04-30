# 🎓 Eduverse — Documentation Complète

## 🚀 Comment lancer ?
1. **Base de données** : Importer le fichier `schema.sql` (ou `migration_vers_eduverse.sql`) dans votre serveur MySQL pour créer la base `eduverse-java`.
2. **Configuration** : Vérifiez les identifiants de la base dans `src/main/java/com/elearning/util/DatabaseConnection.java` (url, user, password).
3. **Compilation & Lancement** : Exécutez `mvn clean compile javafx:run` (ou lancez `MainApp.java` directement depuis votre IDE JavaFX).

## 🌐 APIs (3 APIs)

### API 1 — DiceBear (Avatar)
- **C'est quoi ?** Génération d'avatars uniques basés sur le nom de l'utilisateur.
- **Fichier :** `AvatarService.java`
- **Fonctionne sans clé API**

### API 2 — Abstract (Validation email)
- **C'est quoi ?** Vérification de l'existence et de la validité réelle d'une adresse email.
- **Fichier :** `EmailValidationService.java`
- **Clé :** abstractapi.com → mettre dans `EmailValidationService.java`

### API 3 — Groq / LLaMA 3.1 (IA)
- **C'est quoi ?** API Cloud ultra-rapide hébergeant le modèle LLaMA 3.1 pour nos assistants virtuels.
- **Fichiers :** `AIAssistantService.java`, `AIRecommendationService.java`
- **Clé :** console.groq.com → mettre dans `AIAssistantService.java` et `AIRecommendationService.java`

## ✨ Fonctionnalités Avancées (6 FA)

### FA 1 — CAPTCHA Puzzle Orientation
- **C'est quoi ?** Système anti-bot où l'utilisateur doit faire tourner une image à l'aide d'un slider pour la remettre à l'endroit.
- **Fichier :** `CaptchaGenerator.java`
- **Présent sur :** Login + Inscription

### FA 2 — Force du Mot de Passe
- **C'est quoi ?** Évaluation en temps réel de la robustesse du mot de passe avec des règles strictes (maj, min, chiffres, caractères spéciaux).
- **Fichier :** `PasswordStrengthChecker.java`
- **5 niveaux :** Très faible → Très fort
- **Présent sur :** Inscription + Reset + Formulaire

### FA 3 — Reset Password
- **C'est quoi ?** Parcours permettant à l'utilisateur de définir un nouveau mot de passe via l'envoi d'un token sécurisé par email.
- **Fichiers :** `PasswordResetService.java`, `PasswordResetDAO.java`
- **Vues :** `ForgotPasswordView.fxml`, `ResetPasswordView.fxml`
- **Config SMTP :** Mailtrap

### FA 4 — 2FA OTP Email
- **C'est quoi ?** Authentification à deux facteurs nécessitant un code temporaire envoyé par mail.
- **Fichier :** `TwoFactorService.java`
- **Flux :** Login → Code email → Dashboard
- **Config :** Mailtrap

### FA 5 — Face ID (Reconnaissance Faciale)
- **C'est quoi ?** Authentification biométrique par reconnaissance du visage de l'utilisateur en temps réel (via webcam).
- **Fichier :** `FaceRecognitionService.java`
- **Algorithme :** LBPH OpenCV (20 échantillons)
- **1 visage = 1 compte** (unicité garantie)
- **Flux enregistrement :** Ouverture caméra → Vérification unicité → Capture 20 échantillons → Modèle entraîné.
- **Flux connexion :** Saisie email → Scan webcam → Comparaison modèle → Redirection ou Rejet.

### FA 6 — Protection Brute Force
- **C'est quoi ?** Blocage temporaire d'un compte après un nombre excessif de tentatives de connexion échouées.
- **Fichiers :** `LoginAttemptDAO.java`, `UserService.java`
- **Règle :** 5 échecs → blocage 15 minutes

## 🤖 Intelligence Artificielle (2 IAs)

### IA 1 — Assistant Chat
- **C'est quoi ?** Un chatbot interactif capable de répondre aux questions pédagogiques de l'utilisateur.
- **Fichier :** `AIAssistantService.java`
- **Connectée à la BDD :** Explique les concepts de la formation et conserve l'historique des requêtes (dans la table `ai_conversation`).
- **Exemple question :** "Qu'est-ce que Java ?" → **Réponse :** "Java est un langage orienté objet multiplateforme..."

### IA 2 — Recommandations Automatiques
- **C'est quoi ?** Une IA proactive qui analyse l'activité de l'utilisateur pour suggérer des actions, sans que l'utilisateur ne pose de question.
- **Fichier :** `AIRecommendationService.java`
- **Se déclenche :** Automatiquement au démarrage du dashboard (Admin/Enseignant/Etudiant).
- **3 types de recommandations :** 1. *Sécurité* (ex: "Activez le 2FA"). 2. *Contenu* (ex: "Continuez votre cours de Java"). 3. *Modération* (ex: "3 utilisateurs suspects bloqués").

## 🏗️ Architecture
- **Entity** : Classes Java simples représentant les tables SQL (données pures).
- **DAO** : *Data Access Object*, contient exclusivement les requêtes SQL (CRUD) vers la BDD.
- **Service** : Logique métier complexe (règles de gestion, IA, emails, mots de passe).
- **Controller** : Logique de l'interface graphique JavaFX (événements boutons, validations visuelles).
- **FXML** : Fichiers XML définissant le placement et le design de l'interface.

## 🗄️ Base de Données (eduverse-java)
Toutes les tables avec colonnes importantes :
- **user** : `id`, `full_name`, `email`, `password`, `role`, `statut`, `is_approved`, `is_blocked`, `picture`, `roles`
- **login_attempt** : `id`, `user_id`, `attempts`, `last_attempt`, `lock_until`
- **password_reset_token** : `id`, `user_id`, `token`, `expires_at`, `created_at`
- **ai_conversation** : `id`, `user_id`, `prompt`, `response`, `created_at`
- **ai_recommendation** : `id`, `priorite`, `type`, `titre`, `description`, `action_suggeree`, `lue`

## ❓ 10 Questions soutenance + Réponses

**Q1 : Pourquoi Platform.runLater() ?**
Parce que JavaFX exige que toute modification de l'interface graphique (texte, couleurs, images) soit effectuée uniquement par le "Thread UI" principal.

**Q2 : C'est quoi LBPH ?**
Local Binary Patterns Histograms est un algorithme robuste de reconnaissance faciale qui analyse la texture du visage au lieu de simples formes.

**Q3 : Pourquoi BCrypt ?**
C'est un algorithme de hachage unidirectionnel sécurisé intégrant un "sel" aléatoire, rendant les mots de passe insensibles aux attaques par dictionnaire.

**Q4 : Comment l'IA est connectée à la BDD ?**
Elle utilise des Services/DAO pour lire l'historique et enregistrer ses nouvelles réponses directement dans les tables `ai_conversation` ou `ai_recommendation`.

**Q5 : Pourquoi le pattern DAO ?**
Il sépare la logique d'accès à la base de données (les requêtes SQL) du reste du code métier, rendant le projet plus clair et plus facile à maintenir.

**Q6 : C'est quoi un Thread en Java ?**
C'est un processus léger permettant au programme d'exécuter des tâches en arrière-plan (ex: l'enregistrement vidéo) sans bloquer l'interface de l'application.

**Q7 : Pourquoi PreparedStatement ?**
Il précompile la requête SQL et insère les variables de manière sécurisée, ce qui bloque totalement les failles d'injection SQL.

**Q8 : Comment fonctionne le 2FA ?**
À la connexion, un code temporaire (OTP) est généré, stocké en BDD et envoyé par email via Mailtrap. L'accès est bloqué tant qu'il n'est pas saisi.

**Q9 : C'est quoi le CAPTCHA ?**
C'est un test visuel généré dynamiquement sur un Canvas JavaFX demandant à l'utilisateur de prouver qu'il est humain (en redressant une image).

**Q10 : Comment fonctionne le Face ID ?**
Il détecte d'abord le visage, vérifie qu'il n'est pas déjà assigné, capture 20 images pour entraîner un modèle, puis compare les nouveaux scans à ce modèle (seuil).

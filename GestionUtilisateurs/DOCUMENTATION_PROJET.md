# Documentation du projet — Gestion des Utilisateurs (JavaFX + JDBC)

## 1. Architecture générale (référence Workshop JavaFX slide 3)
L'application repose sur une architecture MVC (Modèle-Vue-Contrôleur) classique :
- **Model** : La classe `com.elearning.entity.User.java` qui représente la structure des données (POJO).
- **View** : Les interfaces graphiques sont conçues en XML via SceneBuilder (fichiers `.fxml` dans `src/main/resources/com/elearning/gui/`).
- **Controller** : Les classes Java (`*Controller.java`) qui lient les actions de l'utilisateur (boutons, champs) à la logique métier grâce à l'annotation `@FXML`.

## 2. Configuration et dépendances (référence Workshop JavaFX slides 7-8)
Le fichier `pom.xml` gère les dépendances Maven :
- **javafx-controls** et **javafx-fxml** : Nécessaires pour utiliser les composants graphiques JavaFX.
- **mysql-connector-j** : Le driver JDBC permettant à l'application Java de communiquer avec le serveur MySQL.
- **Plugin javafx-maven-plugin** : Permet de compiler et d'exécuter l'application facilement via des commandes Maven.

## 3. Point d'entrée — MainApp.java
La classe principale du programme `MainApp` hérite de la classe `Application` de JavaFX :
- **extends Application** : Obligatoire pour toute application JavaFX.
- **start(Stage)** : C'est la méthode de démarrage. Le `Stage` reçu en paramètre représente la fenêtre Windows/Mac principale.
- La classe s'occupe de charger le premier fichier, `LoginView.fxml`, de lui appliquer une feuille de style (`style.css`), et d'afficher l'interface.

## 4. Connexion à la base de données (référence Workshop JDBC slides 9-15)
La connexion est gérée par la classe utilitaire `DatabaseConnection.java` en utilisant le **Pattern Singleton** :
- **DriverManager.getConnection()** : Permet de se connecter avec l'URL pointant vers le serveur local, le login (`root`) et le mot de passe.
- **Exception SQLException** : Chaque tentative de connexion est entourée d'un `try/catch` pour intercepter les erreurs (serveur éteint, mauvais mot de passe).
- Le design pattern Singleton permet d'éviter l'ouverture de multiples connexions simultanées, ce qui surchargerait inutilement le serveur MySQL.

## 5. Entité User.java (référence Workshop JDBC slide 23)
C'est la classe qui représente la table `users` de la base de données.
- Elle contient les attributs avec des types simples correspondant aux types SQL (`String` pour `VARCHAR`, `int` pour `INT`, `LocalDateTime` pour `DATETIME`).
- Tous les attributs sont privés (encapsulation) et la classe dispose de constructeurs complets ainsi que des méthodes **getters et setters**.

## 6. DAO — UserDAO.java
Le package Data Access Object centralise toutes les requêtes SQL :
- Utilisation systématique de `PreparedStatement` pour sécuriser les exécutions (ex: éviter les injections SQL) au lieu d'un simple `Statement`.
- **Exemple : `ajouterUser(User)`** : 
  On prépare une requête `INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)`. Les `?` sont remplacés dynamiquement via `stmt.setString(1, user.getFullName())` avant l'exécution avec `stmt.executeUpdate()`.

## 7. Service — UserService.java
C'est l'étage contenant toute la  **logique métier** et les règles de validation :
- Avant d'appeler le DAO pour insérer ou modifier un utilisateur, on passe par les méthodes de UserService.
- Par exemple, la vérification du format de l'e-mail ou la vérification que le mot de passe contient bien une majuscule et un caractère spécial se font ici.

## 8. Contrôleurs JavaFX
Ce sont le cœur interactif de l'application :
- **LoginController** : Capture l'événement clic sur le bouton de connexion, vérifie l'existence de l'utilisateur, et charge dynamiquement le prochain FXML (`AdminDashboardView`, `EtudiantDashboardView`...) selon la règle de rôle.
- **AdminDashboardController** : Le gros contrôleur gérant le tableau de bord avec des graphes textuels, les totaux, et le grand DataGrid.
- **UserFormController** : Le contrôleur du popup (fenêtre modale) pour l'ajout et l'édition rapide.

## 9. Interface utilisateur (référence Workshop JavaFX slides 19-22)
Le Tableau principal de `AdminDashboardView.fxml` est géré avec JavaFX standard :
- **TableView** avec une liste d'accompagnement appelée **ObservableList**. Quand un objet est enlevé de cette liste en mémoire, le tableau disparaît de l'UI instantanément sans qu'il faille tout recoder.
- **PropertyValueFactory** : Indique à une colonne `<TableColumn>` de récupérer automatiquement l'attribut `fullName` via `getFullName()` sur l'entité User.

## 10. Fonctionnalités supplémentaires
- **Recherche dynamique** : Dans `AdminDashboardController`, un *Listener* est attaché à la propriété texte du champ de recherche. Dès qu'on tape une lettre, le tableau se filtre automatiquement.
- **Filtrage par rôle** : Une `ComboBox` liée à la clause SQL `WHERE role = ?` permet de trier par Admin/Enseignant/Etudiant.
- **Tri par colonnes** : Possibilité de classer par DATE ASC ou DESC.
- **Export PDF** : Intégration de la librairie iText pour générer un fichier physique des résultats de la recherche.

## 11. Sécurité
L'application adopte des standards de développement sécurisés :
- **Protection par requêtes préparées (`PreparedStatement`)** : Empêche formellement de faire chuter la BDD via des injections SQL malicieuses en traitant les paramètres comme du simple texte.
- **Hachage cryptographique** : Utilisation du standard SHA-256 (via `MessageDigest`) pour enregistrer l'empreinte irrémédiable du mot de passe en base (on ne sauvegarde jamais en texte clair).
- **Contrôle de saisie strict** : Aucun champ vide n'est toléré, le niveau de solidité du mot de passe est vérifié.

## 12. Git collaboratif
L'intégralité du développement a été conservée sous la forme de l'historique sur Git :
- Développement sur une branche séparée (`branche_youssef` -> `branche_admin`) pour simuler un travail d'équipe et éviter les conflits dans un master.
- Commits granulaires (ex: "Fix Layout", "Add TableView", "Hash passwords").

## 13. Glossaire des termes techniques
- **POJO** : Plain Old Java Object, une simple classe avec des getters et des setters.
- **MVC** : Modèle - Vue - Contrôleur, un patron simplifiant la compréhension de l'architecture.
- **JDBC** : Java Database Connectivity, l'API Java standard pour la gestion des BDD relationnelles.
- **Singleton** : Patron de conception garantissant l'existence d'une seule instance en mémoire.
- **SHA-256** : Algorithme de hachage transformant un texte en une chaîne hexadécimale fixe non inversible.

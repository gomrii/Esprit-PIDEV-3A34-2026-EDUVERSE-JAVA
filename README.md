# EduVerse JavaFX

Application e-learning desktop construite en JavaFX, Maven et MySQL.

Le projet regroupe plusieurs espaces fonctionnels dans une meme application :
- administration des utilisateurs
- dashboard et navigation par role
- formations
- quiz
- cours et chapitres
- clubs et evenements
- outils IA, voix, paiement et services annexes

## Vue d'ensemble

Le point d'entree principal est [MainApp.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/MainApp.java).

L'application charge d'abord l'ecran de connexion, puis redirige l'utilisateur vers un espace partage avec sidebar selon son role :
- Admin
- Etudiant
- Enseignant

Le shell principal recent est base sur [MainDashboard.fxml](/C:/Users/YOSRA/Downloads/javafx1/src/main/resources/MainDashboard.fxml) et [MainDashboardController.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Controllers/MainDashboardController.java).

## Stack technique

- Java 17
- Maven
- JavaFX 21
- MySQL 8
- JDBC
- JUnit 5
- iText / PDFBox
- Gson / Jackson
- OkHttp / Apache HttpClient 5
- Stripe
- Groq AI
- JavaCV / OpenCV
- Twilio
- ZXing

## Structure du projet

Le depot contient deux ensembles historiques de packages qui cohabitent :

1. `com.elearning.*`
   - authentification
   - utilisateurs
   - formations
   - services transverses
   - vues JavaFX principales sous `src/main/resources/com/elearning/gui`

2. `Controllers`, `Services`, `Entities`, `Utils`
   - quiz
   - cours / chapitres
   - clubs / evenements
   - dashboard partage
   - vues FXML directement sous `src/main/resources`

Arborescence utile :

```text
javafx1/
|-- pom.xml
|-- config/
|   |-- api.properties
|   `-- api.properties.example
|-- database/
|   |-- eduverse-2.sql
|   |-- schema.sql
|   `-- migrations quiz...
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   |-- com/elearning/
|   |   |   |-- Controllers/
|   |   |   |-- Services/
|   |   |   |-- Entities/
|   |   |   `-- Utils/
|   |   `-- resources/
|   |       |-- com/elearning/gui/
|   |       |-- com/elearning/css/
|   |       |-- css/
|   |       |-- html/
|   |       |-- scripts/
|   |       `-- *.fxml
|   `-- test/
|       `-- java/com/elearning/service/
`-- docs *.md
```

## Modules principaux

### 1. Authentification et utilisateurs

Fichiers principaux :
- [LoginController.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/controller/LoginController.java)
- [UserService.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/service/UserService.java)
- [UserDAO.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/dao/UserDAO.java)
- [SessionManager.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/util/SessionManager.java)

Fonctionnalites :
- connexion par role
- inscription
- mot de passe oublie / reset
- double facteur
- gestion des utilisateurs
- validation et statuts de compte

### 2. Shell de navigation

Fichiers principaux :
- [MainDashboard.fxml](/C:/Users/YOSRA/Downloads/javafx1/src/main/resources/MainDashboard.fxml)
- [MainDashboardController.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Controllers/MainDashboardController.java)
- [style.css](/C:/Users/YOSRA/Downloads/javafx1/src/main/resources/css/style.css)

Fonctionnalites :
- sidebar commune
- menu dynamique selon le role
- chargement du contenu au centre via `BorderPane`
- theme global JavaFX

### 3. Formations

Fichiers principaux :
- [FormationDashboardController.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/controller/FormationDashboardController.java)
- [FormationService.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/service/FormationService.java)
- [FormationDAO.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/dao/FormationDAO.java)

Fonctionnalites :
- liste des formations
- details
- creation / edition
- inscription
- IA pour certaines descriptions

### 4. Quiz

Fichiers principaux :
- [AdminQuizListController.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Controllers/AdminQuizListController.java)
- [TeacherQuizListController.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Controllers/TeacherQuizListController.java)
- [StudentQuizListController.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Controllers/StudentQuizListController.java)
- [QuizService.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Services/QuizService.java)

Fonctionnalites :
- liste des quiz par espace
- creation / modification
- questions / reponses
- passage et resultat cote etudiant
- statistiques quiz
- generation IA

### 5. Cours, chapitres, clubs et evenements

Packages principaux :
- `Controllers`
- `Services`
- `Entities`

Fonctionnalites :
- cours admin / enseignant / etudiant
- chapitres
- clubs
- evenements
- calendrier
- demandes et validation

### 6. Integrations optionnelles

Le depot contient aussi :
- Stripe pour paiement
- Groq AI
- chatbot
- synthese vocale et speech-to-text
- reconnaissance faciale
- QR code
- traduction

Ces fonctions ne sont pas toutes necessaires pour lancer l'application de base, mais certaines exigent une configuration locale supplementaire.

## Prerequis

- JDK 17
- Maven 3.8+
- MySQL 8+
- IntelliJ IDEA recommande

## Configuration base de donnees

Deux classes de connexion existent actuellement :
- [DatabaseConnection.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/util/DatabaseConnection.java)
- [MyConnection.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Utils/MyConnection.java)

Elles pointent toutes les deux vers la base `eduverse` sur `localhost:3306` avec :
- utilisateur : `root`
- mot de passe : vide par defaut

### 1. Creer la base

```sql
CREATE DATABASE IF NOT EXISTS eduverse
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;
```

### 2. Importer le schema principal

Le schema le plus complet du depot est :
- `database/eduverse-2.sql`

Commande typique :

```bash
mysql -u root -p eduverse < database/eduverse-2.sql
```

Selon votre module de travail, des scripts complementaires existent aussi dans `database/`.

### 3. Adapter les identifiants si necessaire

Verifier ces fichiers :
- [DatabaseConnection.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/com/elearning/util/DatabaseConnection.java)
- [MyConnection.java](/C:/Users/YOSRA/Downloads/javafx1/src/main/java/Utils/MyConnection.java)

## Configuration API optionnelle

Le projet charge des cles depuis :
- `config/api.properties`
- ou des variables d'environnement

Un modele existe dans :
- `config/api.properties.example`

Attention :
- ne pas commiter de vraies cles
- le fichier exemple present dans le depot doit etre nettoye avant un usage public s'il contient des secrets reels

Integrations visees :
- Stripe
- Groq AI

Details :
- [API_INTEGRATION_GUIDE.md](/C:/Users/YOSRA/Downloads/javafx1/API_INTEGRATION_GUIDE.md)

## Lancer le projet

### Avec Maven

```bash
mvn clean compile
mvn javafx:run
```

Le `mainClass` Maven est configure sur :
- `com.elearning.MainApp`

### Avec IntelliJ

1. Ouvrir le dossier du projet.
2. Laisser IntelliJ importer le `pom.xml`.
3. Configurer le SDK en Java 17.
4. Lancer `com.elearning.MainApp`.

## Tests

Tests disponibles :
- [UserServiceTest.java](/C:/Users/YOSRA/Downloads/javafx1/src/test/java/com/elearning/service/UserServiceTest.java)

Commande :

```bash
mvn test
```

## Ressources et styles

Le projet utilise plusieurs repertoires de styles :
- `src/main/resources/com/elearning/css/style.css`
- `src/main/resources/css/style.css`

Le second couvre notamment le shell partage et plusieurs vues recentes du dashboard et du module quiz.

## Scripts et assets annexes

Ressources presentes dans le depot :
- `src/main/resources/scripts/` : scripts Python pour voix / accessibilite / quiz
- `src/main/resources/html/map.html` : contenu HTML integre
- `avatars/`, `faces/` : assets locaux

Certaines fonctions dependantes de Python ou de bibliotheques systeme peuvent necessiter une installation manuelle hors Maven.

## Documentation utile du depot

- [DATABASE_SETUP.md](/C:/Users/YOSRA/Downloads/javafx1/DATABASE_SETUP.md)
- [DATABASE_SCHEMA.md](/C:/Users/YOSRA/Downloads/javafx1/DATABASE_SCHEMA.md)
- [DATABASE_QUICKSTART.md](/C:/Users/YOSRA/Downloads/javafx1/DATABASE_QUICKSTART.md)
- [API_INTEGRATION_GUIDE.md](/C:/Users/YOSRA/Downloads/javafx1/API_INTEGRATION_GUIDE.md)
- [QUICK_REFERENCE.md](/C:/Users/YOSRA/Downloads/javafx1/QUICK_REFERENCE.md)
- [IMPLEMENTATION_SUMMARY.md](/C:/Users/YOSRA/Downloads/javafx1/IMPLEMENTATION_SUMMARY.md)

## Points d'attention

- Le projet n'est pas encore uniformise sur une seule convention de packages.
- Plusieurs modules partagent la meme base `eduverse`.
- Certaines vues et certains services historiques coexistent avec le shell recent.
- Avant un deploiement ou une publication, il faut verifier les secrets dans `config/` et nettoyer les fichiers sensibles.

## Prochaine etape recommandee

Si vous poursuivez le projet, le travail utile est de normaliser :
- la configuration base de donnees
- la gestion des secrets
- la structure des packages
- les styles CSS dupliques
- le point d'entree des modules annexes

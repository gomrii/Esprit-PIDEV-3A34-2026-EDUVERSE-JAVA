# 🎓 Module Gestion des Utilisateurs — JavaFX + JDBC
## Projet Eduverse · Migration Symfony → Java

---

## 📁 Structure du projet

```
GestionUtilisateurs/
│
├── pom.xml                          ← Dépendances Maven (JavaFX, MySQL, iText)
├── database/
│   └── schema.sql                   ← Script SQL de création de la table + données de test
│
└── src/
    ├── main/java/com/elearning/
    │   ├── MainApp.java              ← Point d'entrée (lance l'application JavaFX)
    │   │
    │   ├── entity/
    │   │   └── User.java             ← Entité POJO (≈ Entity Symfony)
    │   │
    │   ├── dao/
    │   │   └── UserDAO.java          ← Accès BDD JDBC (≈ UserRepository Symfony)
    │   │
    │   ├── service/
    │   │   ├── UserService.java      ← Logique métier + validation (≈ Services Symfony)
    │   │   └── PdfExportService.java ← Export PDF avec iText (≈ PdfService Symfony)
    │   │
    │   ├── controller/
    │   │   ├── LoginController.java           ← Page connexion
    │   │   ├── AdminDashboardController.java  ← Tableau de bord admin
    │   │   ├── UserFormController.java        ← Formulaire ajout/modification
    │   │   └── SimpleViewController.java      ← Vues enseignant/étudiant
    │   │
    │   └── util/
    │       ├── DatabaseConnection.java  ← Singleton connexion MySQL
    │       └── SessionManager.java     ← Gestion utilisateur connecté
    │
    ├── main/resources/com/elearning/
    │   ├── gui/
    │   │   ├── LoginView.fxml               ← Page de connexion
    │   │   ├── AdminDashboardView.fxml      ← Tableau de bord admin
    │   │   ├── UserFormView.fxml            ← Formulaire ajout/modif
    │   │   ├── EnseignantDashboardView.fxml ← Espace enseignant
    │   │   └── EtudiantDashboardView.fxml   ← Espace étudiant
    │   └── css/
    │       └── style.css                    ← Styles CSS JavaFX
    │
    └── test/java/com/elearning/service/
        └── UserServiceTest.java             ← Tests unitaires JUnit 5
```

---

## ⚙️ Prérequis

| Outil | Version minimale | Lien |
|---|---|---|
| JDK | 17 (LTS) | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org |
| MySQL | 8.0+ | https://dev.mysql.com |
| IntelliJ IDEA | 2022+ (recommandé) | https://jetbrains.com |
| Scene Builder | 17+ (optionnel) | https://gluonhq.com/products/scene-builder |

---

## 🚀 Installation pas à pas

### Étape 1 — Créer la base de données

Ouvrez MySQL Workbench ou votre terminal MySQL et exécutez :

```sql
-- Copier-coller le contenu de database/schema.sql
source /chemin/vers/GestionUtilisateurs/database/schema.sql;
```

### Étape 2 — Configurer la connexion

Ouvrez `src/main/java/com/elearning/util/DatabaseConnection.java` et ajustez :

```java
private static final String URL      = "jdbc:mysql://localhost:3306/elearning?...";
private static final String USER     = "root";      // votre utilisateur MySQL
private static final String PASSWORD = "";           // votre mot de passe MySQL
```

### Étape 3 — Installer les dépendances Maven

```bash
cd GestionUtilisateurs
mvn clean install
```

### Étape 4 — Lancer l'application

```bash
mvn javafx:run
```

Ou dans IntelliJ :
1. Ouvrir le projet (File → Open → sélectionner le dossier GestionUtilisateurs)
2. Maven se charge automatiquement
3. Ouvrir `MainApp.java` → clic droit → Run

---

## 🔑 Comptes de démonstration

| Email | Mot de passe | Rôle | Résultat |
|---|---|---|---|
| admin@elearning.tn | Admin123! | ADMIN | → Dashboard Admin complet |
| ahmed@elearning.tn | Admin123! | ENSEIGNANT | → Espace Enseignant |
| sarra@elearning.tn | Admin123! | ETUDIANT | → Espace Étudiant |
| amine@elearning.tn | Admin123! | ETUDIANT | ❌ Compte en attente |
| fatma@elearning.tn | Admin123! | ETUDIANT | ❌ Compte bloqué |

> **Note :** Le script SQL insère des hash SHA-256 pour "Admin123!". Si vous ajoutez des utilisateurs via l'interface, les hash sont générés automatiquement.

---

## 📋 Fonctionnalités implémentées

### ✅ CRUD complet (4 pts)
| Opération | Où | Comment |
|---|---|---|
| **Créer** | Bouton "➕ Ajouter" | Formulaire modal avec validation |
| **Lire** | TableView | Chargement automatique au démarrage |
| **Modifier** | Bouton "✏ Modifier" | Formulaire pré-rempli |
| **Supprimer** | Bouton "🗑 Supprimer" | Confirmation requise |

### ✅ Contrôle de saisie (2 pts)
- Champs obligatoires (nom, email, mot de passe, rôle)
- Format email validé par regex
- Mot de passe fort : 8+ chars, majuscule, chiffre, caractère spécial
- Test d'unicité email (un email ne peut pas être utilisé deux fois)
- Messages d'erreur inline par champ
- Validation en temps réel sur l'email (au clic hors du champ)

### ✅ Interface graphique JavaFX (4 pts)
- Page de connexion stylisée
- Dashboard admin avec sidebar + cartes statistiques
- TableView avec coloration conditionnelle du statut
- Formulaire modal réutilisable (créer + modifier)
- CSS complet : couleurs, hover, responsive

### ✅ Fonctionnalités supplémentaires (1 pt)
- **Recherche dynamique** : résultats filtrés à chaque frappe
- **Tri** : par ID, Nom, Email, Rôle, Statut, Date
- **Filtrage** par rôle (ComboBox)
- **Statistiques** : compteurs en temps réel (total, étudiants, enseignants, bloqués...)
- **Export PDF** : liste exportable avec mise en forme colorée (iText)
- **Bloquer/Débloquer** en un clic (toggle)
- **Approuver** les comptes en attente
- **Redirection selon le rôle** après connexion

---

## 🏗️ Architecture — Tableau de correspondance Symfony ↔ Java

| Couche | Symfony | Java (ce projet) |
|---|---|---|
| **Entité** | `src/Entity/User.php` avec annotations ORM | `entity/User.java` POJO (pas d'ORM) |
| **Base de données** | Doctrine (ORM automatique) | JDBC + `PreparedStatement` manuel |
| **Dépôt** | `UserRepository extends ServiceEntityRepository` | `UserDAO` avec méthodes JDBC |
| **Service** | `Services/` + autowiring | `UserService` instancié manuellement |
| **Controller** | `AdminController extends AbstractController` | `AdminDashboardController implements Initializable` |
| **Vue** | Templates Twig `.html.twig` | Fichiers FXML `.fxml` |
| **CSS** | Fichiers `.css` dans `public/assets/` | Fichiers `.css` dans `resources/css/` |
| **Session** | Session PHP Symfony | `SessionManager` Singleton |
| **Sécurité** | `UserChecker`, `AppAuthenticator` | `UserService.authentifier()` |
| **Formulaire** | `FormType` + `handleRequest()` | Champs `@FXML` + listener `setOnAction()` |
| **Validation** | Annotations `#[Assert\...]` | `userService.validerCreation()` |
| **Flash messages** | `$this->addFlash('success', '...')` | `Alert` JavaFX |
| **Redirection** | `$this->redirectToRoute('admin_dashboard')` | `stage.setScene(newScene)` |
| **Export PDF** | `PdfService` avec Dompdf | `PdfExportService` avec iText |
| **Pagination** | Service `Paginator` avec `LIMIT/OFFSET` | `LIMIT ? OFFSET ?` en SQL |

---

## 🔐 Sécurité implémentée

| Mesure | Implémentation Java |
|---|---|
| Injection SQL | `PreparedStatement` avec `?` (jamais de concaténation) |
| Whitelist colonnes tri | `Map<String,String>` de colonnes autorisées dans `UserDAO` |
| Hash mot de passe | SHA-256 + sel aléatoire (ou jBCrypt en production) |
| Contrôle d'accès | `SessionManager.estAdmin()` avant chaque action admin |
| Unicité email | Vérification BDD avant insertion/modification |
| Auto-suppression | Un admin ne peut pas supprimer son propre compte |

---

## 🧪 Lancer les tests

```bash
mvn test
```

Les tests unitaires (`UserServiceTest`) vérifient :
- Validation nom (vide, trop court)
- Validation email (format, champ vide)
- Validation mot de passe (faible, fort)
- Validation rôle
- Hashage et vérification de mot de passe
- Validation téléphone tunisien

---

## 💡 Comment intégrer l'Export PDF dans l'interface

Dans `AdminDashboardController.java`, ajoutez ce bouton (déjà dans le FXML) :

```java
@FXML
private void handleExportPdf(ActionEvent event) {
    // Ouvrir une boîte de dialogue pour choisir où sauvegarder
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Exporter la liste en PDF");
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
    chooser.setInitialFileName("utilisateurs_" +
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");

    File fichier = chooser.showSaveDialog(
        ((Node) event.getSource()).getScene().getWindow());

    if (fichier != null) {
        try {
            List<User> users = userService.rechercherUsers("", "", "id", "ASC");
            new PdfExportService().exporterListeUtilisateurs(users, fichier.getAbsolutePath());
            afficherSucces("PDF exporté : " + fichier.getName());
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'export : " + e.getMessage());
        }
    }
}
```

---

## 🎯 Points de notation — Checklist

| Critère | Pts max | ✅ Implémenté |
|---|---|---|
| CRUD complet + scénario | 4 | ✅ CREATE/READ/UPDATE/DELETE fonctionnels avec navigation logique |
| Contrôle de saisie | 2 | ✅ Tous les contrôles + unicité email + messages d'erreur |
| Fonctionnalités supplémentaires | 1 | ✅ Recherche, tri, stats, export PDF, bloquer/approuver |
| Interface graphique | 4 | ✅ Toutes les vues réalisées, navigation fonctionnelle, CSS soigné |
| Compréhension du code | 7 | ✅ Commentaires détaillés, liens Symfony dans chaque classe |
| Git collaboratif | 2 | → Committer régulièrement sur votre branche ! |
| **TOTAL** | **20** | |

---

## 📝 Initialiser Git pour le projet collaboratif

```bash
# Depuis le dossier du projet
git init
git remote add origin https://github.com/votre-equipe/elearning-java.git

# Créer votre branche personnelle (OBLIGATOIRE selon la grille)
git checkout -b feature/gestion-utilisateurs-VotrePrenom

# Premier commit
git add .
git commit -m "feat: module gestion utilisateurs - CRUD + JavaFX + JDBC"
git push -u origin feature/gestion-utilisateurs-VotrePrenom

# Commits réguliers (au moins tous les 3 jours selon la grille)
git add .
git commit -m "fix: validation email en temps réel"
git push
```

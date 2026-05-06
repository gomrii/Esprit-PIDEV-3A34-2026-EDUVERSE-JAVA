# 🔗 Intégration API - Stripe & Groq AI

## Vue d'ensemble

Ce projet intègre deux APIs externes majeures :

1. **Stripe Payment Gateway** - Traitement des paiements pour l'achat de crédits
2. **Groq LLaMA AI** - Génération automatique de descriptions professionnelles en français

---

## 📋 Table des matières

1. [Configuration des API](#configuration-des-api)
2. [Structure de la base de données](#structure-de-la-base-de-données)
3. [Utilisation de Stripe](#utilisation-de-stripe)
4. [Utilisation de Groq AI](#utilisation-de-groq-ai)
5. [Exemples d'intégration dans les contrôleurs](#exemples-dintégration)
6. [Gestion des erreurs](#gestion-des-erreurs)
7. [Security & Bonnes pratiques](#security--bonnes-pratiques)

---

## 🔑 Configuration des API

### Méthode 1 : Variables d'environnement (Recommandé)

Définissez les variables d'environnement système :

```bash
# Stripe
export STRIPE_SECRET_KEY=sk_test_51T4vaiIcMGpBkhgm...
export STRIPE_PUBLIC_KEY=pk_test_51T4vaiIcMGpBkhgm...
export STRIPE_WEBHOOK_SECRET=whsec_d9bb5267a3d9f4...
export STRIPE_WEBHOOK_PORT=8080
export STRIPE_WEBHOOK_PATH=/webhooks/stripe

# Groq AI
export GROQ_API_KEY=gsk_4JgFfLVsscUP9aZpifOO...
export GROQ_MODEL=llama-3.3-70b-versatile
```

### Méthode 2 : Fichier de configuration

Créer un fichier `config/api.properties` :

```properties
# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_51T4vaiIcMGpBkhgm...
STRIPE_PUBLIC_KEY=pk_test_51T4vaiIcMGpBkhgm...
STRIPE_WEBHOOK_SECRET=whsec_d9bb5267a3d9f4...
STRIPE_WEBHOOK_PORT=8080
STRIPE_WEBHOOK_PATH=/webhooks/stripe

# Groq Configuration
GROQ_API_KEY=gsk_4JgFfLVsscUP9aZpifOO...
GROQ_MODEL=llama-3.3-70b-versatile
```

**IMPORTANT** : Ne JAMAIS commiter ce fichier dans Git !
Ajouter à `.gitignore` :
```
config/api.properties
config/.env
```

### Validation de la configuration

```java
ApiConfigUtil configUtil = ApiConfigUtil.getInstance();
configUtil.logConfigurationStatus();

if (configUtil.validateConfiguration()) {
    System.out.println("✓ Toutes les clés API sont configurées");
} else {
    System.out.println("✗ Erreur : clés API manquantes");
}
```

---

## 📊 Structure de la base de données

### Tables créées automatiquement

#### `stripe_transaction`
Stocke tous les paiements Stripe

```sql
SELECT * FROM stripe_transaction;
-- Colonnes : id, user_id, stripe_session_id, stripe_payment_intent_id, 
--           amount_cents, credits, status, payment_method, created_at, completed_at, metadata
```

#### `stripe_webhook_event`
Audit trail des webhooks reçus

```sql
SELECT * FROM stripe_webhook_event;
```

#### `groq_generated_content`
Stocke les descriptions générées par IA

```sql
SELECT * FROM groq_generated_content;
```

#### Modifications à la table `user`
- `wallet_balance` : Solde des crédits de l'utilisateur
- `wallet_updated_at` : Date de dernière mise à jour

#### Modifications à la table `formation`
- `ai_generated_description` : Booléen indiquant si description générée par IA
- `groq_generation_status` : PENDING, COMPLETED, FAILED

---

## 💳 Utilisation de Stripe

### 1. Initialiser le service

```java
StripePaymentService stripeService = new StripePaymentService();

// Vérifier les clés disponibles
stripeService.getPublicStripeKey();  // Pour le front-end
```

### 2. Créer une session de checkout

```java
try {
    int userId = 5;
    int credits = 25;  // 25 crédits = $24.99
    
    StripePaymentService.StripeCheckoutResponse response = 
        stripeService.createCheckoutSession(
            userId,
            credits,
            "https://votre-app.com/payment/success",  // successUrl
            "https://votre-app.com/payment/cancel"    // cancelUrl
        );
    
    System.out.println("URL de paiement : " + response.checkoutUrl);
    System.out.println("Session ID : " + response.sessionId);
    System.out.println("Montant : $" + response.amountUSD);
    
    // Rediriger l'utilisateur vers response.checkoutUrl
    
} catch (Exception e) {
    e.printStackTrace();
    // Afficher erreur à l'utilisateur
}
```

### 3. Vérifier le statut d'une transaction

```java
String sessionId = "cs_test_...";
StripeTransaction transaction = stripeService.getTransactionBySessionId(sessionId);

if (transaction != null) {
    System.out.println("Status: " + transaction.getStatus());
    System.out.println("Crédits: " + transaction.getCredits());
    System.out.println("Montant: $" + transaction.getAmountDollars());
}
```

### 4. Récupérer l'historique des achats

```java
List<StripeTransaction> history = stripeService.getTransactionHistory(userId);

for (StripeTransaction tx : history) {
    System.out.println(tx.getCreatedAt() + " - $" + tx.getAmountDollars() + 
                      " (" + tx.getStatus() + ")");
}
```

### 5. Obtenir les packages de crédits disponibles

```java
Map<Integer, Integer> packages = stripeService.getAvailableCreditPackages();

// Format: crédits -> centimes
for (Integer credits : packages.keySet()) {
    int cents = packages.get(credits);
    System.out.println(credits + " crédits = $" + (cents / 100.0));
}
```

---

## 🤖 Utilisation de Groq AI

### 1. Générer une description pour une formation

```java
GroqAIService groqService = new GroqAIService();

String formationTitle = "Programmation en Java pour débutants";
String courseLevel = "Débutant";

String description = groqService.generateFormationDescription(formationTitle, courseLevel);
System.out.println(description);
// Résultat : "Maîtrisez les fondamentaux de Java avec cette introduction complète..."
```

### 2. Générer une biographie d'instructeur

```java
String userName = "Dr. Ahmed Larbi";
String expertise = "Intelligence Artificielle et Machine Learning";

String bio = groqService.generateUserBiography(userName, expertise);
System.out.println(bio);
```

### 3. Générer un résumé de ressource

```java
String resourceTitle = "Tutoriel Python Avancé";
String resourceType = "Vidéo YouTube";
String context = "Programmation asynchrone en Python";

String summary = groqService.generateResourceSummary(resourceTitle, resourceType, context);
System.out.println(summary);
```

### 4. Générer un titre accrocheur

```java
String description = "Apprenez à créer des applications web modernes";
String targetAudience = "Développeurs débutants";

String title = groqService.generateFormationTitle(description, targetAudience);
System.out.println(title);  // Ex: "Web Dev Express - Du Zéro au Héros"
```

### 5. Génération avec fallback automatique

```java
// Retourne la description générée par IA, ou une description par défaut si erreur
String description = groqService.generateDescriptionWithFallback(
    "Développement Web avec React",
    "FORMATION"
);
```

### 6. Tester la connexion

```java
if (groqService.testConnection()) {
    System.out.println("✓ Connexion à Groq API OK");
} else {
    System.out.println("✗ Impossible de se connecter à Groq API");
}
```

---

## 🔌 Exemples d'intégration

### Exemple 1 : Contrôleur pour l'achat de crédits

```java
package com.elearning.controller;

import com.elearning.service.StripePaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CreditPurchaseController {
    
    @FXML private ComboBox<Integer> creditPackageCombo;
    @FXML private Label priceLabel;
    @FXML private Button purchaseButton;
    
    private StripePaymentService stripeService;
    private int currentUserId = 1;  // À récupérer de la session
    
    @FXML
    public void initialize() {
        stripeService = new StripePaymentService();
        
        // Remplir les options de crédits
        for (Integer credits : stripeService.getAvailableCreditPackages().keySet()) {
            creditPackageCombo.getItems().add(credits);
        }
        
        creditPackageCombo.setOnAction(e -> updatePrice());
    }
    
    private void updatePrice() {
        Integer selectedCredits = creditPackageCombo.getValue();
        if (selectedCredits != null) {
            int cents = stripeService.getAvailableCreditPackages().get(selectedCredits);
            double price = cents / 100.0;
            priceLabel.setText("$" + String.format("%.2f", price));
        }
    }
    
    @FXML
    public void handlePurchaseClick() {
        Integer selectedCredits = creditPackageCombo.getValue();
        
        if (selectedCredits == null) {
            showError("Veuillez sélectionner un package");
            return;
        }
        
        try {
            StripePaymentService.StripeCheckoutResponse response = 
                stripeService.createCheckoutSession(
                    currentUserId,
                    selectedCredits,
                    "https://localhost:8443/success",
                    "https://localhost:8443/cancel"
                );
            
            // Ouvrir l'URL dans le navigateur
            openBrowser(response.checkoutUrl);
            
            showSuccess("Redirection vers Stripe...");
            
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }
    
    private void openBrowser(String url) {
        java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
```

### Exemple 2 : Générer une description lors de la création de formation

```java
public class FormationFormController {
    
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> levelCombo;
    @FXML private Button generateAIButton;
    
    private GroqAIService groqService;
    
    @FXML
    public void initialize() {
        groqService = new GroqAIService();
    }
    
    @FXML
    public void handleGenerateAIDescription() {
        String title = titleField.getText();
        String level = levelCombo.getValue();
        
        if (title.isEmpty()) {
            showWarning("Veuillez entrer un titre");
            return;
        }
        
        // Afficher un indicateur de chargement
        generateAIButton.setDisable(true);
        generateAIButton.setText("Génération en cours...");
        
        // Exécuter en thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                String description = groqService.generateFormationDescription(title, level != null ? level : "Général");
                
                javafx.application.Platform.runLater(() -> {
                    if (description != null) {
                        descriptionArea.setText(description);
                        showSuccess("Description générée avec succès !");
                    } else {
                        showError("Impossible de générer la description");
                    }
                    
                    generateAIButton.setDisable(false);
                    generateAIButton.setText("Générer avec IA");
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur : " + e.getMessage());
                    generateAIButton.setDisable(false);
                    generateAIButton.setText("Générer avec IA");
                });
            }
        }).start();
    }
    
    @FXML
    public void handleSaveFormation() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        
        if (title.isEmpty() || description.isEmpty()) {
            showWarning("Tous les champs sont obligatoires");
            return;
        }
        
        // Sauvegarder en base de données
        Formation formation = new Formation();
        formation.setTitle(title);
        formation.setDescription(description);
        
        // ... reste du code de sauvegarde
    }
}
```

### Exemple 3 : Serveur Webhook pour Stripe (optionnel)

```java
package com.elearning.util;

import com.elearning.service.StripeWebhookHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;

/**
 * Serveur HTTP simple pour recevoir les webhooks Stripe.
 * 
 * Note : Pour la production, utilisez Spring Boot ou un serveur dédié.
 */
public class StripeWebhookServer {
    
    private static final int PORT = 8080;
    private static final String PATH = "/webhooks/stripe";
    
    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        StripeWebhookHandler webhookHandler = new StripeWebhookHandler();
        
        server.createContext(PATH, exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Lire le payload
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody())
                );
                StringBuilder payload = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    payload.append(line);
                }
                
                // Récupérer la signature
                String signature = exchange.getHeaders().getFirst("Stripe-Signature");
                
                // Traiter l'événement
                boolean success = webhookHandler.handleWebhookEvent(
                    payload.toString(),
                    signature
                );
                
                // Retourner 200 OK
                exchange.sendResponseHeaders(success ? 200 : 400, 0);
                exchange.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        });
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("✓ Webhook server démarré sur http://localhost:" + PORT + PATH);
    }
    
    public static void main(String[] args) throws IOException {
        start();
        System.out.println("Appuyez sur Ctrl+C pour arrêter");
    }
}
```

---

## ⚠️ Gestion des erreurs

### Erreurs courantes et solutions

#### 1. Clé API non trouvée
```java
try {
    stripeService.createCheckoutSession(...);
} catch (IllegalArgumentException e) {
    // Vérifier les variables d'environnement ou le fichier de config
    logger.severe("Clé API non configurée: " + e.getMessage());
}
```

#### 2. Erreur Stripe (réseau ou API)
```java
try {
    stripeService.createCheckoutSession(...);
} catch (Exception e) {
    // Vérifier la clé, la connexion Internet, ou le compte Stripe
    logger.severe("Erreur Stripe: " + e.getMessage());
    showUserFriendlyError("Impossible de traiter le paiement. Réessayez plus tard.");
}
```

#### 3. Erreur Groq AI (quota dépassé ou erreur réseau)
```java
String description = groqService.generateFormationDescription(title, level);

if (description == null) {
    // Utiliser une description par défaut
    description = groqService.generateDescriptionWithFallback(title, "FORMATION");
}
```

---

## 🔒 Security & Bonnes pratiques

### 1. **Ne JAMAIS hardcoder les clés API**
```java
// ❌ MAUVAIS
String apiKey = "sk_test_abc123...";

// ✅ BON
String apiKey = ApiConfigUtil.getInstance().getStripeSecretKey();
```

### 2. **Vérifier toujours les signatures Stripe**
```java
// Les webhooks doivent être vérifiés avec le webhook secret
Event event = Webhook.constructEvent(payload, signature, secret);
```

### 3. **Utiliser HTTPS pour les webhooks**
```
En production, configurez :
- certificat SSL valide
- port 443 (HTTPS)
```

### 4. **Valider les montants côté serveur**
```java
// Ne pas faire confiance au montant du client
Integer amountCents = CREDIT_PRICING.get(credits);  // Valider depuis le serveur
```

### 5. **Gérer les transactions atomiquement**
```java
// Mettre à jour la transaction ET le portefeuille de manière atomique
if (transactionDAO.updateStatus(...) && userDAO.updateUser(...)) {
    // Transaction réussie
} else {
    // Erreur critique - audit et notification
}
```

### 6. **Logger les événements importants**
```java
logger.info("Paiement complété: userId=" + userId + ", credits=" + credits);
logger.warning("Paiement échoué: " + reason);
logger.severe("Erreur critique: " + error);
```

---

## 📝 Notes importantes

### Stripe
- **Mode Test** : Utilisez `sk_test_` et `pk_test_`
- **Mode Production** : Utilisez `sk_live_` et `pk_live_`
- **Webhooks** : Configurez dans le dashboard Stripe
- **Documentaton** : https://stripe.com/docs

### Groq AI
- **Modèle** : llama-3.3-70b-versatile (gratuit pour les tests)
- **Limite de tokens** : 300 tokens par défaut
- **Température** : 0.7 (recommandé pour générer du contenu créatif)
- **Documentation** : https://console.groq.com/docs

---

## 🧪 Tester les intégrations

```bash
# Compiler et construire
mvn clean install

# Lancer l'application
mvn javafx:run

# Créer un JAR exécutable
mvn package
```

---

## 📞 Support et Troubleshooting

En cas de problème :

1. Vérifiez les logs : `logger.info(...)` et `e.printStackTrace()`
2. Testez les clés API manuellement
3. Vérifiez la connexion Internet
4. Consultez la documentation officielle
5. Créez un issue sur le repo du projet

---

**Dernière mise à jour** : 30 Avril 2026

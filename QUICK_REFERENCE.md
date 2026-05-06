# 🔗 Quick Reference - API Integration

## 📌 Stripe Payment Quick Start

### Create a Payment Session
```java
StripePaymentService stripe = new StripePaymentService();

try {
    StripePaymentService.StripeCheckoutResponse response = 
        stripe.createCheckoutSession(
            userId,                              // User ID
            25,                                 // Credits (10, 25, 50, or 100)
            "https://your-app.com/payment/success",
            "https://your-app.com/payment/cancel"
        );
    
    // Redirect user to response.checkoutUrl
    Desktop.getDesktop().browse(URI.create(response.checkoutUrl));
    
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
}
```

### Available Credit Packages
```java
Map<Integer, Integer> pricing = stripe.getAvailableCreditPackages();
// Output: {10=999, 25=2499, 50=4899, 100=8999}
// Key = credits, Value = cents
```

### Check Transaction Status
```java
StripeTransaction tx = stripe.getTransactionBySessionId(sessionId);
if (tx != null) {
    System.out.println("Status: " + tx.getStatus());  // PENDING, COMPLETED, FAILED, CANCELED
    System.out.println("Amount: $" + tx.getAmountDollars());
    System.out.println("Credits: " + tx.getCredits());
}
```

### Get User Transaction History
```java
List<StripeTransaction> history = stripe.getTransactionHistory(userId);
for (StripeTransaction tx : history) {
    System.out.println(tx.getCreatedAt() + " - $" + tx.getAmountDollars());
}
```

---

## 🤖 Groq AI Quick Start

### Generate Formation Description
```java
GroqAIService groq = new GroqAIService();

String description = groq.generateFormationDescription(
    "Advanced Java Programming",
    "Intermédiaire"
);

System.out.println(description);
// Maîtrisez les concepts avancés de Java avec cette formation approfondie...
```

### Generate User Biography
```java
String bio = groq.generateUserBiography(
    "Dr. Ahmed Larbi",
    "Machine Learning et IA"
);
```

### Generate Resource Summary
```java
String summary = groq.generateResourceSummary(
    "Python Async Programming",
    "Vidéo YouTube",
    "Programmation asynchrone en Python"
);
```

### Generate Catchy Title
```java
String title = groq.generateFormationTitle(
    "Learn web development from scratch",
    "Débutants programmeurs"
);
// "Web Dev Express - Du Zéro au Héros!"
```

### Safe Generation with Fallback
```java
String description = groq.generateDescriptionWithFallback(
    "Mon Titre",
    "FORMATION"
);
// Returns AI-generated OR safe default if API fails
```

### Test Connection
```java
if (groq.testConnection()) {
    System.out.println("✓ Groq API is accessible");
}
```

---

## 🔑 Configuration Quick Start

### Method 1: Environment Variables (Recommended)
```bash
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_PUBLIC_KEY=pk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...
export GROQ_API_KEY=gsk_...
```

### Method 2: Configuration File
```bash
# Create config/api.properties
mkdir config
cp config/api.properties.example config/api.properties
# Edit with your keys
nano config/api.properties
```

### Access Configuration
```java
ApiConfigUtil config = ApiConfigUtil.getInstance();

String stripeSecret = config.getStripeSecretKey();
String groqKey = config.getGroqApiKey();

// Validate
if (config.validateConfiguration()) {
    System.out.println("✓ All keys configured");
}

// Log status
config.logConfigurationStatus();
```

---

## 💾 Database Quick Reference

### Add Credits to User
```java
User user = userDAO.getUserById(userId);
user.addCredits(25);  // Add 25 credits
userDAO.updateUser(user);
```

### Get User Credits
```java
User user = userDAO.getUserById(userId);
double credits = user.getWalletBalance();
System.out.println("User has: " + credits + " credits");
```

### Get Transaction by Session ID
```java
StripeTransactionDAO dao = new StripeTransactionDAO();
StripeTransaction tx = dao.getTransactionByStripeSessionId(sessionId);
```

### Get Completed Transactions Between Dates
```java
List<StripeTransaction> txs = dao.getCompletedTransactionsBetweenDates(
    LocalDateTime.of(2025, 1, 1, 0, 0),
    LocalDateTime.of(2025, 12, 31, 23, 59)
);
```

### Get Groq AI Generation Stats
```java
GroqGeneratedContentDAO dao = new GroqGeneratedContentDAO();
GroqGeneratedContentDAO.GroqUsageStats stats = dao.getUsageStats();

System.out.println("Total: " + stats.totalRequests);
System.out.println("Success Rate: " + stats.getSuccessRate() + "%");
```

---

## ⚠️ Error Handling

### Stripe Error Handling
```java
try {
    stripe.createCheckoutSession(...);
} catch (IllegalArgumentException e) {
    // API key not configured
    System.err.println("Config error: " + e.getMessage());
} catch (Exception e) {
    // Stripe API error (network, etc)
    System.err.println("Stripe error: " + e.getMessage());
}
```

### Groq Error Handling
```java
String description = groq.generateFormationDescription(title, level);

if (description == null) {
    // API failed, use fallback
    description = groq.generateDescriptionWithFallback(title, "FORMATION");
}
```

---

## 🧪 Testing

### Run All Integration Tests
```java
IntegrationTestUtil.runAllTests();
```

### Run Specific Tests
```java
IntegrationTestUtil.testStripeConfiguration();
IntegrationTestUtil.testGroqConfiguration();
IntegrationTestUtil.testCreditPricing();
```

### Print Quick Start Guide
```java
IntegrationTestUtil.printQuickStart();
```

---

## 🔐 Security Checklist

- [ ] API keys NOT in source code
- [ ] config/api.properties in .gitignore
- [ ] Using environment variables or secure vault
- [ ] Testing with Stripe test keys (sk_test_, pk_test_)
- [ ] Webhook signature verified
- [ ] Webhook endpoint is HTTPS (production)
- [ ] Amount validation on server-side
- [ ] Logging configured (no key exposure)
- [ ] Rate limiting monitored
- [ ] Error messages user-friendly (no technical details)

---

## 📱 JavaFX Controller Integration Template

```java
public class PaymentController {
    
    private StripePaymentService stripeService;
    private GroqAIService groqService;
    
    @FXML
    public void initialize() {
        stripeService = new StripePaymentService();
        groqService = new GroqAIService();
    }
    
    @FXML
    private void handleBuyCreditClick() {
        try {
            var response = stripeService.createCheckoutSession(
                userId, 25, successUrl, cancelUrl
            );
            openBrowser(response.checkoutUrl);
        } catch (Exception e) {
            showError("Payment error: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGenerateDescriptionClick() {
        new Thread(() -> {
            String desc = groqService.generateFormationDescription(
                titleField.getText(), 
                "Débutant"
            );
            Platform.runLater(() -> descriptionArea.setText(desc));
        }).start();
    }
}
```

---

## 📊 Common Queries

### Get All Pending Transactions
```java
List<StripeTransaction> pending = dao.getTransactionsByStatus("PENDING");
```

### Get Total Revenue
```java
double total = dao.getTotalCompletedAmountForUser(userId);
```

### Get Failed Generations
```java
List<GroqGeneratedContent> failed = dao.getContentsByStatus("FAILED");
```

### Get Latest AI Description for Formation
```java
GroqGeneratedContent content = dao.getLatestContentForEntity("FORMATION", formationId);
```

---

## 🚀 Production Deployment

### Before Going Live

1. **Switch to Live Keys**
   ```
   sk_live_... (not sk_test_)
   pk_live_... (not pk_test_)
   ```

2. **Configure Webhooks**
   - Dashboard: https://dashboard.stripe.com/webhooks
   - URL: https://your-domain.com/webhooks/stripe
   - Events: checkout.session.completed, session.expired

3. **Set HTTPS**
   - SSL certificate for webhook endpoint
   - Firewall rules for port 443

4. **Monitor**
   - Check Stripe Dashboard
   - Monitor Groq API usage
   - Set up alerts for failed transactions

5. **Backup & Recovery**
   - Daily database backups
   - Transaction audit logs
   - Fallback API keys

---

## 📞 Support Links

- **Stripe API Docs**: https://stripe.com/docs
- **Stripe Testing**: https://stripe.com/docs/testing
- **Groq Console**: https://console.groq.com
- **Groq API Docs**: https://console.groq.com/docs

---

**Last Updated**: 30 April 2026  
**Status**: ✅ Ready for Production

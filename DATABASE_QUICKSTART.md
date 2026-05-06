# 🚀 Quick Start - New Database Setup

## What's New?

Your project has been integrated with **eduverse-2** database that includes:
- ✅ User authentication & management
- ✅ Courses, formations & chapters
- ✅ Quiz system (questions, answers, results)
- ✅ Club management & events
- ✅ AI recommendations
- ✅ Payment transactions (Stripe)
- ✅ Course viewing history
- ✅ Login analytics

---

## 🎯 First Steps (Do This Now!)

### 1. Import the Database (2 minutes)

**Option A: Using MySQL Command Line**
```bash
mysql -u root -p
CREATE DATABASE IF NOT EXISTS eduverse CHARACTER SET utf8mb4;
USE eduverse;
SOURCE database/eduverse-2.sql;
SHOW TABLES;  # Verify ~20+ tables created
```

**Option B: Using phpMyAdmin**
1. Open http://localhost/phpmyadmin
2. Create database: `eduverse`
3. Go to Import tab
4. Select: `database/eduverse-2.sql`
5. Click Import

**Option C: One-liner**
```bash
mysql -u root eduverse < database/eduverse-2.sql
```

### 2. Test Database Connection

Run this Java test:
```bash
cd c:\Users\slima\Downloads\Esprit-PIDEV-3A34-2026-EDUVERSE-JAVA-user+formation
mvn clean compile
```

If compilation succeeds, database entities are recognized! ✓

### 3. Build the Project

```bash
mvn clean install
```

If all tests pass, you're ready to go! 🎉

---

## 📊 Database Tables Overview

### Core Tables
```
Users & Auth:      user, login_attempt
Content:           formation, cours, cour, chapitre, ressource, certificate
Assessment:        quiz, question, reponse, quiz_result
Community:         club, event, club_members, club_membership, join_request
Payments:          stripe_transaction
Analytics:         ai_recommendation, historique
```

### Test Some Data
```sql
-- Check users
SELECT * FROM user LIMIT 5;

-- Check courses
SELECT title, status FROM cours LIMIT 5;

-- Check quiz system
SELECT COUNT(*) as quiz_count FROM quiz;
SELECT COUNT(*) as questions_count FROM question;

-- Check clubs
SELECT * FROM club LIMIT 5;
```

---

## 📁 Project Structure

```
src/main/java/
├── com/elearning/
│   ├── controller/       ← Request handlers
│   ├── entity/           ← Database models (User, Formation, etc.)
│   ├── dao/              ← Data access (queries)
│   ├── service/          ← Business logic
│   └── util/             ← Utilities (DatabaseConnection, etc.)
├── Controllers/          ← Additional controllers
├── Entities/             ← Additional entities (Quiz, Club, etc.)
├── Services/             ← Additional services
└── Test/                 ← Test classes

database/
├── schema.sql            ← Original schema
├── eduverse-2.sql        ← New schema (USE THIS ONE)
└── quiz_*.sql            ← Migration scripts
```

---

## 🔧 Configuration Files

**Location**: `config/api.properties` (Create from `api.properties.example`)

```properties
# Database (configured automatically)
# USER: root
# PASSWORD: (empty)
# URL: jdbc:mysql://localhost:3306/eduverse

# Optional: Stripe integration
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLIC_KEY=pk_test_...

# Optional: AI features (Groq)
GROQ_API_KEY=gsk_...
GROQ_MODEL=llama-3.3-70b-versatile
```

---

## ⚠️ Important Notes

1. **Database Connection**
   - File: `src/main/java/com/elearning/util/DatabaseConnection.java`
   - Default: `root` user, empty password
   - Change if your MySQL is configured differently

2. **Not Committing Credentials**
   - Never commit `config/api.properties` with real keys
   - It's in `.gitignore` - keep it that way

3. **Multiple Course Tables**
   - Both `cours` and `cour` tables exist
   - Consider standardizing in future

---

## 📚 Documentation Files

- **[DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)** - Full table reference
- **[DATABASE_SETUP.md](DATABASE_SETUP.md)** - Detailed setup & troubleshooting
- **[INTEGRATION_CHECKLIST.md](INTEGRATION_CHECKLIST.md)** - What's done & what's next

---

## 🐛 Troubleshooting

### Problem: "Connection refused"
```bash
# Make sure MySQL is running
# Windows: net start MySQL80
# macOS: brew services start mysql@8.0
```

### Problem: "Access denied for user 'root'"
```bash
# Update password in DatabaseConnection.java if needed
# Or reset MySQL root password
```

### Problem: "No tables found"
```bash
# Import wasn't successful, try:
mysql -u root eduverse < database/eduverse-2.sql
mysql -u root eduverse -e "SHOW TABLES;"
```

### Problem: "Entity class not found"
```bash
# Rebuild project:
mvn clean compile
```

---

## ✨ What's Next?

1. **Organize Entities** - Move scattered entity classes to `com.elearning.entity`
2. **Create Missing DAOs** - QuizDAO, ClubDAO, CertificateDAO, etc.
3. **Update Services** - Implement business logic for new entities
4. **Test CRUD** - Test Create, Read, Update, Delete for all entities
5. **Run Application** - Start the JavaFX UI

---

## 🎓 Entity Classes Status

| Entity | Location | Status |
|--------|----------|--------|
| User | `com.elearning.entity` | ✅ Ready |
| Formation | `com.elearning.entity` | ✅ Ready |
| Quiz | `Entities` | ⚠️ Needs move |
| Club | `Entities` | ⚠️ Needs move |
| Event | `Entities` | ⚠️ Needs move |
| Certificate | `com.elearning.entity` | ⚠️ Needs creation |
| QuizResult | - | ❌ Missing |
| ClubMembership | - | ❌ Missing |

---

## 💡 Tips

- Most database connections use the Singleton pattern (DatabaseConnection)
- All timestamps are in UTC
- All string fields use UTF-8 encoding
- Use parameterized queries to prevent SQL injection

---

**Ready to go?** Start with: `mysql -u root eduverse < database/eduverse-2.sql` ✨

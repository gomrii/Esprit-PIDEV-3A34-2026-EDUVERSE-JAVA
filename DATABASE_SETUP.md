# Database Setup & Migration Guide

## Current Database Configuration

**Database Name**: `eduverse`  
**Host**: `localhost:3306`  
**Driver**: MySQL Connector/J (com.mysql.cj.jdbc.Driver)  
**User**: `root`  
**Password**: (empty by default)  
**Charset**: utf8mb4  

Location in code: [DatabaseConnection.java](src/main/java/com/elearning/util/DatabaseConnection.java)

## Setup Instructions

### Step 1: Create the Database
```sql
CREATE DATABASE IF NOT EXISTS eduverse 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;
```

### Step 2: Import the Latest Schema
```bash
# Using MySQL CLI
mysql -u root -p eduverse < database/eduverse-2.sql

# Or using a MySQL GUI like phpMyAdmin:
# 1. Create database "eduverse"
# 2. Go to Import tab
# 3. Select file: database/eduverse-2.sql
# 4. Click Import
```

### Step 3: Verify Installation
```sql
mysql> use eduverse;
mysql> SHOW TABLES;
-- Should show 20+ tables including: user, formation, cours, quiz, question, etc.
```

## Database Schema v2 Features

### New/Updated Tables
- **ai_recommendation** - AI-powered admin alerts and recommendations
- **quiz** - Quiz management system
- **question** - Quiz questions
- **reponse** - Answer options/responses
- **quiz_result** - Student quiz attempt records
- **club** - Student clubs
- **event** - Club events and activities
- **club_membership** - Membership tracking with approval workflow
- **chapitre** - Course chapters/sections
- **certificate** - Course completion certificates

### Enhanced Existing Tables
- **user** - Core user accounts (ADMIN, ENSEIGNANT, ETUDIANT, FORMATEUR)
- **formation** - Training courses with approval workflow
- **cours/cour** - Course content (note: both tables exist for compatibility)
- **stripe_transaction** - Payment processing records
- **login_attempt** - Security audit logging

## Java Entity Updates Required

The following entities need to be created/verified:

### Required Entities
- [ ] `User.java` - Mapped to `user` table
- [ ] `Formation.java` - Mapped to `formation` table
- [ ] `Quiz.java` - Mapped to `quiz` table
- [ ] `Question.java` - Mapped to `question` table
- [ ] `Reponse.java` - Mapped to `reponse` table
- [ ] `QuizResult.java` - Mapped to `quiz_result` table
- [ ] `Club.java` - Mapped to `club` table
- [ ] `Event.java` - Mapped to `event` table
- [ ] `ClubMembership.java` - Mapped to `club_membership` table
- [ ] `Certificate.java` - Mapped to `certificate` table
- [ ] `Chapitre.java` - Mapped to `chapitre` table
- [ ] `AIRecommendation.java` - Mapped to `ai_recommendation` table

## Configuration Files

### 1. Database Connection
File: `src/main/java/com/elearning/util/DatabaseConnection.java`
```java
private static final String URL = "jdbc:mysql://localhost:3306/eduverse?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USER = "root";
private static final String PASSWORD = "";
```

### 2. API Configuration
File: `config/api.properties`
```properties
# Optional - Stripe integration
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLIC_KEY=pk_test_...

# Optional - Groq AI integration
GROQ_API_KEY=gsk_...
GROQ_MODEL=llama-3.3-70b-versatile
```

## Testing Database Connectivity

```bash
cd c:\Users\slima\Downloads\Esprit-PIDEV-3A34-2026-EDUVERSE-JAVA-user+formation
mvn clean compile

# Then run:
java -cp target/classes:lib/* com.elearning.MainApp
```

## Important Notes

1. **Security**: Never commit `config/api.properties` with real API keys
2. **Passwords**: Update DatabaseConnection with your MySQL password if needed
3. **Encoding**: Database uses utf8mb4 for full Unicode support
4. **Timezones**: Set to UTC for consistency
5. **SSL**: Disabled for local development (`useSSL=false`)

## Troubleshooting

### Connection Refused
```bash
# Check MySQL is running
net start MySQL80  # Windows
# or
brew services start mysql@8.0  # macOS
```

### Access Denied
```bash
# Check credentials in DatabaseConnection.java
# Default: user=root, password=(empty)
```

### Table Not Found
```bash
# Verify database was imported
mysql -u root -p eduverse -e "SHOW TABLES;"
```

### Character Set Issues
```bash
# Verify UTF-8 encoding
mysql -u root -p eduverse -e "SHOW CREATE DATABASE eduverse;"
```

## Data Migration (From Previous Version)

If migrating from an older version:

```bash
# 1. Backup old data
mysqldump -u root -p eduverse > backup-old.sql

# 2. Update schema
mysql -u root -p eduverse < database/eduverse-2.sql

# 3. Verify data integrity
mysql -u root -p eduverse -e "SELECT COUNT(*) FROM user;"
```

## Next Steps

1. ✅ Database imported and configured
2. ⏳ Verify Java entity classes match schema
3. ⏳ Update DAO/Service classes for new tables
4. ⏳ Test data CRUD operations
5. ⏳ Update unit tests with new schema

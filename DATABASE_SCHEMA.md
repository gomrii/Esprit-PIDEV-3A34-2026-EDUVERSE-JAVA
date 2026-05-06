# EDUVERSE Database Schema v2 (Updated)

## Overview
The database has been updated to support expanded features including quiz management, clubs, events, and AI recommendations.

## Core Tables

### 1. **User Management**
- `user` - User accounts and profiles
- `login_attempt` - Authentication history

### 2. **Educational Content**
- `formation` - Main training/formation courses
- `cours` - Courses (alternative structure)
- `cour` - Courses (extended structure with file support)
- `chapitre` - Chapter/Section content within courses
- `certificate` - Certificates awarded to users
- `ressource` - Learning resources

### 3. **Quiz & Assessment**
- `question` - Quiz questions
- `quiz` - Quiz definitions
- `reponse` - Answers/Responses to questions
- `quiz_result` - Student quiz completion records

### 4. **Social & Community**
- `club` - Student clubs
- `club_members` - Club membership
- `club_membership` - Club membership status tracking
- `event` - Events (associated with clubs)
- `join_request` - Club join requests

### 5. **Transactions & Payments**
- `stripe_transaction` - Payment transaction records
- `certificate` - Completion certificates with scores

### 6. **AI & Analytics**
- `ai_recommendation` - AI-generated admin recommendations
- `historique` - Course viewing history
- `login_attempt` - Login analytics

### 7. **System Tables**
- `doctrine_migration_versions` - Database migration tracking
- `messenger_messages` - Message queue

## Key Relationships

```
User (1) ──→ (many) Formation
         ──→ (many) Course/Cour
         ──→ (many) Certificate
         ──→ (many) Event
         
Club (1) ──→ (many) Event
        ──→ (many) Club_Members
        
Course (1) ──→ (many) Chapitre
         ──→ (many) Ressource
         
Quiz (1) ──→ (many) Question
Question (1) ──→ (many) Reponse
```

## Important Columns & Constraints

### Status Fields (Common)
- `status`: PENDING, APPROVED, BLOCKED, ACTIVE, etc.
- `is_approved` (tinyint): 0 or 1
- `priority` (in AI): HAUTE, MOYENNE, FAIBLE

### Timestamps
- `created_at` - Creation date
- `updated_at` - Last modification
- `date_consultation` - Access history
- `attempted_at` - For login attempts

### User Roles (from user table)
- ADMIN - Administrator
- ENSEIGNANT - Teacher/Instructor
- ETUDIANT - Student
- FORMATEUR - Trainer

## Integration Points

1. **Authentication**: Uses `user` and `login_attempt` tables
2. **Content Management**: `formation`, `cours`/`cour`, `chapitre`
3. **Assessment**: `quiz`, `question`, `reponse`, `quiz_result`
4. **Community**: `club`, `event`, `join_request`
5. **Transactions**: `stripe_transaction`, `certificate`
6. **AI**: `ai_recommendation` for admin insights

## Migration Notes

- Database supports both `cours` and `cour` tables (consider consolidating)
- Multiple course structures exist - recommend standardization
- AI recommendations table includes historical data (51+ records)
- Login attempts tracked for security analytics

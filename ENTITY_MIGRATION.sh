#!/bin/bash
# Entity Organization Migration Script
# This script helps reorganize entity classes from scattered locations

PROJECT_ROOT="c:/Users/slima/Downloads/Esprit-PIDEV-3A34-2026-EDUVERSE-JAVA-user+formation"
SRC_DIR="$PROJECT_ROOT/src/main/java"

echo "🔄 Entity Migration Script"
echo "=========================="
echo ""
echo "Current entity locations:"
echo "  1. com/elearning/entity/"
echo "  2. Entities/"
echo ""
echo "This script will help consolidate to: com/elearning/entity/"
echo ""

# Entities to move from Entities/ to com/elearning/entity/
ENTITIES_TO_MOVE=(
    "Chapitre.java"
    "Club.java"
    "Cours.java"
    "Event.java"
    "Historique.java"
    "Question.java"
    "Quiz.java"
    "Reponse.java"
)

echo "Entities to migrate:"
for entity in "${ENTITIES_TO_MOVE[@]}"; do
    echo "  → $entity"
done
echo ""

echo "⚠️  This script provides migration PLAN, not automatic execution."
echo ""
echo "Manual Steps to Follow:"
echo "======================="
echo ""
echo "1. Move Entity Files:"
for entity in "${ENTITIES_TO_MOVE[@]}"; do
    echo "   cp src/main/java/Entities/$entity src/main/java/com/elearning/entity/"
done
echo ""

echo "2. Update Package Declarations in each file:"
echo "   FROM: package Entities;"
echo "   TO:   package com.elearning.entity;"
echo ""

echo "3. Update Imports Across Codebase:"
echo "   FROM: import Entities.*;"
echo "   TO:   import com.elearning.entity.*;"
echo ""

echo "4. Verify Controllers Use Correct Imports:"
grep -r "import Entities" "$SRC_DIR/Controllers" 2>/dev/null | head -5 || echo "   No Entities imports found in Controllers"
echo ""

echo "5. Check Services for Entity Imports:"
grep -r "import Entities" "$SRC_DIR/Services" 2>/dev/null | head -5 || echo "   No Entities imports found in Services"
echo ""

echo "6. Create Missing Entity Classes:"
echo "   - Certificate.java"
echo "   - ClubMembership.java"
echo "   - JoinRequest.java"
echo "   - LoginAttempt.java"
echo "   - QuizResult.java"
echo ""

echo "7. Rebuild Project:"
echo "   mvn clean compile"
echo ""

echo "📋 Create Missing Entities Template"
echo "===================================="
echo ""
echo "File: src/main/java/com/elearning/entity/Certificate.java"
echo "---"
cat > "$PROJECT_ROOT/TEMPLATES_Certificate.java" << 'EOF'
package com.elearning.entity;

import java.time.LocalDateTime;

public class Certificate {
    private int id;
    private double score;
    private LocalDateTime awardedAt;
    private int userId;
    private int formationId;
    private Integer quizId;

    // Constructors
    public Certificate() {}

    public Certificate(double score, LocalDateTime awardedAt, int userId, int formationId) {
        this.score = score;
        this.awardedAt = awardedAt;
        this.userId = userId;
        this.formationId = formationId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public LocalDateTime getAwardedAt() { return awardedAt; }
    public void setAwardedAt(LocalDateTime awardedAt) { this.awardedAt = awardedAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public Integer getQuizId() { return quizId; }
    public void setQuizId(Integer quizId) { this.quizId = quizId; }
}
EOF
echo "Template created at TEMPLATES_Certificate.java"
echo ""

echo "File: src/main/java/com/elearning/entity/QuizResult.java"
echo "---"
cat > "$PROJECT_ROOT/TEMPLATES_QuizResult.java" << 'EOF'
package com.elearning.entity;

import java.time.LocalDateTime;

public class QuizResult {
    private int id;
    private int userId;
    private int quizId;
    private double score;
    private int correctAnswers;
    private int totalQuestions;
    private LocalDateTime completedAt;

    // Constructors
    public QuizResult() {}

    public QuizResult(int userId, int quizId, double score, int correctAnswers, int totalQuestions) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.completedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
EOF
echo "Template created at TEMPLATES_QuizResult.java"
echo ""

echo "✅ Migration plan complete!"
echo ""
echo "Next: Follow the manual steps above to complete the migration."

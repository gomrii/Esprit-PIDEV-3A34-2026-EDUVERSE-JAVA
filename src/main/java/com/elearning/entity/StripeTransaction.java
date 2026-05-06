package com.elearning.entity;

import java.time.LocalDateTime;

/**
 * StripeTransaction — Représente une transaction Stripe.
 * 
 * Utilisée pour :
 * - Tracer les achats de crédits
 * - Stocker l'état du paiement
 * - Auditer les transactions
 */
public class StripeTransaction {
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELED = "CANCELED";

    private int id;
    private int userId;
    private String stripeSessionId;
    private String stripePaymentIntentId;
    private int amountCents;           // Montant en centimes
    private int credits;               // Nombre de crédits à acheter
    private String status;             // PENDING, COMPLETED, FAILED, CANCELED
    private String paymentMethod;      // carte, apple_pay, etc.
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String metadata;           // JSON supplémentaire

    public StripeTransaction() {
        this.status = STATUS_PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public StripeTransaction(int userId, String stripeSessionId, int amountCents, int credits) {
        this.userId = userId;
        this.stripeSessionId = stripeSessionId;
        this.amountCents = amountCents;
        this.credits = credits;
        this.status = STATUS_PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(int amountCents) {
        this.amountCents = amountCents;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public double getAmountDollars() {
        return amountCents / 100.0;
    }

    @Override
    public String toString() {
        return "StripeTransaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", stripeSessionId='" + stripeSessionId + '\'' +
                ", amountDollars=" + getAmountDollars() +
                ", credits=" + credits +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

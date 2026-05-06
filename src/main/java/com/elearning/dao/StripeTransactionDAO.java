package com.elearning.dao;

import com.elearning.entity.StripeTransaction;
import com.elearning.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class StripeTransactionDAO {
    
    private static final Logger logger = Logger.getLogger(StripeTransactionDAO.class.getName());

    /**
     * Crée une nouvelle transaction Stripe en base de données.
     */
    public int createTransaction(StripeTransaction transaction) {
        String sql = "INSERT INTO stripe_transaction " +
                     "(user_id, stripe_session_id, amount_cents, credits, status, payment_method, metadata) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, transaction.getUserId());
            stmt.setString(2, transaction.getStripeSessionId());
            stmt.setInt(3, transaction.getAmountCents());
            stmt.setInt(4, transaction.getCredits());
            stmt.setString(5, transaction.getStatus());
            stmt.setString(6, transaction.getPaymentMethod());
            stmt.setString(7, transaction.getMetadata());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 1) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        transaction.setId(id);
                        logger.info("Transaction créée : ID = " + id + ", User = " + transaction.getUserId());
                        return id;
                    }
                }
            }
            
            logger.warning("Erreur lors de la création de la transaction");
            return -1;
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL lors de la création de la transaction : " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Récupère une transaction par son ID.
     */
    public StripeTransaction getTransactionById(int id) {
        String sql = "SELECT * FROM stripe_transaction WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTransaction(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Récupère une transaction par son Stripe Session ID.
     */
    public StripeTransaction getTransactionByStripeSessionId(String sessionId) {
        String sql = "SELECT * FROM stripe_transaction WHERE stripe_session_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTransaction(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Récupère toutes les transactions d'un utilisateur.
     */
    public List<StripeTransaction> getTransactionsByUserId(int userId) {
        String sql = "SELECT * FROM stripe_transaction WHERE user_id = ? ORDER BY created_at DESC";
        List<StripeTransaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }

    /**
     * Récupère les transactions par statut.
     */
    public List<StripeTransaction> getTransactionsByStatus(String status) {
        String sql = "SELECT * FROM stripe_transaction WHERE status = ? ORDER BY created_at DESC";
        List<StripeTransaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }

    /**
     * Met à jour le statut et l'ID de paiement Stripe.
     */
    public boolean updateTransactionStatus(int transactionId, String status, String paymentIntentId) {
        String sql = "UPDATE stripe_transaction SET status = ?, stripe_payment_intent_id = ?, " +
                     "completed_at = NOW() WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setString(2, paymentIntentId);
            stmt.setInt(3, transactionId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Transaction mise à jour : ID = " + transactionId + ", Status = " + status);
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Récupère toutes les transactions complétées entre deux dates.
     */
    public List<StripeTransaction> getCompletedTransactionsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM stripe_transaction WHERE status = ? AND completed_at BETWEEN ? AND ? " +
                     "ORDER BY completed_at DESC";
        List<StripeTransaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, StripeTransaction.STATUS_COMPLETED);
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }

    /**
     * Récupère le montant total des transactions complétées pour un utilisateur.
     */
    public double getTotalCompletedAmountForUser(int userId) {
        String sql = "SELECT SUM(amount_cents) as total FROM stripe_transaction " +
                     "WHERE user_id = ? AND status = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, StripeTransaction.STATUS_COMPLETED);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long totalCents = rs.getLong("total");
                    return totalCents / 100.0;
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Supprime une transaction (rare, généralement pour la maintenance).
     */
    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM stripe_transaction WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===============================================
    // Helper Method
    // ===============================================

    private StripeTransaction mapRowToTransaction(ResultSet rs) throws SQLException {
        StripeTransaction transaction = new StripeTransaction();
        transaction.setId(rs.getInt("id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setStripeSessionId(rs.getString("stripe_session_id"));
        transaction.setStripePaymentIntentId(rs.getString("stripe_payment_intent_id"));
        transaction.setAmountCents(rs.getInt("amount_cents"));
        transaction.setCredits(rs.getInt("credits"));
        transaction.setStatus(rs.getString("status"));
        transaction.setPaymentMethod(rs.getString("payment_method"));
        transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            transaction.setCompletedAt(completedAt.toLocalDateTime());
        }
        
        transaction.setMetadata(rs.getString("metadata"));
        
        return transaction;
    }
}

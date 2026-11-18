package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Data access for the bank_transaction table. */
public class TransactionDAO {

    /** Joins an existing database transaction so the balance and its history are saved together. */
    public void insert(Connection conn, String accountNumber, Transaction t) throws SQLException {
        String sql = "INSERT INTO bank_transaction (transaction_id, account_number, type, amount, "
                + "transaction_date, balance_after, reference) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTransactionId());
            ps.setString(2, accountNumber);
            ps.setString(3, t.getType().name());
            ps.setBigDecimal(4, t.getAmount());
            ps.setObject(5, t.getTransactionDate());
            ps.setBigDecimal(6, t.getBalanceAfter());
            ps.setString(7, t.getReference());
            ps.executeUpdate();
        }
    }

    /** Newest first, for the history screen. */
    public List<Transaction> findByAccount(String accountNumber) {
        return Database.execute(conn -> load(conn, accountNumber, "DESC"));
    }

    /** Oldest first, used when an account is rebuilt from the database. */
    List<Transaction> loadOldestFirst(Connection conn, String accountNumber) throws SQLException {
        return load(conn, accountNumber, "ASC");
    }

    private List<Transaction> load(Connection conn, String accountNumber, String order) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, type, amount, transaction_date, balance_after, reference "
                + "FROM bank_transaction WHERE account_number=? ORDER BY seq " + order;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(rs.getString("transaction_id"),
                            TransactionType.valueOf(rs.getString("type")),
                            rs.getBigDecimal("amount"),
                            rs.getObject("transaction_date", LocalDateTime.class),
                            rs.getBigDecimal("balance_after"),
                            rs.getString("reference")));
                }
            }
        }
        return list;
    }
}

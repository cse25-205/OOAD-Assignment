package com.bank.dao;

import com.bank.model.InterestPayment;
import com.bank.model.PaymentStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

/** Data access for the interest_payment table. One row per account and month (UNIQUE constraint). */
public class InterestPaymentDAO {

    /** Insert or update the row for this account and month. Joins an existing database transaction. */
    public void save(Connection conn, InterestPayment p) throws SQLException {
        String sql = "MERGE INTO interest_payment (payment_id, account_number, period, amount, status, transaction_id) "
                + "KEY (account_number, period) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPaymentId());
            ps.setString(2, p.getAccount().getAccountNumber());
            ps.setString(3, p.getPeriod().toString());
            ps.setBigDecimal(4, p.getAmount());
            ps.setString(5, p.getStatus().name());
            ps.setString(6, p.getTransaction() == null ? null : p.getTransaction().getTransactionId());
            ps.executeUpdate();
        }
    }

    public void save(InterestPayment p) {
        Database.execute(conn -> {
            save(conn, p);
            return null;
        });
    }

    /** The months for which this account has already been paid interest. */
    Set<YearMonth> creditedPeriods(Connection conn, String accountNumber) throws SQLException {
        Set<YearMonth> periods = new HashSet<>();
        String sql = "SELECT period FROM interest_payment WHERE account_number=? AND status=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, PaymentStatus.CREDITED.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    periods.add(YearMonth.parse(rs.getString(1)));
                }
            }
        }
        return periods;
    }

    /** @return the stored status for this account and month, or null if there is no payment yet */
    public PaymentStatus statusOf(String accountNumber, YearMonth period) {
        return Database.execute(conn -> {
            String sql = "SELECT status FROM interest_payment WHERE account_number=? AND period=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, accountNumber);
                ps.setString(2, period.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? PaymentStatus.valueOf(rs.getString(1)) : null;
                }
            }
        });
    }

    public int count() {
        return Database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM interest_payment");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        });
    }
}

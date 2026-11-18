package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.ChequeAccount;
import com.bank.model.Customer;
import com.bank.model.IndividualCustomer;
import com.bank.model.InterestPayment;
import com.bank.model.InvestmentAccount;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CRUD for accounts. A loaded account is rebuilt with its full history and its credited interest months
 * (Account.restoreState). Balance changes and their records are saved in ONE database transaction.
 */
public class AccountDAO {
    private final CustomerDAO customerDAO;
    private final TransactionDAO transactionDAO;
    private final InterestPaymentDAO interestPaymentDAO;

    public AccountDAO(CustomerDAO customerDAO, TransactionDAO transactionDAO, InterestPaymentDAO interestPaymentDAO) {
        this.customerDAO = customerDAO;
        this.transactionDAO = transactionDAO;
        this.interestPaymentDAO = interestPaymentDAO;
    }

    private record Loaded(String number, String type, String customerId, String branch, BigDecimal balance,
                          List<Transaction> history, Set<YearMonth> creditedPeriods) { }

    /** Saves a new account and any transactions it already holds (e.g. the opening deposit). */
    public void insert(Account a) {
        Database.execute(conn -> {
            String sql = "INSERT INTO account (account_number, account_type, branch, balance, customer_id) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, a.getAccountNumber());
                ps.setString(2, a.getAccountType());
                ps.setString(3, a.getBranch());
                ps.setBigDecimal(4, a.getBalance());
                ps.setString(5, a.getOwner().getCustomerId());
                ps.executeUpdate();
            }
            for (Transaction t : a.getTransactions()) {
                transactionDAO.insert(conn, a.getAccountNumber(), t);
            }
            return null;
        });
    }

    public Account findByNumber(String accountNumber) {
        List<Loaded> rows = Database.execute(conn -> query(conn, "WHERE account_number=?", accountNumber));
        if (rows.isEmpty()) {
            return null;
        }
        return build(rows.get(0), customerDAO.findById(rows.get(0).customerId()));
    }

    /** Loads the customer's accounts. Each account is also registered with the customer the first time it is seen. */
    public List<Account> findByCustomer(Customer owner) {
        List<Loaded> rows = Database.execute(conn -> query(conn, "WHERE customer_id=?", owner.getCustomerId()));
        List<Account> accounts = new ArrayList<>();
        for (Loaded row : rows) {
            Account account = build(row, owner);
            attach(owner, account);
            accounts.add(account);
        }
        return accounts;
    }

    public List<Account> findAll() {
        Map<String, Customer> owners = new HashMap<>();
        customerDAO.findAll().forEach(c -> owners.put(c.getCustomerId(), c));
        List<Loaded> rows = Database.execute(conn -> query(conn, "", (String[]) null));
        List<Account> accounts = new ArrayList<>();
        for (Loaded row : rows) {
            Account account = build(row, owners.get(row.customerId()));
            attach(account.getOwner(), account);
            accounts.add(account);
        }
        return accounts;
    }

    /** Updates branch and balance of an existing account. */
    public void update(Account a) {
        Database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE account SET branch=?, balance=? WHERE account_number=?")) {
                ps.setString(1, a.getBranch());
                ps.setBigDecimal(2, a.getBalance());
                ps.setString(3, a.getAccountNumber());
                ps.executeUpdate();
            }
            return null;
        });
    }

    /** Atomically saves the new balance and the transaction (a deposit or withdrawal). */
    public void applyTransaction(Account a, Transaction t) {
        Database.execute(conn -> {
            updateBalance(conn, a);
            transactionDAO.insert(conn, a.getAccountNumber(), t);
            return null;
        });
    }

    /** Atomically saves the new balance, the INTEREST transaction and the CREDITED payment record. */
    public void applyInterest(Account a, InterestPayment payment) {
        Database.execute(conn -> {
            updateBalance(conn, a);
            transactionDAO.insert(conn, a.getAccountNumber(), payment.getTransaction());
            interestPaymentDAO.save(conn, payment);
            return null;
        });
    }

    public void delete(String accountNumber) {
        Database.execute(conn -> {
            for (String sql : new String[] {
                    "DELETE FROM interest_payment WHERE account_number=?",
                    "DELETE FROM bank_transaction WHERE account_number=?",
                    "DELETE FROM account WHERE account_number=?"}) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, accountNumber);
                    ps.executeUpdate();
                }
            }
            return null;
        });
    }

    public boolean accountNumberExists(String accountNumber) {
        return Database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM account WHERE account_number=?")) {
                ps.setString(1, accountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        });
    }

    public int count() {
        return Database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM account");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    private void attach(Customer owner, Account account) {
        boolean known = owner.getAccounts().stream().anyMatch(a -> a.getAccountNumber().equals(account.getAccountNumber()));
        if (!known) {
            owner.addAccount(account);
        }
    }

    private void updateBalance(Connection conn, Account a) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE account SET balance=? WHERE account_number=?")) {
            ps.setBigDecimal(1, a.getBalance());
            ps.setString(2, a.getAccountNumber());
            ps.executeUpdate();
        }
    }

    private List<Loaded> query(Connection conn, String where, String... params) throws SQLException {
        List<Loaded> rows = new ArrayList<>();
        String sql = "SELECT account_number, account_type, customer_id, branch, balance FROM account "
                + where + " ORDER BY account_number";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setString(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Loaded(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getBigDecimal(5), null, null));
                }
            }
        }
        List<Loaded> full = new ArrayList<>();
        for (Loaded row : rows) {
            full.add(new Loaded(row.number(), row.type(), row.customerId(), row.branch(), row.balance(),
                    transactionDAO.loadOldestFirst(conn, row.number()),
                    interestPaymentDAO.creditedPeriods(conn, row.number())));
        }
        return full;
    }

    private Account build(Loaded row, Customer owner) {
        Account account = switch (row.type()) {
            case "SAVINGS" -> new SavingsAccount(row.number(), row.branch(), owner);
            case "INVESTMENT" -> new InvestmentAccount(row.number(), row.branch(), owner);
            case "CHEQUE" -> new ChequeAccount(row.number(), row.branch(), (IndividualCustomer) owner);
            default -> throw new IllegalStateException("Unknown account type: " + row.type());
        };
        account.restoreState(row.balance(), row.history(), row.creditedPeriods());
        return account;
    }
}

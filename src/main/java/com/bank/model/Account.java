package com.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Account {
    private final String accountNumber;
    private final String branch;
    private final Customer owner;
    private BigDecimal balance = new BigDecimal("0.00");
    private final List<Transaction> transactions = new ArrayList<>();
    private final Set<YearMonth> interestPeriods = new HashSet<>();

    protected Account(String accountNumber, String branch, Customer owner) {
        if (owner == null) throw new IllegalArgumentException("An account must have an owner");
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.owner = owner;
    }

    public abstract String getAccountType();

    // ---- deposit (overloaded) ----
    public Transaction deposit(BigDecimal amount) {
        return deposit(amount, "Deposit");
    }

    public Transaction deposit(BigDecimal amount, String reference) {
        requireValidAmount(amount);
        credit(amount);
        return record(TransactionType.DEPOSIT, amount, reference);
    }

    // ---- balance changes: only subclasses and this class may use them ----
    protected void credit(BigDecimal amount) {
        balance = balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    protected void debit(BigDecimal amount) {
        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance = balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
    }

    protected Transaction record(TransactionType type, BigDecimal amount, String reference) {
        Transaction t = new Transaction(type, amount.setScale(2, RoundingMode.HALF_UP), balance, reference);
        transactions.add(t);
        return t;
    }

    /** Credits interest once per month; a second credit for the same month is refused (FR14). */
    protected Transaction applyInterest(YearMonth period, BigDecimal interest) {
        if (interestPeriods.contains(period)) {
            throw new IllegalStateException("Interest already credited for " + period);
        }
        credit(interest);
        interestPeriods.add(period);
        return record(TransactionType.INTEREST, interest, "Interest " + period);
    }

    public boolean isInterestCredited(YearMonth period) {
        return interestPeriods.contains(period);
    }

    protected static void requireValidAmount(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount is required");
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount can have at most two decimal places");
        }
    }

    /** For the DAO only: rebuilds an account's state from database rows. */
    public void restoreState(BigDecimal savedBalance, List<Transaction> history, Set<YearMonth> creditedPeriods) {
        this.balance = savedBalance.setScale(2, RoundingMode.HALF_UP);
        this.transactions.clear();
        this.transactions.addAll(history);
        this.interestPeriods.clear();
        this.interestPeriods.addAll(creditedPeriods);
    }

    public BigDecimal getBalance() { return balance; }
    public Customer getOwner() { return owner; }
    public String getAccountNumber() { return accountNumber; }
    public String getBranch() { return branch; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
}

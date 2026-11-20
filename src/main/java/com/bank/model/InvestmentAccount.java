package com.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

public class InvestmentAccount extends Account implements InterestBearing, Withdrawable {
    public static final BigDecimal MINIMUM_OPENING_DEPOSIT = new BigDecimal("500.00");
    private final BigDecimal monthlyRate = new BigDecimal("0.05"); // 5%

    //Opens a new account: the opening deposit must be at least BWP500.00.
    public InvestmentAccount(String accountNumber, String branch, Customer owner, BigDecimal openingDeposit) {
        super(accountNumber, branch, owner);
        if (openingDeposit == null || openingDeposit.compareTo(MINIMUM_OPENING_DEPOSIT) < 0) {
            throw new IllegalArgumentException("Opening deposit must be at least BWP500.00");
        }
        deposit(openingDeposit, "Opening deposit");
    }

    public InvestmentAccount(String accountNumber, String branch, Customer owner) {
        super(accountNumber, branch, owner);
    }

    @Override
    public String getAccountType() { return "INVESTMENT"; }

    @Override
    public Transaction withdraw(BigDecimal amount) {
        requireValidAmount(amount);
        debit(amount);
        return record(TransactionType.WITHDRAWAL, amount, "Withdrawal");
    }

    @Override
    public BigDecimal calculateInterest() {
        return getBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Transaction creditInterest(YearMonth period) {
        return applyInterest(period, calculateInterest());
    }
}

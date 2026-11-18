package com.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

/** Deposits and monthly interest only. It does not implement Withdrawable. */
public class SavingsAccount extends Account implements InterestBearing {
    private final BigDecimal monthlyRate = new BigDecimal("0.0005"); // 0.05%

    public SavingsAccount(String accountNumber, String branch, Customer owner) {
        super(accountNumber, branch, owner);
    }

    @Override
    public String getAccountType() { return "SAVINGS"; }

    @Override
    public BigDecimal calculateInterest() {
        return getBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Transaction creditInterest(YearMonth period) {
        return applyInterest(period, calculateInterest());
    }
}

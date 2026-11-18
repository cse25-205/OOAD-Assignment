package com.bank.model;

import java.math.BigDecimal;

/** Only an employed individual (with complete employment details) can own one. */
public class ChequeAccount extends Account implements Withdrawable {

    public ChequeAccount(String accountNumber, String branch, IndividualCustomer owner) {
        super(accountNumber, branch, owner);
        if (!owner.isEmployed()) {
            throw new IllegalArgumentException(
                "A Cheque account needs an employed individual with employer name and address");
        }
    }

    @Override
    public String getAccountType() { return "CHEQUE"; }

    @Override
    public Transaction withdraw(BigDecimal amount) {
        requireValidAmount(amount);
        debit(amount);
        return record(TransactionType.WITHDRAWAL, amount, "Withdrawal");
    }
}

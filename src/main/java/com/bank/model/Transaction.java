package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime transactionDate;
    private final BigDecimal balanceAfter;
    private final String reference;

    public Transaction(TransactionType type, BigDecimal amount, BigDecimal balanceAfter, String reference) {
        this(UUID.randomUUID().toString(), type, amount, LocalDateTime.now(), balanceAfter, reference);
    }

    public Transaction(String transactionId, TransactionType type, BigDecimal amount,
                       LocalDateTime transactionDate, BigDecimal balanceAfter, String reference) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.balanceAfter = balanceAfter;
        this.reference = reference;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getReference() {
        return reference;
    }

    public String getDetails() {
        return transactionDate + " | " + type + " | BWP " + amount + " | balance BWP " + balanceAfter
                + (reference == null || reference.isEmpty() ? "" : " | " + reference);
    }
}

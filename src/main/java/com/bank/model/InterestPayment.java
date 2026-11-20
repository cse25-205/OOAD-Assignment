package com.bank.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public class InterestPayment {
    private final String paymentId;
    private final Account account;
    private final YearMonth period;
    private BigDecimal amount = new BigDecimal("0.00");
    private PaymentStatus status = PaymentStatus.PENDING;
    private String failureReason;
    private Transaction transaction; // set when CREDITED

    public InterestPayment(String paymentId, Account account, YearMonth period) {
        if (!(account instanceof InterestBearing)) {
            throw new IllegalArgumentException(account.getAccountType() + " accounts do not earn interest");
        }
        this.paymentId = paymentId;
        this.account = account;
        this.period = period;
    }

    // Calculates and credits the interest. Any error moves the payment to FAILED.
    public void process() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only a PENDING payment can be processed");
        }
        try {
            Transaction t = ((InterestBearing) account).creditInterest(period);
            this.transaction = t;
            this.amount = t.getAmount();
            this.status = PaymentStatus.CREDITED;
            this.failureReason = null;
        } catch (RuntimeException e) {
            this.status = PaymentStatus.FAILED;
            this.failureReason = e.getMessage();
        }
    }

    //Retries a FAILED payment, unless interest was already credited for that account and month.
    public void retry() {
        if (status != PaymentStatus.FAILED) {
            throw new IllegalStateException("Only a FAILED payment can be retried");
        }
        if (account.isInterestCredited(period)) {
            throw new IllegalStateException("Interest already credited for " + period);
        }
        status = PaymentStatus.PENDING;
        process();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Account getAccount() {
        return account;
    }

    public YearMonth getPeriod() {
        return period;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}

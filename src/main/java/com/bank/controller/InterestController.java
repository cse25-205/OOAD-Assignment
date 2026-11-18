package com.bank.controller;

import com.bank.dao.AccountDAO;
import com.bank.dao.InterestPaymentDAO;
import com.bank.model.Account;
import com.bank.model.InterestBearing;
import com.bank.model.InterestPayment;
import com.bank.model.PaymentStatus;
import com.bank.util.BankException;
import java.time.YearMonth;
import java.util.UUID;

/** Runs the monthly interest job. Safe to run repeatedly: a month is never credited twice (FR14). */
public class InterestController {
    public record RunSummary(YearMonth period, int credited, int skipped, int failed) {
        @Override
        public String toString() {
            return "Interest run for " + period + ": " + credited + " credited, "
                    + skipped + " skipped, " + failed + " failed.";
        }
    }

    private final AccountDAO accountDAO;
    private final InterestPaymentDAO paymentDAO;

    public InterestController(AccountDAO accountDAO, InterestPaymentDAO paymentDAO) {
        this.accountDAO = accountDAO;
        this.paymentDAO = paymentDAO;
    }

    /**
     * Pays interest for the month to every interest-bearing account that has not been paid yet.
     * A FAILED payment leaves no CREDITED record, so the next run simply tries that account again.
     */
    public RunSummary runMonthlyInterest(YearMonth period) {
        int credited = 0;
        int skipped = 0;
        int failed = 0;
        for (Account account : accountDAO.findAll()) {
            if (!(account instanceof InterestBearing earner)
                    || earner.calculateInterest().signum() <= 0
                    || account.isInterestCredited(period)) {
                skipped++;
                continue;
            }
            InterestPayment payment = new InterestPayment(UUID.randomUUID().toString(), account, period);
            payment.process();
            if (persist(payment)) {
                credited++;
            } else {
                failed++;
            }
        }
        return new RunSummary(period, credited, skipped, failed);
    }

    /** @return true if the interest was credited and saved */
    private boolean persist(InterestPayment payment) {
        try {
            if (payment.getStatus() == PaymentStatus.CREDITED) {
                accountDAO.applyInterest(payment.getAccount(), payment);
                return true;
            }
            paymentDAO.save(payment);
            return false;
        } catch (BankException e) {
            return false;
        }
    }
}

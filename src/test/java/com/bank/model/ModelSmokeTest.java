package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class ModelSmokeTest {
    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        if (ok) passed++;
        else failed++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
    }

    static boolean throwsIAE(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    public static void main(String[] args) {
        IndividualCustomer iris = new IndividualCustomer("C1", "iris", Customer.hashPassword("pw1"),
                "Gaborone", "71000000", LocalDate.now(), "Iris", "Test", "123456789");
        OrganisationCustomer acme = new OrganisationCustomer("C2", "acme", Customer.hashPassword("pw2"),
                "Francistown", "acme@mail.bw", LocalDate.now(), "Acme Ltd", "BW0001",
                LocalDate.of(2015, 3, 1), "Bob");

        check("password check passes for right password", iris.verifyPassword("pw1"));
        check("password check fails for wrong password", !iris.verifyPassword("bad"));
        check("display names", iris.getDisplayName().equals("Iris Test") && acme.getDisplayName().equals("Acme Ltd"));

        // Savings
        SavingsAccount sav = new SavingsAccount("S1", "Main", iris);
        iris.addAccount(sav);
        check("savings is NOT withdrawable", !(((Object) sav) instanceof Withdrawable));
        Transaction d = sav.deposit(bd("1000"));
        check("deposit updates balance", sav.getBalance().compareTo(bd("1000")) == 0);
        check("deposit returns a transaction", d.getType() == TransactionType.DEPOSIT && d.getBalanceAfter().compareTo(bd("1000")) == 0);
        check("zero deposit rejected", throwsIAE(() -> sav.deposit(BigDecimal.ZERO)));
        check("negative deposit rejected", throwsIAE(() -> sav.deposit(bd("-5"))));
        check("3 decimal places rejected", throwsIAE(() -> sav.deposit(bd("1.005"))));
        sav.creditInterest(YearMonth.of(2026, 10));
        check("savings interest 0.05%", sav.getBalance().compareTo(bd("1000.50")) == 0);
        boolean dup = false;
        try {
            sav.creditInterest(YearMonth.of(2026, 10));
        } catch (IllegalStateException e) {
            dup = true;
        }
        check("second interest credit same month refused", dup);

        // Investment
        check("investment below BWP500 rejected", throwsIAE(() -> new InvestmentAccount("I0", "Main", acme, bd("499.99"))));
        InvestmentAccount inv = new InvestmentAccount("I1", "Main", acme, bd("500"));
        acme.addAccount(inv);
        check("investment opens with exactly BWP500", inv.getBalance().compareTo(bd("500")) == 0);
        inv.creditInterest(YearMonth.of(2026, 10));
        check("investment interest 5%", inv.getBalance().compareTo(bd("525.00")) == 0);
        check("overdraw rejected", throwsIAE(() -> inv.withdraw(bd("600"))));
        inv.withdraw(bd("25.00"));
        check("withdrawal reduces balance", inv.getBalance().compareTo(bd("500.00")) == 0);
        check("history has 3 transactions", inv.getTransactions().size() == 3);

        // Cheque
        check("cheque refused for unemployed individual", throwsIAE(() -> new ChequeAccount("Q0", "Main", iris)));
        iris.setEmploymentDetails(new EmploymentDetails("BAC", "Gaborone"));
        ChequeAccount chq = new ChequeAccount("Q1", "Main", iris);
        iris.addAccount(chq);
        chq.deposit(bd("200"));
        chq.withdraw(bd("50"));
        check("cheque deposit and withdraw", chq.getBalance().compareTo(bd("150.00")) == 0);

        // Ownership
        check("account cannot be added to another customer", throwsIAE(() -> acme.addAccount(sav)));

        // Interest payment lifecycle
        SavingsAccount sav2 = new SavingsAccount("S2", "Main", iris);
        sav2.deposit(bd("2000"));
        InterestPayment p1 = new InterestPayment("P1", sav2, YearMonth.of(2026, 11));
        check("new payment is PENDING", p1.getStatus() == PaymentStatus.PENDING);
        p1.process();
        check("process -> CREDITED", p1.getStatus() == PaymentStatus.CREDITED && p1.getAmount().compareTo(bd("1.00")) == 0);
        check("payment produces a transaction", p1.getTransaction() != null);
        InterestPayment p2 = new InterestPayment("P2", sav2, YearMonth.of(2026, 11));
        p2.process();
        check("duplicate month -> FAILED", p2.getStatus() == PaymentStatus.FAILED);
        boolean noRetry = false;
        try {
            p2.retry();
        } catch (IllegalStateException e) {
            noRetry = true;
        }
        check("retry refused when already credited", noRetry);
        check("cheque payment not allowed", throwsIAE(() -> new InterestPayment("P3", chq, YearMonth.of(2026, 11))));

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}

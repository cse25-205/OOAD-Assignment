package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.ChequeAccount;
import com.bank.model.Customer;
import com.bank.model.EmploymentDetails;
import com.bank.model.IndividualCustomer;
import com.bank.model.InvestmentAccount;
import com.bank.model.OrganisationCustomer;
import com.bank.model.SavingsAccount;
import com.bank.model.Withdrawable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Inserts demonstration data (5 customers, 9 accounts, many transactions) when the database is empty. */
public final class SampleDataLoader {
    public static final String SAMPLE_PASSWORD = "Bank@2026";
    private static final String BRANCH = "Gaborone Main";

    private SampleDataLoader() { }

    public static void loadIfEmpty(CustomerDAO customers, AccountDAO accounts) {
        if (customers.count() > 0) {
            return;
        }
        IndividualCustomer thato = person(customers, "C0000001", "thato", "Thato", "Molefe", "880101123",
                "Plot 12, Gaborone", "71234567", new EmploymentDetails("Choppies Enterprises", "Broadhurst, Gaborone"));
        IndividualCustomer neo = person(customers, "C0000002", "neo", "Neo", "Kgosi", "920505456",
                "Block 8, Francistown", "72345678", null);
        IndividualCustomer kabo = person(customers, "C0000003", "kabo", "Kabo", "Sebina", "850303789",
                "Jwaneng Ext 3", "73456789", new EmploymentDetails("Debswana", "Jwaneng Mine, Jwaneng"));
        OrganisationCustomer kalahari = organisation(customers, "C0000004", "kalahari", "Kalahari Traders (Pty) Ltd",
                "BW00001234567", LocalDate.of(2015, 6, 1), "Plot 55, Maun", "6861234", "Mpho Dube");
        OrganisationCustomer baobab = organisation(customers, "C0000005", "baobab", "Baobab Holdings",
                "BW00007654321", LocalDate.of(2019, 2, 14), "Main Mall, Gaborone", "3951234", "Lorato Phiri");

        save(accounts, new SavingsAccount("1000000001", BRANCH, thato), null, "2500.00", "30.00");
        save(accounts, new InvestmentAccount("1000000002", BRANCH, thato, bd("1000.00")), "200.00");
        save(accounts, new ChequeAccount("1000000003", BRANCH, thato), null, "8500.00", "1200.00");
        save(accounts, new SavingsAccount("1000000004", BRANCH, neo), null, "1200.00", "100.00");
        save(accounts, new ChequeAccount("1000000005", BRANCH, kabo), null, "5000.00", "750.00");
        save(accounts, new InvestmentAccount("1000000006", BRANCH, kabo, bd("700.00")), null);
        save(accounts, new SavingsAccount("1000000007", BRANCH, kalahari), null, "20000.00", "5000.00");
        save(accounts, new InvestmentAccount("1000000008", BRANCH, kalahari, bd("15000.00")), "1000.00", "2500.00");
        save(accounts, new SavingsAccount("1000000009", BRANCH, baobab), null, "3000.00");
    }

    private static IndividualCustomer person(CustomerDAO dao, String id, String username, String first, String surname,
                                             String idNumber, String address, String contact, EmploymentDetails job) {
        IndividualCustomer c = new IndividualCustomer(id, username, Customer.hashPassword(SAMPLE_PASSWORD),
                address, contact, LocalDate.now(), first, surname, idNumber);
        c.setEmploymentDetails(job);
        dao.insert(c);
        return c;
    }

    private static OrganisationCustomer organisation(CustomerDAO dao, String id, String username, String name,
                                                     String regNo, LocalDate regDate, String address,
                                                     String contact, String person) {
        OrganisationCustomer c = new OrganisationCustomer(id, username, Customer.hashPassword(SAMPLE_PASSWORD),
                address, contact, LocalDate.now(), name, regNo, regDate, person);
        dao.insert(c);
        return c;
    }

    /**
     * Builds the account's history in memory (optional withdrawal first argument, then deposits), then
     * saves the account and all of its transactions in one go.
     */
    private static void save(AccountDAO accounts, Account account, String withdrawal, String... deposits) {
        for (String deposit : deposits) {
            if (deposit != null) {
                account.deposit(bd(deposit), "Sample deposit");
            }
        }
        if (withdrawal != null && account instanceof Withdrawable w) {
            w.withdraw(bd(withdrawal));
        }
        account.getOwner().addAccount(account);
        accounts.insert(account);
    }

    private static BigDecimal bd(String value) { return new BigDecimal(value); }
}

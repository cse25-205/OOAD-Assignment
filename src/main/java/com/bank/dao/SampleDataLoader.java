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

//Inserts demonstration data (10 customers, 17 accounts, many transactions) when the database is empty.
public final class SampleDataLoader {
    public static final String SAMPLE_PASSWORD = "bank@2026";
    private static final String BRANCH = "Gaborone Main";

    private SampleDataLoader() { }

    public static void loadIfEmpty(CustomerDAO customers, AccountDAO accounts) {
        if (customers.count() > 0) {
            return;
        }
        // person(dao, customerId, USERNAME, first name, surname, ID number, address, phone, employer)
        // organisation(dao, customerId, USERNAME, ORGANISATION NAME, registration number, date, address, phone, contact person)
        // A username must be lowercase with no spaces.
        IndividualCustomer resego = person(customers, "C0000001", "resego", "Resego", "Moremi", "881014201",
                "Plot 1204, Gaborone West", "71230001", new EmploymentDetails("Botswana Power Corporation", "Motlakase House, Gaborone"));
        IndividualCustomer faith = person(customers, "C0000002", "faith", "Faith", "Smith", "900322118",
                "Block 6, Francistown", "72340002", new EmploymentDetails("Mascom Wireless", "Plot 50676, Gaborone"));
        IndividualCustomer kagiso = person(customers, "C0000003", "kagiso", "Kagiso", "Baker", "950607334",
                "Ward 4, Molepolole", "73450003", null);
        IndividualCustomer layla = person(customers, "C0000004", "layla", "Layla", "Ontatlhile", "870911456",
                "Extension 9, Palapye", "74560004", new EmploymentDetails("Water Utilities Corporation", "Palapye Branch"));
        IndividualCustomer tumelo = person(customers, "C0000005", "tumelo", "Tumelo", "Kgathi", "930218572",
                "Phase 2, Maun", "75670005", null);
        IndividualCustomer davidson = person(customers, "C0000006", "davidson", "Davidson", "Modise", "840430689",
                "Area W, Selebi-Phikwe", "76780006", new EmploymentDetails("First National Bank Botswana", "Main Mall, Francistown"));
        IndividualCustomer sarona = person(customers, "C0000007", "sarona", "Sarona", "Arends", "970105791",
                "Ward 12, Kanye", "77890007", null);
        OrganisationCustomer dube = organisation(customers, "C0000008", "dube",
                "Dube Holdings", "BW00001234501", LocalDate.of(2015, 6, 1),
                "Plot 55, Maun", "6861234", "Mpho Dube");
        OrganisationCustomer innovate = organisation(customers, "C0000009", "innovate",
                "Innovate Hub", "BW00002345602", LocalDate.of(2018, 3, 12),
                "Industrial Site, Lobatse", "5332345", "Oratilwe Sebego");
        OrganisationCustomer evergreen = organisation(customers, "C0000010", "evergreen",
                "Evergreen Ventures", "BW00003456703", LocalDate.of(2021, 9, 20),
                "Fairgrounds Office Park, Gaborone", "3903456", "Gosego Mothusi");

        save(accounts, new SavingsAccount("1000000001", BRANCH, resego), null, "3200.00", "150.00");
        save(accounts, new InvestmentAccount("1000000002", BRANCH, resego, bd("1500.00")), "300.00");
        save(accounts, new ChequeAccount("1000000003", BRANCH, resego), null, "12000.00");
        save(accounts, new ChequeAccount("1000000004", BRANCH, faith), "1800.00", "9500.00");
        save(accounts, new SavingsAccount("1000000005", BRANCH, faith), null, "800.00");
        save(accounts, new SavingsAccount("1000000006", BRANCH, kagiso), null, "2100.00", "400.00");
        save(accounts, new ChequeAccount("1000000007", BRANCH, layla), "900.00", "7600.00");
        save(accounts, new InvestmentAccount("1000000008", BRANCH, layla, bd("2000.00")), null, "500.00");
        save(accounts, new SavingsAccount("1000000009", BRANCH, tumelo), null, "1500.00");
        save(accounts, new InvestmentAccount("1000000010", BRANCH, tumelo, bd("650.00")), null);
        save(accounts, new ChequeAccount("1000000011", BRANCH, davidson), "600.00", "6400.00");
        save(accounts, new SavingsAccount("1000000012", BRANCH, sarona), null, "950.00");
        save(accounts, new SavingsAccount("1000000013", BRANCH, dube), null, "25000.00", "4000.00");
        save(accounts, new InvestmentAccount("1000000014", BRANCH, dube, bd("18000.00")), "2500.00");
        save(accounts, new SavingsAccount("1000000015", BRANCH, innovate), null, "6200.00");
        save(accounts, new SavingsAccount("1000000016", BRANCH, evergreen), null, "3300.00");
        save(accounts, new InvestmentAccount("1000000017", BRANCH, evergreen, bd("5000.00")), null, "1000.00");
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

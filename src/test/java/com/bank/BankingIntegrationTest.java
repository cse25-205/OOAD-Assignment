package com.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bank.controller.AppContext;
import com.bank.controller.InterestController;
import com.bank.dao.Database;
import com.bank.dao.SampleDataLoader;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.util.BankException;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankingIntegrationTest {
    private AppContext app;

    @BeforeAll
    static void useInMemoryDatabase() {
        Database.configure("jdbc:h2:mem:banktest;DB_CLOSE_DELAY=-1");
    }

    @BeforeEach
    void freshDatabase() {
        Database.reset();
        app = new AppContext();
    }

    private void registerAndLoginIndividual(String username) {
        app.registration().registerIndividual(username, "Secret#123", "Tumi", "Ndlovu", "ID-" + username,
                "Plot 1, Gaborone", "71000000", null, null);
        app.login().login(username, "Secret#123");
    }

    private BigDecimal balanceInDatabase(String accountNumber) {
        return app.accountDAO().findByNumber(accountNumber).getBalance();
    }

    @Test
    void IT01_registerThenLoginSucceeds() {
        registerAndLoginIndividual("tumi");
        assertTrue(app.login().isLoggedIn());
        assertEquals("Tumi Ndlovu", app.login().requireLogin().getDisplayName());
    }

    @Test
    void IT02_badLoginsAllGiveTheSameGenericMessage() {
        registerAndLoginIndividual("tumi");
        app.login().logout();
        String wrongPassword = assertThrows(BankException.class, () -> app.login().login("tumi", "wrong")).getMessage();
        String unknownUser = assertThrows(BankException.class, () -> app.login().login("nobody", "Secret#123")).getMessage();
        String missing = assertThrows(BankException.class, () -> app.login().login("", "")).getMessage();
        assertEquals(wrongPassword, unknownUser);
        assertEquals(wrongPassword, missing);
        assertFalse(app.login().isLoggedIn());
    }

    @Test
    void IT03_passwordsAreStoredHashedNotPlain() {
        registerAndLoginIndividual("tumi");
        String stored = app.customerDAO().findByUsername("tumi").getPasswordHash();
        assertFalse(stored.contains("Secret#123"));
        assertEquals(Customer.hashPassword("Secret#123"), stored);
        assertEquals(64, stored.length());
    }

    @Test
    void IT04_duplicateRegistrationIsRejectedWithTheCause() {
        registerAndLoginIndividual("tumi");
        BankException dupUser = assertThrows(BankException.class, () -> app.registration().registerIndividual(
                "tumi", "Secret#123", "A", "B", "OTHER-ID", "addr", "123", null, null));
        assertTrue(dupUser.getMessage().contains("username"));
        BankException dupId = assertThrows(BankException.class, () -> app.registration().registerIndividual(
                "someoneelse", "Secret#123", "A", "B", "ID-tumi", "addr", "123", null, null));
        assertTrue(dupId.getMessage().contains("ID number"));
        BankException missing = assertThrows(BankException.class, () -> app.registration().registerIndividual(
                "third", "Secret#123", "", "B", "ID-3", "addr", "123", null, null));
        assertTrue(missing.getMessage().contains("first name"));
    }

    @Test
    void IT05_depositUpdatesTheDatabaseAndHistory() {
        registerAndLoginIndividual("tumi");
        Account savings = app.accounts().openAccount("Savings", "100", null, null);
        app.accounts().deposit(savings.getAccountNumber(), "250.50");

        assertEquals(new BigDecimal("350.50"), balanceInDatabase(savings.getAccountNumber()));
        List<Transaction> history = app.accounts().history(savings.getAccountNumber());
        assertEquals(2, history.size());
        assertEquals(TransactionType.DEPOSIT, history.get(0).getType());
        assertEquals(new BigDecimal("250.50"), history.get(0).getAmount());
        assertEquals(new BigDecimal("350.50"), history.get(0).getBalanceAfter());
    }

    @Test
    void IT06_invalidAmountsAreRejectedAndBalanceIsUnchanged() {
        registerAndLoginIndividual("tumi");
        Account savings = app.accounts().openAccount("Savings", "100", null, null);
        String number = savings.getAccountNumber();
        for (String bad : new String[] {"", "abc", "0", "-5", "1.005"}) {
            assertThrows(BankException.class, () -> app.accounts().deposit(number, bad));
        }
        assertEquals(new BigDecimal("100.00"), balanceInDatabase(number));
    }

    @Test
    void IT07_savingsWithdrawalIsAlwaysRejected() {
        registerAndLoginIndividual("tumi");
        Account savings = app.accounts().openAccount("Savings", "1000", null, null);
        BankException e = assertThrows(BankException.class, () -> app.accounts().withdraw(savings.getAccountNumber(), "10"));
        assertTrue(e.getMessage().contains("not allowed"));
        assertEquals(new BigDecimal("1000.00"), balanceInDatabase(savings.getAccountNumber()));
    }

    @Test
    void IT08_investmentRequiresMinimumOpeningDepositOf500() {
        registerAndLoginIndividual("tumi");
        assertThrows(BankException.class, () -> app.accounts().openAccount("Investment", "499.99", null, null));
        assertThrows(BankException.class, () -> app.accounts().openAccount("Investment", "", null, null));
        Account ok = app.accounts().openAccount("Investment", "500.00", null, null);
        assertEquals(new BigDecimal("500.00"), balanceInDatabase(ok.getAccountNumber()));
        assertEquals(1, app.accounts().listAccounts().size());
    }

    @Test
    void IT09_withdrawalsWorkButNeverOverdraw() {
        registerAndLoginIndividual("tumi");
        Account investment = app.accounts().openAccount("Investment", "600", null, null);
        String number = investment.getAccountNumber();
        app.accounts().withdraw(number, "100");
        assertEquals(new BigDecimal("500.00"), balanceInDatabase(number));
        BankException e = assertThrows(BankException.class, () -> app.accounts().withdraw(number, "500.01"));
        assertTrue(e.getMessage().contains("Insufficient funds"));
        assertEquals(new BigDecimal("500.00"), balanceInDatabase(number));
    }

    @Test
    void IT10_chequeAccountIsOnlyForEmployedIndividuals() {
        registerAndLoginIndividual("tumi");
        assertThrows(BankException.class, () -> app.accounts().openAccount("Cheque", "0", null, null));
        Account cheque = app.accounts().openAccount("Cheque", "0", "Mascom", "Gaborone");
        assertEquals("CHEQUE", cheque.getAccountType());
        assertTrue(((com.bank.model.IndividualCustomer) app.customerDAO().findByUsername("tumi")).isEmployed());

        app.login().logout();
        app.registration().registerOrganisation("acme", "Secret#123", "Acme Ltd", "BW1", LocalDate.of(2020, 1, 1),
                "Maun", "6860000", "Boss");
        app.login().login("acme", "Secret#123");
        assertThrows(BankException.class, () -> app.accounts().openAccount("Cheque", "0", "Acme", "Maun"));
        assertEquals("SAVINGS", app.accounts().openAccount("Savings", "", null, null).getAccountType());
    }

    @Test
    void IT11_aCustomerCanHoldSeveralAccountsIncludingTwoOfTheSameType() {
        registerAndLoginIndividual("tumi");
        app.accounts().openAccount("Savings", "10", null, null);
        app.accounts().openAccount("Savings", "20", null, null);
        app.accounts().openAccount("Investment", "500", null, null);
        assertEquals(3, app.accounts().listAccounts().size());
    }

    @Test
    void IT12_customersCanOnlyTouchTheirOwnAccounts() {
        registerAndLoginIndividual("tumi");
        Account mine = app.accounts().openAccount("Savings", "100", null, null);
        app.login().logout();
        registerAndLoginIndividual("other");
        assertThrows(BankException.class, () -> app.accounts().deposit(mine.getAccountNumber(), "50"));
        assertThrows(BankException.class, () -> app.accounts().history(mine.getAccountNumber()));
        assertEquals(new BigDecimal("100.00"), balanceInDatabase(mine.getAccountNumber()));
    }

    @Test
    void IT13_nothingWorksWhenLoggedOut() {
        registerAndLoginIndividual("tumi");
        Account a = app.accounts().openAccount("Savings", "100", null, null);
        app.login().logout();
        assertThrows(BankException.class, () -> app.accounts().deposit(a.getAccountNumber(), "5"));
        assertThrows(BankException.class, () -> app.accounts().listAccounts());
    }

    @Test
    void IT14_sessionEndsAfterFiveMinutesOfInactivity() {
        registerAndLoginIndividual("tumi");
        Instant now = Instant.now();
        app.login().setClock(Clock.fixed(now, ZoneId.systemDefault()));
        app.login().touch();
        app.login().setClock(Clock.fixed(now.plusSeconds(4 * 60), ZoneId.systemDefault()));
        assertTrue(app.login().checkSession());
        app.login().setClock(Clock.fixed(now.plusSeconds(10 * 60), ZoneId.systemDefault()));
        assertThrows(BankException.class, () -> app.accounts().listAccounts());
        assertFalse(app.login().isLoggedIn());
    }

    @Test
    void IT15_interestUsesTheRightRatesAndIsNeverCreditedTwiceForAMonth() {
        registerAndLoginIndividual("tumi");
        Account savings = app.accounts().openAccount("Savings", "1000", null, null);
        Account investment = app.accounts().openAccount("Investment", "1000", null, null);
        Account cheque = app.accounts().openAccount("Cheque", "1000", "Mascom", "Gaborone");
        YearMonth month = YearMonth.of(2026, 11);

        InterestController.RunSummary first = app.interest().runMonthlyInterest(month);
        assertEquals(2, first.credited());
        assertEquals(new BigDecimal("1000.50"), balanceInDatabase(savings.getAccountNumber()));
        assertEquals(new BigDecimal("1050.00"), balanceInDatabase(investment.getAccountNumber()));
        assertEquals(new BigDecimal("1000.00"), balanceInDatabase(cheque.getAccountNumber()));

        InterestController.RunSummary second = app.interest().runMonthlyInterest(month);
        assertEquals(0, second.credited());
        assertEquals(new BigDecimal("1000.50"), balanceInDatabase(savings.getAccountNumber()));
        assertEquals(new BigDecimal("1050.00"), balanceInDatabase(investment.getAccountNumber()));

        app.interest().runMonthlyInterest(month.plusMonths(1));
        assertEquals(new BigDecimal("1001.00"), balanceInDatabase(savings.getAccountNumber()));
        List<Transaction> history = app.accounts().history(savings.getAccountNumber());
        assertEquals(TransactionType.INTEREST, history.get(0).getType());
    }

    @Test
    void IT16_sampleDataLoadsOnceAndTheSampleUsersCanLogIn() {
        SampleDataLoader.loadIfEmpty(app.customerDAO(), app.accountDAO());
        SampleDataLoader.loadIfEmpty(app.customerDAO(), app.accountDAO());
        assertEquals(10, app.customerDAO().count());
        assertEquals(17, app.accountDAO().count());
        app.login().login("resego", SampleDataLoader.SAMPLE_PASSWORD);
        assertEquals(3, app.accounts().listAccounts().size());
        assertNotNull(app.customerDAO().findByUsername("dube"));
        assertNull(app.customerDAO().findByUsername("ghost"));
    }
}

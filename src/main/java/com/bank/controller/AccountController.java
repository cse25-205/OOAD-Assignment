package com.bank.controller;

import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.TransactionDAO;
import com.bank.model.Account;
import com.bank.model.ChequeAccount;
import com.bank.model.Customer;
import com.bank.model.EmploymentDetails;
import com.bank.model.IndividualCustomer;
import com.bank.model.InvestmentAccount;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import com.bank.model.Withdrawable;
import com.bank.util.BankException;
import com.bank.util.Money;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.Supplier;

public class AccountController {
    public static final String DEFAULT_BRANCH = "Gaborone Main";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LoginController session;
    private final AccountDAO accountDAO;
    private final CustomerDAO customerDAO;
    private final TransactionDAO transactionDAO;

    public AccountController(LoginController session, AccountDAO accountDAO,
                             CustomerDAO customerDAO, TransactionDAO transactionDAO) {
        this.session = session;
        this.accountDAO = accountDAO;
        this.customerDAO = customerDAO;
        this.transactionDAO = transactionDAO;
    }

    public List<Account> listAccounts() {
        return accountDAO.findByCustomer(session.requireLogin());
    }

    public Account openAccount(String type, String openingDeposit, String employerName, String employerAddress) {
        Customer customer = session.requireLogin();
        String kind = type == null ? "" : type.trim().toUpperCase();
        BigDecimal opening = (openingDeposit == null || openingDeposit.isBlank())
                ? BigDecimal.ZERO : Money.parse(openingDeposit);
        if (opening.signum() < 0) {
            throw new BankException("The opening deposit cannot be negative.");
        }
        String number = nextAccountNumber();

        boolean employmentChanged = false;
        Account account;
        switch (kind) {
            case "SAVINGS" -> {
                account = guard(() -> new SavingsAccount(number, DEFAULT_BRANCH, customer));
                openingDepositIfAny(account, opening);
            }
            case "INVESTMENT" -> account = guard(() -> new InvestmentAccount(number, DEFAULT_BRANCH, customer, opening));
            case "CHEQUE" -> {
                if (!(customer instanceof IndividualCustomer person)) {
                    throw new BankException("A Cheque account can only be opened for an employed individual.");
                }
                if (!person.isEmployed()) {
                    EmploymentDetails details = new EmploymentDetails(employerName, employerAddress);
                    if (details.isComplete()) {
                        person.setEmploymentDetails(new EmploymentDetails(employerName.trim(), employerAddress.trim()));
                        employmentChanged = true;
                    }
                }
                try {
                    account = guard(() -> new ChequeAccount(number, DEFAULT_BRANCH, person));
                } catch (BankException e) {
                    person.setEmploymentDetails(null);
                    throw e;
                }
                openingDepositIfAny(account, opening);
            }
            default -> throw new BankException("Choose an account type: Savings, Investment or Cheque.");
        }
        accountDAO.insert(account);
        customer.addAccount(account);
        if (employmentChanged) {
            customerDAO.update(customer);
        }
        return account;
    }

    public Transaction deposit(String accountNumber, String amountText) {
        Account account = ownedAccount(accountNumber);
        BigDecimal amount = Money.parse(amountText);
        Transaction transaction = guard(() -> account.deposit(amount));
        accountDAO.applyTransaction(account, transaction);
        return transaction;
    }

    public Transaction withdraw(String accountNumber, String amountText) {
        Account account = ownedAccount(accountNumber);
        if (!(account instanceof Withdrawable withdrawable)) {
            throw new BankException("Withdrawals are not allowed from a " + displayType(account) + " account.");
        }
        BigDecimal amount = Money.parse(amountText);
        Transaction transaction = guard(() -> withdrawable.withdraw(amount));
        accountDAO.applyTransaction(account, transaction);
        return transaction;
    }

    public List<Transaction> history(String accountNumber) {
        return transactionDAO.findByAccount(ownedAccount(accountNumber).getAccountNumber());
    }

    private void openingDepositIfAny(Account account, BigDecimal opening) {
        if (opening.signum() > 0) {
            guard(() -> account.deposit(opening, "Opening deposit"));
        }
    }

    /** Loads the account and checks it belongs to the logged-in customer. */
    private Account ownedAccount(String accountNumber) {
        Customer customer = session.requireLogin();
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BankException("Select an account first.");
        }
        Account account = accountDAO.findByNumber(accountNumber.trim());
        if (account == null || !account.getOwner().getCustomerId().equals(customer.getCustomerId())) {
            throw new BankException("That account was not found among your accounts.");
        }
        return account;
    }

    private static <T> T guard(Supplier<T> action) {
        try {
            return action.get();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BankException(e.getMessage(), e);
        }
    }

    private static String displayType(Account account) {
        String type = account.getAccountType();
        return type.charAt(0) + type.substring(1).toLowerCase();
    }

    private String nextAccountNumber() {
        String number;
        do {
            number = "10" + String.format("%08d", RANDOM.nextInt(100_000_000));
        } while (accountDAO.accountNumberExists(number));
        return number;
    }
}

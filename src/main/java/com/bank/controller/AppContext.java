package com.bank.controller;

import com.bank.dao.Database;
import com.bank.dao.SampleDataLoader;
import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.InterestPaymentDAO;
import com.bank.dao.TransactionDAO;

public class AppContext {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final InterestPaymentDAO interestPaymentDAO = new InterestPaymentDAO();

    private final AccountDAO accountDAO =
            new AccountDAO(customerDAO, transactionDAO, interestPaymentDAO);

    private final LoginController loginController =
            new LoginController(customerDAO);

    private final RegistrationController registrationController =
            new RegistrationController(customerDAO);

    private final AccountController accountController =
            new AccountController(loginController, accountDAO, customerDAO, transactionDAO);

    private final InterestController interestController =
            new InterestController(accountDAO, interestPaymentDAO);


    // Controller access methods
    public LoginController login() {
        return loginController;
    }

    public RegistrationController registration() {
        return registrationController;
    }

    public AccountController accounts() {
        return accountController;
    }

    public InterestController interest() {
        return interestController;
    }


    // DAO access methods (used for demonstration data)
    public CustomerDAO customerDAO() {
        return customerDAO;
    }

    public AccountDAO accountDAO() {
        return accountDAO;
    }

    public void start() {
        Database.init();
        SampleDataLoader.loadIfEmpty(customerDAO, accountDAO);
    }

}
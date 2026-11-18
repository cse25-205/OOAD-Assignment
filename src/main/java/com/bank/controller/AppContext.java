package com.bank.controller;

import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.InterestPaymentDAO;
import com.bank.dao.TransactionDAO;

/** Wires the DAOs and controllers together. The views receive controllers only, never DAOs. */
public class AppContext {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final InterestPaymentDAO interestPaymentDAO = new InterestPaymentDAO();
    private final AccountDAO accountDAO = new AccountDAO(customerDAO, transactionDAO, interestPaymentDAO);

    private final LoginController loginController = new LoginController(customerDAO);
    private final RegistrationController registrationController = new RegistrationController(customerDAO);
    private final AccountController accountController =
            new AccountController(loginController, accountDAO, customerDAO, transactionDAO);
    private final InterestController interestController = new InterestController(accountDAO, interestPaymentDAO);

    public LoginController login() { return loginController; }
    public RegistrationController registration() { return registrationController; }
    public AccountController accounts() { return accountController; }
    public InterestController interest() { return interestController; }

    /** Used only at start-up to load the demonstration data. */
    public CustomerDAO customerDAO() { return customerDAO; }
    public AccountDAO accountDAO() { return accountDAO; }
}

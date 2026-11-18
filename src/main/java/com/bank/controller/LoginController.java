package com.bank.controller;

import com.bank.dao.CustomerDAO;
import com.bank.util.BankException;
import com.bank.model.Customer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Handles login, logout and the 5-minute inactivity timeout. Holds the current session. */
public class LoginController {
    public static final int SESSION_TIMEOUT_MINUTES = 5;
    private static final String GENERIC_ERROR = "Invalid username or password.";

    private final CustomerDAO customerDAO;
    private Clock clock = Clock.systemDefaultZone();
    private Customer current;
    private Instant lastActivity;

    public LoginController(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    /** Missing, unknown or wrong details all give the SAME message so nothing is revealed. */
    public Customer login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new BankException(GENERIC_ERROR);
        }
        Customer customer = customerDAO.findByUsername(username.trim().toLowerCase());
        if (customer == null || !customer.verifyPassword(password)) {
            throw new BankException(GENERIC_ERROR);
        }
        current = customer;
        touch();
        return customer;
    }

    public void logout() {
        current = null;
        lastActivity = null;
    }

    /** Returns the logged-in customer, or throws if nobody is logged in or the session timed out. */
    public Customer requireLogin() {
        if (current == null) {
            throw new BankException("Please log in to continue.");
        }
        if (isExpired()) {
            logout();
            throw new BankException("Your session ended after " + SESSION_TIMEOUT_MINUTES
                    + " minutes of inactivity. Please log in again.");
        }
        touch();
        return current;
    }

    /** @return true while a customer is logged in and the session has not expired; logs out if it has. */
    public boolean checkSession() {
        if (current != null && isExpired()) {
            logout();
        }
        return current != null;
    }

    /** Records user activity (called by the GUI on mouse and key events). */
    public void touch() {
        if (current != null) {
            lastActivity = clock.instant();
        }
    }

    public boolean isLoggedIn() { return current != null; }

    /** Lets tests move time forward. */
    public void setClock(Clock clock) { this.clock = clock; }

    private boolean isExpired() {
        return lastActivity != null
                && Duration.between(lastActivity, clock.instant()).toMinutes() >= SESSION_TIMEOUT_MINUTES;
    }
}

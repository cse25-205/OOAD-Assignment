package com.bank.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Customer {
    private final String customerId;
    private final String username;
    private final String passwordHash;
    private final String address;
    private final String contactDetails;
    private final LocalDate dateRegistered;
    private final List<Account> accounts = new ArrayList<>();

    protected Customer(String customerId, String username, String passwordHash,
                       String address, String contactDetails, LocalDate dateRegistered) {
        this.customerId = customerId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.address = address;
        this.contactDetails = contactDetails;
        this.dateRegistered = dateRegistered;
    }

    public abstract String getDisplayName();

    /** Hashes the typed password and compares it with the stored hash. */
    public boolean verifyPassword(String input) {
        return input != null && passwordHash.equals(hashPassword(input));
    }

    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addAccount(Account account) {
        if (account.getOwner() != this) {
            throw new IllegalArgumentException("Account belongs to a different customer");
        }
        if (!accounts.contains(account)) accounts.add(account);
    }

    public List<Account> getAccounts() { return Collections.unmodifiableList(accounts); }

    public String getCustomerId() { return customerId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getAddress() { return address; }
    public String getContactDetails() { return contactDetails; }
    public LocalDate getDateRegistered() { return dateRegistered; }
}

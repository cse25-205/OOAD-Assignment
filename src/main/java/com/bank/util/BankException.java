package com.bank.util;

/** Thrown for any business-rule or validation failure; the message is safe to show to the user. */
public class BankException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BankException(String message) { super(message); }
    public BankException(String message, Throwable cause) { super(message, cause); }
}

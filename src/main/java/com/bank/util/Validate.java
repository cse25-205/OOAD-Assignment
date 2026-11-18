package com.bank.util;

public final class Validate {
    private Validate() { }

    /** Returns the trimmed value or throws a BankException naming the missing detail. */
    public static String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new BankException("Missing required detail: " + label + ".");
        }
        return value.trim();
    }
}

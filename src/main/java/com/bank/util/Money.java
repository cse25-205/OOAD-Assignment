package com.bank.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Monetary policy: BigDecimal, two decimal places, HALF_UP rounding. */
public final class Money {
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Money() { }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Parses user text such as "1,250.50". */
    public static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) {
            throw new BankException("Enter an amount greater than zero, for example 150.00.");
        }
        try {
            return new BigDecimal(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new BankException("The amount must be a number, for example 150.00.");
        }
    }

    public static BigDecimal parsePositive(String text) {
        return requirePositive(parse(text));
    }

    public static BigDecimal requirePositive(BigDecimal value) {
        if (value == null || round(value).signum() <= 0) {
            throw new BankException("The amount must be greater than zero.");
        }
        return round(value);
    }

    public static String format(BigDecimal value) {
        return String.format("BWP %,.2f", value);
    }
}

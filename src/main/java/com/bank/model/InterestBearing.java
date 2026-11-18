package com.bank.model;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Implemented by accounts that earn monthly interest (Savings, Investment). */
public interface InterestBearing {
    BigDecimal calculateInterest();
    Transaction creditInterest(YearMonth period);
}

package com.bank.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface InterestBearing {
    BigDecimal calculateInterest();
    Transaction creditInterest(YearMonth period);
}

package com.bank.model;

import java.math.BigDecimal;

/** Implemented by accounts that allow withdrawals (Investment, Cheque). Savings does not. */
public interface Withdrawable {
    Transaction withdraw(BigDecimal amount);
}

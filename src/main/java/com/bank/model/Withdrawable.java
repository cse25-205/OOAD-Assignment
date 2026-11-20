package com.bank.model;

import java.math.BigDecimal;

public interface Withdrawable {
    Transaction withdraw(BigDecimal amount);
}

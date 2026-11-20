package com.bank.view;

public interface Navigator {
    void showLogin(String message);

    void showRegister();

    void showDashboard();

    Theme theme();

    void setTheme(Theme theme);
}

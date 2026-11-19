package com.bank.view;

/** Lets a screen ask the application to show another screen or change the theme. */
public interface Navigator {
    void showLogin(String message);
    void showRegister();
    void showDashboard();

    Theme theme();
    void setTheme(Theme theme);
}

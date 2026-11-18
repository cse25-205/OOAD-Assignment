package com.bank.view;

/** Lets a screen ask the application to show another screen. */
public interface Navigator {
    void showLogin(String message);
    void showRegister();
    void showDashboard();
}

package com.bank.view;

import com.bank.controller.LoginController;
import com.bank.util.BankException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Boundary class: shows the login form and passes the input to LoginController. No logic here. */
public class LoginView extends VBox {

    public LoginView(Navigator navigator, LoginController controller, String message) {
        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new Insets(30));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordToggleField password = new PasswordToggleField();
        password.setPromptText("Password");
        Label feedback = Ui.errorLabel();
        feedback.setText(message == null ? "" : message);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setMaxWidth(420);
        form.addRow(0, new Label("Username"), username);
        form.addRow(1, new Label("Password"), password);

        Button login = Ui.primary("Log in");
        Runnable submit = () -> {
            try {
                controller.login(username.getText(), password.getText());
                navigator.showDashboard();
            } catch (BankException e) {
                feedback.setText(e.getMessage());
                password.clear();
            }
        };
        login.setOnAction(e -> submit.run());
        password.setOnAction(e -> submit.run());

        Button register = Ui.link("New customer? Register here");
        register.setOnAction(e -> navigator.showRegister());

        getChildren().addAll(Ui.title("Banking System"), new Label("Please log in to continue"),
                form, login, feedback, register, new ThemePicker(navigator));
    }
}

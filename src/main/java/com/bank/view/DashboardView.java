package com.bank.view;

import com.bank.controller.AccountController;
import com.bank.controller.InterestController;
import com.bank.controller.LoginController;
import com.bank.model.Account;
import com.bank.util.BankException;
import com.bank.util.Money;
import com.bank.model.Transaction;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Boundary class for the logged-in customer: accounts, deposit/withdraw, open account, history, interest.
 * It only displays data and forwards input to the controllers; every rule is enforced there.
 */
public class DashboardView extends BorderPane {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Navigator navigator;
    private final LoginController login;
    private final AccountController accounts;
    private final InterestController interest;

    private final TableView<Account> accountTable = new TableView<>();
    private final TableView<Transaction> historyTable = new TableView<>();
    private final ComboBox<Account> historyAccount = new ComboBox<>();
    private final TextField amount = new TextField();
    private final Label accountsMessage = Ui.errorLabel();

    public DashboardView(Navigator navigator, LoginController login, AccountController accounts,
                         InterestController interest) {
        this.navigator = navigator;
        this.login = login;
        this.accounts = accounts;
        this.interest = interest;

        Label welcome = Ui.title("Welcome, " + login.requireLogin().getDisplayName());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = Ui.primary("Log out");
        logout.setOnAction(e -> {
            login.logout();
            navigator.showLogin("You have been logged out.");
        });
        HBox top = new HBox(16, welcome, spacer, new ThemePicker(navigator), logout);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        top.setPadding(new Insets(16));
        setTop(top);

        TabPane tabs = new TabPane(accountsTab(), openAccountTab(), historyTab(), interestTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        setCenter(tabs);
        refresh();
    }

    private Tab accountsTab() {
        accountTable.getColumns().add(Ui.<Account>column("Account number", 160, Account::getAccountNumber));
        accountTable.getColumns().add(Ui.<Account>column("Type", 120, a -> Ui.titleCase(a.getAccountType())));
        accountTable.getColumns().add(Ui.<Account>column("Branch", 140, Account::getBranch));
        accountTable.getColumns().add(Ui.<Account>column("Balance", 160, a -> Money.format(a.getBalance())));
        accountTable.setPlaceholder(new Label("You have no accounts yet. Open one in the next tab."));

        amount.setPromptText("Amount, e.g. 250.00");
        Button deposit = Ui.primary("Deposit");
        deposit.setOnAction(e -> act(() -> {
            accounts.deposit(selectedNumber(), amount.getText());
            accountsMessage.setText("");
            amount.clear();
            Ui.info("Deposit successful.");
        }));
        Button withdraw = Ui.primary("Withdraw");
        withdraw.setOnAction(e -> act(() -> {
            accounts.withdraw(selectedNumber(), amount.getText());
            accountsMessage.setText("");
            amount.clear();
            Ui.info("Withdrawal successful.");
        }));

        VBox box = new VBox(12, Ui.heading("Your accounts and balances"), accountTable,
                new HBox(10, new Label("Selected account:"), amount, deposit, withdraw), accountsMessage);
        box.setPadding(new Insets(16));
        VBox.setVgrow(accountTable, Priority.ALWAYS);
        return new Tab("Accounts", box);
    }

    private Tab openAccountTab() {
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Savings", "Investment", "Cheque"));
        type.setValue("Savings");
        TextField opening = new TextField();
        opening.setPromptText("Investment needs at least 500.00");
        TextField employer = new TextField();
        employer.setPromptText("Cheque accounts only");
        TextField employerAddress = new TextField();
        employerAddress.setPromptText("Cheque accounts only");
        Label message = Ui.errorLabel();

        Button open = Ui.primary("Open account");
        open.setOnAction(e -> {
            try {
                Account account = accounts.openAccount(type.getValue(), opening.getText(),
                        employer.getText(), employerAddress.getText());
                message.setText("");
                opening.clear();
                refresh();
                Ui.info(Ui.titleCase(account.getAccountType()) + " account " + account.getAccountNumber() + " opened.");
            } catch (BankException ex) {
                message.setText(ex.getMessage());
            }
        });

        javafx.scene.layout.GridPane form = new javafx.scene.layout.GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Account type"), type);
        form.addRow(1, new Label("Opening deposit (BWP)"), opening);
        form.addRow(2, new Label("Employer name"), employer);
        form.addRow(3, new Label("Employer address"), employerAddress);

        VBox box = new VBox(12, Ui.heading("Open a new account"), form, open, message);
        box.setPadding(new Insets(16));
        return new Tab("Open account", box);
    }

    private Tab historyTab() {
        historyAccount.setPromptText("Choose an account");
        historyAccount.setConverter(new StringConverter<>() {
            @Override public String toString(Account a) { return a == null ? "" : Ui.titleCase(a.getAccountType()) + " - " + a.getAccountNumber(); }
            @Override public Account fromString(String s) { return null; }
        });
        historyAccount.setOnAction(e -> act(this::loadHistory));

        historyTable.getColumns().add(Ui.<Transaction>column("Date and time", 170, t -> t.getTransactionDate().format(TIME)));
        historyTable.getColumns().add(Ui.<Transaction>column("Type", 110, t -> t.getType().name()));
        historyTable.getColumns().add(Ui.<Transaction>column("Amount", 130, t -> Money.format(t.getAmount())));
        historyTable.getColumns().add(Ui.<Transaction>column("Balance after", 140, t -> Money.format(t.getBalanceAfter())));
        historyTable.getColumns().add(Ui.<Transaction>column("Reference", 180, Transaction::getReference));
        historyTable.setPlaceholder(new Label("Choose an account to see its transactions, newest first."));

        VBox box = new VBox(12, Ui.heading("Transaction history"), historyAccount, historyTable);
        box.setPadding(new Insets(16));
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        return new Tab("History", box);
    }

    private Tab interestTab() {
        Label result = new Label();
        Button run = Ui.primary("Run this month's interest now");
        run.setOnAction(e -> act(() -> {
            result.setText(interest.runMonthlyInterest(YearMonth.now()).toString());
            refresh();
        }));
        Label note = new Label("Interest normally runs automatically on the 1st of each month. This button is a demonstration "
                + "of the same job. Running it twice in one month never pays interest twice.");
        note.setWrapText(true);
        VBox box = new VBox(12, Ui.heading("Monthly interest"), note, run, result);
        box.setPadding(new Insets(16));
        return new Tab("Interest", box);
    }

    private String selectedNumber() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getAccountNumber();
    }

    private void loadHistory() {
        Account selected = historyAccount.getValue();
        historyTable.setItems(FXCollections.observableArrayList(
                accounts.history(selected == null ? null : selected.getAccountNumber())));
    }

    private void refresh() {
        try {
            String selectedNumber = selectedNumber();
            var list = accounts.listAccounts();
            accountTable.setItems(FXCollections.observableArrayList(list));
            historyAccount.setItems(FXCollections.observableArrayList(list));
            list.stream().filter(a -> a.getAccountNumber().equals(selectedNumber)).findFirst()
                    .ifPresent(a -> accountTable.getSelectionModel().select(a));
            if (historyAccount.getValue() != null) {
                loadHistory();
            }
        } catch (BankException e) {
            sessionProblem(e);
        }
    }

    /** Runs a user action, shows any BankException as a message, then refreshes the tables. */
    private void act(Runnable action) {
        try {
            action.run();
            refresh();
        } catch (BankException e) {
            if (!login.isLoggedIn()) {
                sessionProblem(e);
            } else {
                accountsMessage.setText(e.getMessage());
            }
        }
    }

    private void sessionProblem(BankException e) {
        navigator.showLogin(e.getMessage());
    }
}

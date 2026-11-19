package com.bank.view;

import com.bank.controller.AppContext;
import com.bank.controller.InterestScheduler;
import com.bank.dao.Database;
import com.bank.dao.SampleDataLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.stage.Stage;
import java.util.prefs.Preferences;
import javafx.util.Duration;

/** Application entry point: wires the layers together and switches between screens. */
public class MainApp extends Application implements Navigator {
    private Stage stage;
    private AppContext app;
    private InterestScheduler scheduler;
    private boolean onDashboard;
    private final Preferences preferences = Preferences.userNodeForPackage(MainApp.class);
    private Theme theme;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        theme = Theme.fromName(preferences.get("theme", Theme.BOTSWANA_BLUE.name()));
        Database.init();
        app = new AppContext();
        SampleDataLoader.loadIfEmpty(app.customerDAO(), app.accountDAO());

        scheduler = new InterestScheduler(app.interest());
        scheduler.start();

        // Every 10 seconds check whether the 5-minute inactivity limit has been reached.
        Timeline sessionWatch = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            if (onDashboard && !app.login().checkSession()) {
                showLogin("Your session ended after " + com.bank.controller.LoginController.SESSION_TIMEOUT_MINUTES
                        + " minutes of inactivity. Please log in again.");
            }
        }));
        sessionWatch.setCycleCount(Timeline.INDEFINITE);
        sessionWatch.play();

        stage.setTitle("Banking System");
        showLogin(null);
        stage.show();
    }

    @Override
    public void showLogin(String message) {
        onDashboard = false;
        show(new LoginView(this, app.login(), message));
    }

    @Override
    public void showRegister() {
        onDashboard = false;
        show(new RegisterView(this, app.registration()));
    }

    @Override
    public void showDashboard() {
        onDashboard = true;
        show(new DashboardView(this, app.login(), app.accounts(), app.interest()));
    }

    private void show(Parent root) {
        Scene scene = new Scene(root, 1020, 700);
        scene.getStylesheets().setAll(theme.stylesheets());
        scene.addEventFilter(InputEvent.ANY, e -> app.login().touch());
        stage.setScene(scene);
    }

    @Override
    public Theme theme() { return theme; }

    @Override
    public void setTheme(Theme newTheme) {
        theme = newTheme;
        preferences.put("theme", newTheme.name());
        stage.getScene().getStylesheets().setAll(newTheme.stylesheets());
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.bank.view;

import java.util.function.Function;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;

/** Small helpers so every screen uses the same labels, buttons and messages. */
final class Ui {
    static final String PRIMARY = "-fx-background-color: #1f4e79; -fx-text-fill: white; -fx-font-weight: bold;";
    static final String LINK = "-fx-background-color: transparent; -fx-text-fill: #1f4e79; -fx-underline: true;";

    private Ui() { }

    /** "SAVINGS" -> "Savings" */
    static String titleCase(String text) {
        return text.isEmpty() ? text : text.charAt(0) + text.substring(1).toLowerCase();
    }

    static Label title(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1f4e79;");
        return label;
    }

    static Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return label;
    }

    static Label errorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #b00020;");
        label.setWrapText(true);
        return label;
    }

    static Button primary(String text) {
        Button button = new Button(text);
        button.setStyle(PRIMARY);
        return button;
    }

    static Button link(String text) {
        Button button = new Button(text);
        button.setStyle(LINK);
        return button;
    }

    static void info(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    static void error(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    static <T> TableColumn<T, String> column(String title, double width, Function<T, String> text) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new SimpleStringProperty(text.apply(cell.getValue())));
        return column;
    }
}

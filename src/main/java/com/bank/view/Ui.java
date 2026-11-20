package com.bank.view;

import java.util.function.Function;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;


final class Ui {
    private Ui() {
    }

    static String titleCase(String text) {
        return text.isEmpty() ? text : text.charAt(0) + text.substring(1).toLowerCase();
    }

    static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("title");
        return label;
    }

    static Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("heading");
        return label;
    }

    static Label errorLabel() {
        Label label = new Label();
        label.getStyleClass().add("error");
        label.setWrapText(true);
        return label;
    }

    static Button primary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary");
        return button;
    }

    static Button link(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("link");
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

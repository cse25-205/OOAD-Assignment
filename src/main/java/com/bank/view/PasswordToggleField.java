package com.bank.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;

// A password box with an eye button: click it to show the typed text, click again to hide it.
public class PasswordToggleField extends HBox {
    private final PasswordField hidden = new PasswordField();
    private final TextField shown = new TextField();
    private final Line slash = new Line(3, 3, 21, 17);
    private boolean revealed;

    public PasswordToggleField() {
        super(4);
        setAlignment(Pos.CENTER_LEFT);
        shown.textProperty().bindBidirectional(hidden.textProperty());
        shown.setVisible(false);
        shown.setManaged(false);
        hidden.setMaxWidth(Double.MAX_VALUE);
        shown.setMaxWidth(Double.MAX_VALUE);

        Region eye = new Region();
        eye.getStyleClass().add("eye-icon");
        slash.getStyleClass().add("eye-slash");
        slash.setVisible(false);
        StackPane icon = new StackPane(eye, slash);

        Button toggle = new Button();
        toggle.getStyleClass().add("eye");
        toggle.setGraphic(icon);
        toggle.setFocusTraversable(false);
        toggle.setOnAction(e -> toggle());

        StackPane fields = new StackPane(hidden, shown);
        HBox.setHgrow(fields, Priority.ALWAYS);
        getChildren().addAll(fields, toggle);
    }

    private void toggle() {
        revealed = !revealed;
        hidden.setVisible(!revealed);
        hidden.setManaged(!revealed);
        shown.setVisible(revealed);
        shown.setManaged(revealed);
        slash.setVisible(revealed);
        (revealed ? shown : hidden).requestFocus();
        (revealed ? shown : hidden).positionCaret(hidden.getText().length());
    }

    public String getText() {
        return hidden.getText();
    }

    public void clear() {
        hidden.clear();
    }

    public void setPromptText(String text) {
        hidden.setPromptText(text);
        shown.setPromptText(text);
    }

    public void setOnAction(EventHandler<ActionEvent> handler) {
        hidden.setOnAction(handler);
        shown.setOnAction(handler);
    }
}

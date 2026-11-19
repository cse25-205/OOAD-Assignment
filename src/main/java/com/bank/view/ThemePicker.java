package com.bank.view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/** A small "Theme: [ ... ]" drop-down that switches the whole application look immediately. */
public class ThemePicker extends HBox {

    public ThemePicker(Navigator navigator) {
        super(8);
        setAlignment(Pos.CENTER);
        setMaxWidth(USE_PREF_SIZE);
        ComboBox<Theme> box = new ComboBox<>(FXCollections.observableArrayList(Theme.available()));
        box.setValue(navigator.theme());
        box.setOnAction(e -> navigator.setTheme(box.getValue()));
        getChildren().addAll(new Label("Theme"), box);
        // With only one theme left there is nothing to choose, so the drop-down hides itself.
        if (box.getItems().size() < 2) {
            setVisible(false);
            setManaged(false);
        }
    }
}

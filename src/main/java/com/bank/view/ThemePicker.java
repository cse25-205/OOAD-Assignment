package com.bank.view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ThemePicker extends HBox {

    public ThemePicker(Navigator navigator) {
        super(8);
        setAlignment(Pos.CENTER);
        setMaxWidth(USE_PREF_SIZE);
        ComboBox<Theme> box = new ComboBox<>(FXCollections.observableArrayList(Theme.available()));
        box.setValue(navigator.theme());
        box.setOnAction(e -> navigator.setTheme(box.getValue()));
        getChildren().addAll(new Label("Theme"), box);

        if (box.getItems().size() < 2) {
            setVisible(false);
            setManaged(false);
        }
    }
}

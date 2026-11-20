package com.bank.view;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum Theme {
    BOTSWANA_BLUE("Botswana Blue", "blue"),
    DARK("Dark Mode", "dark");

    private final String label;
    private final String file;

    Theme(String label, String file) {
        this.label = label;
        this.file = file;
    }

    boolean isAvailable() {
        return Theme.class.getResource(path()) != null;
    }

    public static List<Theme> available() {
        return Arrays.stream(values()).filter(Theme::isAvailable).toList();
    }

    private String path() {
        return "/css/themes/" + file + ".css";
    }

    List<String> stylesheets() {
        return List.of(url("/css/app.css"), url(path()));
    }

    static Theme fromName(String name) {
        List<Theme> themes = available();
        if (themes.isEmpty()) {
            throw new IllegalStateException("No theme files found in resources/css/themes - keep at least one.");
        }
        for (Theme theme : themes) {
            if (theme.name().equals(name)) {
                return theme;
            }
        }
        return themes.get(0);
    }

    private static String url(String path) {
        return Objects.requireNonNull(Theme.class.getResource(path), path).toExternalForm();
    }

    @Override
    public String toString() {
        return label;
    }
}

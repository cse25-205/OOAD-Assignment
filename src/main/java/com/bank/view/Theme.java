package com.bank.view;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** The colour themes the user can switch between. Each one is a small CSS file in resources/css/themes. */
public enum Theme {
    BOTSWANA_BLUE("Botswana Blue", "blue"),
    DARK("Dark Mode", "dark"),
    KALAHARI_SAND("Kalahari Sand", "sand"),
    OKAVANGO_GREEN("Okavango Green", "green"),
    MIDNIGHT_PURPLE("Midnight Purple", "purple"),
    HIGH_CONTRAST("High Contrast", "contrast");

    private final String label;
    private final String file;

    Theme(String label, String file) {
        this.label = label;
        this.file = file;
    }

    /** True while this theme's CSS file is still in resources/css/themes. Delete the file to remove the theme. */
    boolean isAvailable() {
        return Theme.class.getResource(path()) != null;
    }

    /** Every theme whose CSS file exists (this is what the drop-down offers). */
    public static List<Theme> available() {
        return Arrays.stream(values()).filter(Theme::isAvailable).toList();
    }

    private String path() { return "/css/themes/" + file + ".css"; }

    /** The shared stylesheet followed by this theme's colours. */
    List<String> stylesheets() {
        return List.of(url("/css/app.css"), url(path()));
    }

    /** The saved theme if it still exists, otherwise the first theme that does. */
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
    public String toString() { return label; }
}

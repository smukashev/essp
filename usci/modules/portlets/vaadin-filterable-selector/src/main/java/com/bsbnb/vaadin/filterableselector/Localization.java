package com.bsbnb.vaadin.filterableselector;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public enum Localization {

    USER_HAS_NO_ACCESS_TO_CREDITORS("USER-HAS-NO-ACCESS-TO-CREDITORS"),
    CREDITORS_HEADER("CREDITORS-HEADER"),
    SUBJECT_TYPES_LABEL("SUBJECT-TYPES-LABEL"),
    MARK_ALL_BUTTON_CAPTION("MARK-ALL-BUTTON-CAPTION"),
    UNMARK_ALL_BUTTON_CAPTION("UNMARK-ALL-BUTTON-CAPTION"),
    SELECT_ALL_BUTTON_CAPTION("SELECT-ALL-BUTTON-CAPTION"),
    SELECTION_EMPTY_PROMPT("SELECTION-EMPTY-PROMPT"),
    SELECTION_EMPTY_CAPTION("SELECTION-EMPTY-CAPTION");
    private static final String BUNDLE_NAME = "com.bsbnb.vaadin.filterableselector.content.Language";
    private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("ru", "RU"));

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("ru", "RU"));
    }
    private String key;

    private Localization(String key) {
        this.key = key;
    }

    public String getValue() {
        return bundle.getString(key);
    }

    public String getKey() {
        return key;
    }
}

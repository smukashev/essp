package com.bsbnb.creditregistry.portlets.notifications.ui;

/**
 *
 * @author Aidar.Myrzahanov
 */
public enum Localization {

    CAPTION_TEXT("CAPTION-TEXT"),
    NOTIFICATIONS_TAB_NAME("NOTIFICATIONS-TAB-NAME"),
    ADMIN_TAB_NAME("ADMIN-TAB-NAME"),
    START_MAIL_HANDLING("START-MAIL-HANDLING"),
    STOP_MAIL_HANDLING("STOP-MAIL-HANDLING"),
    SAVE("SAVE"),
    SETTINGS_SAVED_MESSAGE("SETTINGS-SAVED-MESSAGE"),
    MAIL_HANDLING_STARTED("MAIL-HANDLING-STARTED"),
    MAIL_HANDLING_STOPPED("MAIL-HANDLING-STOPPED"),
    SETTINGS_TAB_NAME("SETTINGS-TAB-NAME"), 
    SETTINGS_HEADER("SETTINGS-HEADER");
    private final String key;

    private Localization(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

package com.bsbnb.creditregistry.portlets.serverbrowser;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

/**
 *
 * @author Aidar.Myrzahanov
 */
class PathButton extends Button {

    private final String path;

    PathButton(String name, String targetPath, Button.ClickListener listener) {
        this.path = targetPath;
        setCaption(name);
        setStyleName(BaseTheme.BUTTON_LINK);
        addListener(listener);
    }

    String getPath() {
        return path;
    }
}

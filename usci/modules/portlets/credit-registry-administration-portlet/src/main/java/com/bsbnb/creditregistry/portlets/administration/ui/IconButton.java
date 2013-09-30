package com.bsbnb.creditregistry.portlets.administration.ui;
import com.bsbnb.creditregistry.portlets.administration.PortletIcon;
import com.vaadin.ui.Button;
/**
 *
 * @author Marat Madybayev
 */
public class IconButton extends Button {
    public IconButton(String description, PortletIcon icon, Button.ClickListener listener) {
        setCaption(null);
        setDescription(description);
        setIcon(icon);
        addListener(listener);
    }
}

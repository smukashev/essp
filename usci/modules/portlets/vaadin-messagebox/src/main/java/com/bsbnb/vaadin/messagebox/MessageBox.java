package com.bsbnb.vaadin.messagebox;

import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Aidar.Myrzahanov
 */
public class MessageBox extends Window implements Button.ClickListener {

    private Label textLabel;
    private HorizontalLayout buttonsLayout;
    private HorizontalLayout mainLayout;
    private VerticalLayout subLayout;
    private List<MessageBoxListener> listeners;
    private Embedded icon;

    private MessageBox(String text, String caption, MessageBoxListener listener, MessageBoxButtons buttons, MessageBoxType type) {
        setSizeUndefined();
        //textLabel
        textLabel = new Label(text, Label.CONTENT_XHTML);
        //buttonsLayout
        buttonsLayout = new HorizontalLayout();
        setButtons(buttons);
        //subLayout
        subLayout = new VerticalLayout();
        subLayout.addComponent(textLabel);
        subLayout.addComponent(buttonsLayout);
        //mainLayout
        setType(type);
        mainLayout = new HorizontalLayout();
        if (icon != null) {
            mainLayout.addComponent(icon);
        }
        mainLayout.addComponent(subLayout);
        //window
        VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeUndefined();
        layout.addComponent(mainLayout);
        setCaption(caption);
        setModal(true);
        listeners = new ArrayList<MessageBoxListener>();
        listeners.add(listener);

    }

    private void setButtons(MessageBoxButtons buttons) {
        MessageBoxButton[] boxButtons = buttons.getButtons();
        buttonsLayout.setSpacing(true);
        for (MessageBoxButton button : boxButtons) {
            button.addListener(this);
            if(button.getResult()==MessageResult.OK||button.getResult()==MessageResult.Yes) {
                button.setClickShortcut(ShortcutAction.KeyCode.ENTER);
            } else if(button.getResult()==MessageResult.Cancel) {
                button.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
            }
            buttonsLayout.addComponent(button);
        }
    }

    private void setType(MessageBoxType type) {
        String iconName = "";
        switch (type) {
            case Question:
                iconName = "help";
                break;
            case Information:
                iconName = "attention";
                break;
            case Error:
                iconName = "cancel";
                break;
            default:
                throw new UnsupportedOperationException("Тип не поддерживается(" + type + ");");
        }
        if (iconName.length() > 0) {
            icon = new Embedded("", new ThemeResource("../runo/icons/32/" + iconName + ".png"));
            icon.setWidth("32px");
            icon.setHeight("32px");
        }
    }

    public static void Show(String text, String caption, MessageBoxListener listener, MessageBoxButtons buttons, MessageBoxType type, Window window) {
        MessageBox message = new MessageBox(text, caption, listener, buttons, type);
        if (window != null) {
            window.addWindow(message);
            message.focus();
        }
    }

    public static void Show(String text, String caption, MessageBoxListener listener, MessageBoxButtons buttons, Window window) {
        Show(text, caption, listener, buttons, MessageBoxType.None, window);
    }

    public static void Show(String text, String caption, MessageBoxListener listener, Window window) {
        Show(text, caption, listener, MessageBoxButtons.OK, MessageBoxType.None, window);
    }

    public static void Show(String text, String caption, Window window) {
        Show(text, caption, null, MessageBoxButtons.OK, MessageBoxType.None, window);
    }

    public static void Show(String text, Window window) {
        Show(text, "Message", null, MessageBoxButtons.OK, MessageBoxType.None, window);
    }

    @Override
    public void buttonClick(ClickEvent event) {
        MessageBoxButton button = (MessageBoxButton) event.getButton();
        sendResult(button.getResult());
        close();
    }

    public void sendResult(MessageResult result) {
        for (MessageBoxListener listener : listeners) {
            if (listener != null) {
                listener.messageResult(result);
            }
        }
    }
}

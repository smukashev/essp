package com.bsbnb.vaadin.messagebox;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class InputBox extends Window implements Button.ClickListener{
    
    private Label promptLabel;
    private HorizontalLayout buttonsLayout;
    private HorizontalLayout mainLayout;
    private VerticalLayout subLayout;
    private List<InputBoxListener> listeners;
    private Embedded icon;
    private TextField textField;
    
    private InputBox(String text, String caption, InputBoxListener listener, MessageBoxButtons buttons, MessageBoxType type) {
        setSizeUndefined();
        //promptLabel
        promptLabel = new Label(text, Label.CONTENT_XHTML);
        //textField 
        textField = new TextField("");
        //buttonsLayout
        buttonsLayout = new HorizontalLayout();
        setButtons(buttons);
        //subLayout
        subLayout = new VerticalLayout();
        subLayout.addComponent(promptLabel);
        subLayout.addComponent(textField);
        subLayout.addComponent(buttonsLayout);
        //mainLayout
        setType(type);
        mainLayout = new HorizontalLayout();
        if(icon!=null) {
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
        listeners = new ArrayList<InputBoxListener>();
        listeners.add(listener);
    }
    
    public String getText() {
        return textField.getValue().toString();
    }
    
    private void setButtons(MessageBoxButtons buttons) {
        MessageBoxButton[] boxButtons = buttons.getButtons();
        buttonsLayout.setSpacing(true);
        for(MessageBoxButton button : boxButtons) {
            button.addListener(this);
            buttonsLayout.addComponent(button);
        }
    }
    
    private void setType(MessageBoxType type) {
        String iconName = "";
        switch(type) {
            case Question: iconName = "help"; break;
            case Information: iconName = "attention"; break;
            case Error: iconName = "cancel"; break;
        }
        if(iconName.length()>0) {
            icon = new Embedded("", new ThemeResource("../runo/icons/32/"+iconName+".png"));
            icon.setWidth("32px");
            icon.setHeight("32px");
        }
    }

    public void buttonClick(ClickEvent event) {
        MessageBoxButton button = (MessageBoxButton) event.getButton();        
        sendResult(button.getResult());
        close();
    }
    
    public void sendResult(MessageResult result) {
        for(InputBoxListener listener : listeners) {
            listener.inputResult(result, getText());
        }
    }
    
    public static void Show(String text, String caption, InputBoxListener listener, MessageBoxButtons buttons, MessageBoxType type, Window window) {
        InputBox message = new InputBox(text, caption, listener,buttons, type);
        window.addWindow(message);
    }
    
    public static void Show(String text, String caption, InputBoxListener listener, MessageBoxButtons buttons, Window window) {
        Show(text,caption, listener, buttons, MessageBoxType.None, window);
    }
    
    public static void Show(String text, String caption, InputBoxListener listener, Window window) {
        Show(text,caption,listener, MessageBoxButtons.OK, MessageBoxType.None, window);
    }
    
    public static void Show(String text, String caption, Window window) {
        Show(text,caption,null, MessageBoxButtons.OK, MessageBoxType.None, window);
    }
    
    public static void Show(String text, Window window) {
        Show(text,"Message",null, MessageBoxButtons.OK, MessageBoxType.None, window);
    }
}

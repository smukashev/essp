/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.vaadin.messagebox;

import com.vaadin.ui.Button;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class MessageBoxButton extends Button{
    private MessageResult result;
    
    private MessageBoxButton(String caption, MessageResult result) {
        setCaption(caption);
        this.result = result;
    }
    
    public static final MessageBoxButton OK = new MessageBoxButton("OK", MessageResult.OK);
    public static final MessageBoxButton CANCEL = new MessageBoxButton("Cancel", MessageResult.Cancel);
    public static final MessageBoxButton NO = new MessageBoxButton("No", MessageResult.No);
    public static final MessageBoxButton YES = new MessageBoxButton("Yes", MessageResult.Yes);
    
    public MessageResult getResult() {
        return result;
    }
}

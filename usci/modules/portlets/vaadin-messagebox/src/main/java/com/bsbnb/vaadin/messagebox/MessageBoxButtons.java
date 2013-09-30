/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.vaadin.messagebox;

import com.vaadin.ui.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public enum MessageBoxButtons {
    OK(MessageBoxButton.OK), 
    OKCancel(MessageBoxButton.OK, MessageBoxButton.CANCEL), 
    YesNo(MessageBoxButton.YES, MessageBoxButton.NO), 
    YesNoCancel(MessageBoxButton.YES, MessageBoxButton.NO, MessageBoxButton.CANCEL);
    MessageBoxButtons(MessageBoxButton... buttons) {
        this.buttons = buttons;
    }
    private MessageBoxButton[] buttons;
    public MessageBoxButton[] getButtons() {
        return buttons;
    }
}

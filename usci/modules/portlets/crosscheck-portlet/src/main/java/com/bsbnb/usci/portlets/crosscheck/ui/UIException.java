package com.bsbnb.usci.portlets.crosscheck.ui;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class UIException extends Exception{
    public UIException(Exception inner) {
        super(inner);
    }
    
    public UIException(String message) {
        super(message);
    }
}

package com.bsbnb.usci.portlets.crosscheck.data;


public class DataException extends Exception{
    public DataException() {
        super();
    }
    
    public DataException(Exception exception) {
        super(exception);
    }
    
    public DataException(String message) {
        super(message);
    }
    
    public DataException(String message, Exception cause) {
        super(message, cause);
    }
}
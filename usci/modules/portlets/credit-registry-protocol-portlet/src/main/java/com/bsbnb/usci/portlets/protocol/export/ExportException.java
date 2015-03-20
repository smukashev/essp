package com.bsbnb.usci.portlets.protocol.export;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ExportException extends Exception{
    public ExportException(String message) {
        super(message);
    }
    
    public ExportException(String message, Throwable throwable) {
        super(message,throwable);
    }
    
    public ExportException(Throwable throwable) {
        super(throwable);
    }
}

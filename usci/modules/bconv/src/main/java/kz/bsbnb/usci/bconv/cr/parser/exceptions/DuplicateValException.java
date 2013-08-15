package com.bsbnb.creditregistry.ws.exceptions;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class DuplicateValException extends SAXException {
    private static final long serialVersionUID = 1L;

    public DuplicateValException(String msg) {
        super(msg);
    }
}

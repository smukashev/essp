package kz.bsbnb.usci.receiver.exception;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class UnknownValException extends SAXException {
    public UnknownValException(String val) {
        super("Unknown value: " + val);
    }
}

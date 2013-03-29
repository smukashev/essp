package kz.bsbnb.usci.receiver.exception;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class NullValueException extends SAXException {
    public NullValueException(String tag) {
        super("Null value: " + tag);
    }
}

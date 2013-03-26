package kz.bsbnb.usci.batch.exception;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class TypeErrorException extends SAXException {
    public TypeErrorException(String type) {
        super("Unknown type: " + type);
    }
}

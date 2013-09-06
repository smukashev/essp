package kz.bsbnb.usci.bconv.cr.parser.exceptions;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class UnknownValException extends SAXException {
    private static final long serialVersionUID = 1L;

    public UnknownValException(String tagName, String value) {
        super("Unknown value: " + value + " for " + tagName);
    }
}

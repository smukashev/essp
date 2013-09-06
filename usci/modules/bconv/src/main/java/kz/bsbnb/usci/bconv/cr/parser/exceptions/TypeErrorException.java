package kz.bsbnb.usci.bconv.cr.parser.exceptions;

import org.xml.sax.SAXException;

/**
 * @author l.tulbassiyev
 */
public class TypeErrorException extends SAXException {
    private static final long serialVersionUID = 1L;

    public TypeErrorException(String tagName) {
        super("Type error: " + tagName);
    }
}

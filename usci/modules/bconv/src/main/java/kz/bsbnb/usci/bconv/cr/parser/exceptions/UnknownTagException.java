package kz.bsbnb.usci.bconv.cr.parser.exceptions;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class UnknownTagException extends SAXException {
    private static final long serialVersionUID = 1L;

    public UnknownTagException(String tagName) {
        super("Unknown tag: " + tagName);
    }
}

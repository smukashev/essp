package kz.bsbnb.usci.batch.exception;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class UnknownTagException extends SAXException
{
    public UnknownTagException(String tagName)
    {
        super("Unknown tag: " + tagName);
    }
}

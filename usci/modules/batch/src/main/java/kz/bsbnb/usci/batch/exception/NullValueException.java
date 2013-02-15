package kz.bsbnb.usci.batch.exception;

import org.xml.sax.SAXException;

/**
 * @author k.tulbassiyev
 */
public class NullValueException extends SAXException
{
    public NullValueException(String tag)
    {
        super("Null value: " + tag);
    }
}

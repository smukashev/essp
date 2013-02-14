package kz.bsbnb.usci.batch.parser;

import kz.bsbnb.usci.batch.common.Global;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author k.tulbassiyev
 */
public class CommonParser extends DefaultHandler
{
    protected XMLReader xmlReader;
    protected ContentHandler contentHandler;
    protected CharArrayWriter contents = new CharArrayWriter();
    protected DateFormat dateFormat = new SimpleDateFormat(Global.DATE_FORMAT);

    public CommonParser()
    {
        super();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String str = new String(ch, start, length).trim();
        contents.write(str, 0, str.length());
    }
}

package kz.bsbnb.usci.batch.parser;

import kz.bsbnb.usci.batch.common.Global;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.xml.sax.XMLReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author k.tulbassiyev
 */
public interface IParser
{
    public void parse() throws SAXException, IOException;
}

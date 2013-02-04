package kz.bsbnb.batch.parser.impl;

import kz.bsbnb.batch.parser.AbstractParser;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author k.tulbassiyev
 */
public class MainParser extends AbstractParser
{
    private InputSource inputSource;

    private Logger logger = Logger.getLogger(MainParser.class);

    public MainParser(byte[] xmlBytes)
    {
        ByteArrayInputStream bStream = new ByteArrayInputStream(xmlBytes);
        inputSource = new InputSource(bStream);
    }

    public void parse() throws SAXException, IOException
    {
        xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(this);
        xmlReader.parse(inputSource);
    }

    @Override
    public void startDocument() throws SAXException
    {
        logger.info("started");
    }

    @Override
    public void endDocument() throws SAXException
    {
        logger.info("finished");
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
    {
        logger.info(localName);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        logger.info(localName);
    }
}

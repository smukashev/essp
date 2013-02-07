package kz.bsbnb.usci.batch.parser.impl;

import kz.bsbnb.usci.batch.exception.UnknownTagException;
import kz.bsbnb.usci.batch.parser.AbstractParser;
import kz.bsbnb.usci.eav.model.BaseEntity;
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

    private EntityParser entityParser;

    private BaseEntity currentEntity;

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
        contents.reset();

        if(localName.equalsIgnoreCase("batch"))
        {
        }
        else if(localName.equalsIgnoreCase("entities"))
        {
        }
        else if(localName.equalsIgnoreCase("entity"))
        {
            currentEntity = new BaseEntity(attributes.getValue("class"));

            entityParser = new EntityParser();
            entityParser.parse(xmlReader, this, attributes.getValue("class"), currentEntity);

            xmlReader.setContentHandler(entityParser);
        }
        else
        {
            throw new UnknownTagException(localName);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        if(localName.equalsIgnoreCase("batch"))
        {

        }
        else if(localName.equalsIgnoreCase("entities"))
        {
            logger.info("entities");
        }
        else if(localName.equalsIgnoreCase("entity"))
        {

        }
        else
        {
            throw new UnknownTagException(localName);
        }
    }
}

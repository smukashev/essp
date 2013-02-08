package kz.bsbnb.usci.batch.parser.impl;

import kz.bsbnb.usci.batch.parser.AbstractParser;
import kz.bsbnb.usci.eav.model.BaseEntity;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author k.tulbassiyev
 */
// todo: complete
public class EntityParser extends AbstractParser
{
    private Logger logger = Logger.getLogger(EntityParser.class);

    private BaseEntity parentEntity;
    private BaseEntity childEntity;

    private String entityName;

    public void parse(XMLReader xmlReader, ContentHandler contentHandler, String entityName, BaseEntity parentEntity)
    {
        this.xmlReader = xmlReader;
        this.contentHandler = contentHandler;
        this.entityName = entityName;
        this.parentEntity = parentEntity;

        xmlReader.setContentHandler(this);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        contents.reset();

        if(localName.equalsIgnoreCase("entity"))
        {
            EntityParser entityParser = new EntityParser();

            entityParser.parse(xmlReader, this, attributes.getValue("class"),
                    childEntity);

            logger.info("aaa");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if(localName.equalsIgnoreCase("entity"))
        {

            xmlReader.setContentHandler(contentHandler);
        }
        else
        {
            // todo: put here Batch object
            parentEntity.set(localName, 0L, contents.toString());
        }
    }
}

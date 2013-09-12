package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.eav.model.json.StatusJModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author k.tulbassiyev
 */
@Component
public class MainParser extends BatchParser {
    private InfoParser infoParser = new InfoParser();

    @Autowired
    private PackageParser packageParser;

    @Autowired
    private PortfolioDataParser portfolioDataParser;

    private long index = 1;

    private static final Logger logger
            = Logger.getLogger(MainParser.class.getName());
    
    public void parse(InputStream in, Batch batch) throws SAXException, IOException, XMLStreamException
    {
        index = 1;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        xmlReader = inputFactory.createXMLEventReader(in);
        this.batch = batch;

        while(xmlReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlReader.next();

            if(event.isStartDocument()) {
                logger.info("start document");
            } else if(event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if(startElement(event, startElement, localName)) break;
            } else if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if(endElement(localName)) break;
            } else if(event.isEndDocument()) {
                logger.info("end document");
            } else {
                //logger.info(event.toString());
            }
        }
    }

    @Override
    public void parse(XMLEventReader xmlReader, Batch batch, long index) throws SAXException
    {
        this.xmlReader = xmlReader;
        this.batch = batch;

        while(xmlReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlReader.next();

            if(event.isStartDocument()) {
                logger.info("start document");
            } else if(event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if(startElement(event, startElement, localName)) break;
            } else if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if(endElement(localName)) break;
            } else if(event.isEndDocument()) {
                logger.info("end document");
            } else {
                //logger.info(event.toString());
            }
        }
    }

    public void endDocument() throws SAXException {
    }

    public void parseNextPackage() throws SAXException
    {
        System.out.println("Package #" + index++);
        packageParser.parse(xmlReader, batch, index);
        if (packageParser.hasMore()) {
            currentBaseEntity = packageParser.getCurrentBaseEntity();
            hasMore = true;
        } else {
            hasMore = false;
        }
    }

    public void skipNextPackage() throws SAXException
    {
        System.out.println("Package #" + index++ + " skipped.");

        while(xmlReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlReader.next();
            currentBaseEntity = null;

            if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if(localName.equals("packages")) {
                    hasMore = false;
                    return;
                } else if(localName.equals("package")) {
                    hasMore = true;
                    return;
                }
            }
        }
    }

    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("batch")) {
        } else if(localName.equals("info")) {
            infoParser.parse(xmlReader, batch, index);
        } else if(localName.equals("packages")) {
            parseNextPackage();
            return true;
        } else if(localName.equals("portfolio_data")) {
            portfolioDataParser.parse(xmlReader, batch, index);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("batch")) {
        } else if(localName.equals("info")) {
        } else if(localName.equals("packages")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

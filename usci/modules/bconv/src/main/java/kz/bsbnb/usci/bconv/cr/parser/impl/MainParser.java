package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.Batch;
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

    private static final Logger logger
            = Logger.getLogger(MainParser.class.getName());
    
    public void parse(InputStream in, Batch batch) throws SAXException, IOException, XMLStreamException
    {
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

                startElement(event, startElement, localName);
            } else if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                endElement(localName);
            } else if(event.isEndDocument()) {
                logger.info("end document");
            } else {
                //logger.info(event.toString());
            }
        }
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("batch")) {
        } else if(localName.equals("info")) {
            infoParser.parse(xmlReader, batch);
        } else if(localName.equals("packages")) {
            packageParser.parse(xmlReader, batch);
        } else if(localName.equals("portfolio_data")) {
            portfolioDataParser.parse(xmlReader, batch);
        } else {
            throw new UnknownTagException(localName);
        }
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

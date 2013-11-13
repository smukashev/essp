package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class MainParser extends BatchParser {

    @Autowired
    private InfoParser infoParser;

    @Autowired
    private PackageParser packageParser;

    @Autowired
    private PortfolioDataParser portfolioDataParser;

    private long index = 1;

    private static final Logger logger
            = Logger.getLogger(MainParser.class.getName());

    public long getIndex()
    {
        return index;
    }

    String currentTag = "";
    boolean hasMorePackages = true;
    boolean hasMorePortfolioData = true;

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
        System.out.println("Package #" + index++ + " " + currentTag);
        if (currentTag.equals("packages")) {
            packageParser.parse(xmlReader, batch, index);
            if (packageParser.hasMore()) {
                currentBaseEntity = packageParser.getCurrentBaseEntity();
                //my add
                //currentBaseEntity.put("actual_credit_count",new BaseValue(batch,index,43));
                currentBaseEntity.put("creditor", new BaseValue(batch,index,infoParser.getCurrentBaseEntity()));
                currentBaseEntity.put("account_date", infoParser.getAccountDate());
                currentBaseEntity.put("report_date", infoParser.getReportDate());
                currentBaseEntity.put("actual_credit_count", infoParser.getActualCreditCount());

                hasMorePackages = true;
            } else {
                hasMorePackages = false;
                if (hasMorePortfolioData) {
                    parse(xmlReader, batch, index--);
                }
            }
        } else if(currentTag.equals("portfolio_data")) {

            portfolioDataParser.setInfoParser(infoParser);
            portfolioDataParser.parse(xmlReader, batch, index);
            hasMorePortfolioData = portfolioDataParser.hasMore();
            if (hasMorePortfolioData) {
                currentBaseEntity = portfolioDataParser.getCurrentBaseEntity();

                //currentBaseEntity.put("creditor", new BaseValue(batch,index,infoParser.getCurrentBaseEntity()));
                //currentBaseEntity.put("account_date", infoParser.getAccountDate());
                //currentBaseEntity.put("report_date", infoParser.getReportDate());
                //currentBaseEntity.put("actual_credit_count", infoParser.getActualCreditCount());
            } else {
                if (hasMorePackages) {
                    parse(xmlReader, batch, index--);
                }
            }
        }

        hasMore = hasMorePortfolioData || hasMorePackages;
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
            //currentBaseEntity.put("creditor",new BaseValue(batch,index,infoParser.getCurrentBaseEntity()));
        } else if(localName.equals("packages")) {
            currentTag = localName;
            parseNextPackage();
            return true;
        } else if(localName.equals("portfolio_data")) {
            currentTag = localName;
            parseNextPackage();
            return true;
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("batch")) {
            hasMore = false;
            hasMorePackages = false;
            hasMorePortfolioData = false;
            return true;
        } else if(localName.equals("info")) {
        } else if(localName.equals("packages")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

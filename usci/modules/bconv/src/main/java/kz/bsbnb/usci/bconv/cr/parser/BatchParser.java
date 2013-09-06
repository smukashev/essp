package kz.bsbnb.usci.bconv.cr.parser;

import java.io.CharArrayWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import kz.bsbnb.usci.bconv.cr.parser.Const;
import kz.bsbnb.usci.bconv.cr.parser.model.CustomCharArrayWriter;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author k.tulbassiyev
 */

//branch: xparser
//commit cb5e3882c0d070f76fe3b4552aabf6e81ba9e7c3
//Author: kanat90@gmail.com <kanat90@gmail.com>
//Date:   Tue Aug 6 16:16:30 2013 +0600

public abstract class BatchParser {
    protected XMLEventReader xmlReader;
    protected DateFormat dateFormat = new SimpleDateFormat(Const.DATE_FORMAT);

    private Logger logger = Logger.getLogger(BatchParser.class);

    protected Batch batch;

    protected BaseEntity currentBaseEntity = null;

    protected boolean hasMore = false;

    @Autowired
    protected IMetaClassRepository metaClassRepository;
    
    public BatchParser() {
        super();
    }
    
    public Calendar convertDateToCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    public void parse(XMLEventReader xmlReader, Batch batch) throws SAXException
    {
        this.batch = batch;
        init();
        this.xmlReader = xmlReader;

        while(xmlReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlReader.next();

            if(event.isStartDocument()) {
                logger.info("start document");
            } else if(event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if (startElement(event, startElement, localName)) break;
            } else if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (endElement(localName)) break;
            } else if(event.isEndDocument()) {
                logger.info("end document");
            } else {
                //logger.info(event.toString());
            }
        }
    }

    public abstract boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException;
    public abstract boolean endElement(String localName) throws SAXException;
    public void init() {}

    public BaseEntity getCurrentBaseEntity()
    {
        return currentBaseEntity;
    }

    public boolean hasMore() {
        return hasMore;
    }
}

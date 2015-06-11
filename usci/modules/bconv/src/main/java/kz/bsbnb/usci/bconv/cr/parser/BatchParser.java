package kz.bsbnb.usci.bconv.cr.parser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author k.tulbassiyev
 */
public abstract class BatchParser {
    protected XMLEventReader xmlReader;
    protected DateFormat dateFormat = new SimpleDateFormat(Const.DATE_FORMAT);

    private Logger logger = Logger.getLogger(BatchParser.class);

    protected Batch batch;

    protected BaseEntity currentBaseEntity = null;

    protected boolean hasMore = false;

    protected long index;

    @Autowired
    protected IMetaClassRepository metaClassRepository;

    @Autowired
    protected IBaseEntityRepository baseEntityRepository;

    public BatchParser() {
        super();
        dateFormat.setLenient(false);
    }

    public Calendar convertDateToCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    public void parse(XMLEventReader xmlReader, Batch batch, long index) throws SAXException {
        this.batch = batch;
        init();
        this.xmlReader = xmlReader;
        this.index = index;

        while (xmlReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlReader.next();

            if (event.isStartDocument()) {
                logger.info("start document");
            } else if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if (startElement(event, startElement, localName)) break;
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (endElement(localName)) break;
            } else if (event.isEndDocument()) {
                logger.info("end document");
            } else {
                logger.info(event.toString());
            }
        }
    }

    public abstract boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException;

    public abstract boolean endElement(String localName) throws SAXException;
    public void init() {}

    public BaseEntity getCurrentBaseEntity() {
        return currentBaseEntity;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public long getIndex() {
        return index;
    }

}

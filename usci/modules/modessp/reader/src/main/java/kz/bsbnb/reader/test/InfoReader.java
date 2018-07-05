package kz.bsbnb.reader.test;

import kz.bsbnb.reader.base.BaseReader;
import kz.bsbnb.usci.eav.util.DataUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;


public final class InfoReader extends BaseReader {

    private Date reportDate;
    private long creditorId;
    private Long userId;

    public long getCreditorId() {
        return creditorId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public long getUserId(){
        return userId;
    }

    public InfoReader(XMLEventReader xmlEventReader) {
        super(xmlEventReader);
    }

    public InfoReader withExitTag(String localName) {
        exitTag = localName;
        return this;
    }

    public void read() throws XMLStreamException {
        while(xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if(xmlEvent.isStartElement()) {
                String localPart = xmlEvent.asStartElement().getName().getLocalPart();
                if(localPart.equals("creditor")) {
                    XMLEvent codeEvent = nextStartElement();
                    String code = xmlEventReader.nextEvent().asCharacters().getData();
                    if(code.equals("TTTTKA"))
                        creditorId = 4414L;
                } else if (localPart.equals("report_date")) {
                    String reportDateStr = xmlEventReader.nextEvent().asCharacters().getData();
                    reportDate = DataUtils.getDate(reportDateStr);
                } else if(localPart.equals("userId")) {
                    String userIdStr = xmlEventReader.nextEvent().asCharacters().getData();
                    userId = Long.valueOf(userIdStr);
                }
            } else if(xmlEvent.isEndElement()) {
                String localPart = xmlEvent.asEndElement().getName().getLocalPart();
                if(localPart.equals(exitTag))
                    return;
            }
        }

    }
}

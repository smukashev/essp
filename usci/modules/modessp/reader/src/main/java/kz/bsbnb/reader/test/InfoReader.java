package kz.bsbnb.reader.test;

import kz.bsbnb.reader.base.BaseReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class InfoReader extends BaseReader {

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
                    System.out.println(code);
                } else if (localPart.equals("report_date")) {
                    String reportDate = xmlEventReader.nextEvent().asCharacters().getData();
                    System.out.println(reportDate);
                } else if(localPart.equals("userId")) {
                    String userId = xmlEventReader.nextEvent().asCharacters().getData();
                    System.out.println(userId);
                }
            } else if(xmlEvent.isEndElement()) {
                String localPart = xmlEvent.asEndElement().getName().getLocalPart();
                if(localPart.equals(exitTag))
                    return;
            }
        }

    }
}

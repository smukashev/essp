package kz.bsbnb.reader.base;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class BaseReader {
    protected String exitTag;
    protected XMLEventReader xmlEventReader;
    protected MetaClass metaClass;

    public BaseReader(XMLEventReader xmlEventReader) {
        this.xmlEventReader = xmlEventReader;
    }

    public XMLEvent nextStartElement() throws XMLStreamException {
        while(xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if(xmlEvent.isStartElement())
                return xmlEvent;
        }

        throw new RuntimeException("No Start element");
    }
}

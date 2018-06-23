package kz.bsbnb.reader;

import kz.bsbnb.DataEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;

public class RootReader {
    XMLEventReader xmlEventReader;
    private MetaClass meta;

    public DataEntity read() throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartDocument()) {

            } else if (xmlEvent.isStartElement()) {
                String localName = xmlEvent.asStartElement().getName().getLocalPart();
                DataEntity entity = new DataEntity(meta);
                new AttributeReader(xmlEventReader)
                        .withRootEntity(entity)
                        .withExitTagName(localName)
                        .read();

                return entity;
            }
        }

        return null;
    }

    public RootReader withSource(InputStream source) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);
        xmlEventReader = inputFactory.createXMLEventReader(source);
        return this;
    }

    public void withMeta(MetaClass meta) {
        this.meta = meta;
    }
}

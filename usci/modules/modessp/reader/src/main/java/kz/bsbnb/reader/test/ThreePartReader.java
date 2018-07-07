package kz.bsbnb.reader.test;

import kz.bsbnb.DataEntity;
import kz.bsbnb.reader.RootReader;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;

public class ThreePartReader {
    XMLEventReader xmlEventReader;
    private MetaClass meta;

    public DataEntity read() throws XMLStreamException {
        DataEntity entity = null;
        InfoReader infoReader = null;

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartDocument()) {

            } else if (xmlEvent.isStartElement()) {
                String localName = xmlEvent.asStartElement().getName().getLocalPart();
                if(localName.equals("info")) {
                    infoReader = new InfoReader(xmlEventReader)
                            .withExitTag(localName);
                    infoReader.read();

                } else if(localName.equals("entities")) {
                    entity = new RootReader(xmlEventReader)
                            .withMeta(meta)
                            .withExitTag(localName)
                            .read();
                    entity.setCreditorId(infoReader.getCreditorId());
                    entity.setReportDate(infoReader.getReportDate());
                } else if(localName.equals("refs")) {
                    new RefsReader(xmlEventReader)
                            .withMeta(meta)
                            .withExitTag(localName)
                            .read();
                }
            }
        }

        return entity;
    }

    public ThreePartReader withSource(InputStream source) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);
        xmlEventReader = inputFactory.createXMLEventReader(source);
        return this;
    }

    public ThreePartReader withMeta(MetaClass meta) {
        this.meta = meta;
        return this;
    }

}

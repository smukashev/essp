package kz.bsbnb.reader;

import kz.bsbnb.DataEntity;
import kz.bsbnb.DataSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class SetReader {

    protected DataSet entitySet;
    protected XMLEventReader xmlEventReader;
    protected String exitTag;
    protected MetaClass metaClass;

    public SetReader(XMLEventReader xmlEventReader) {
        this.xmlEventReader = xmlEventReader;
    }

    public SetReader withRootSet(DataSet childSet) {
        this.entitySet = childSet;
        this.metaClass = childSet.getMetaClass();
        return this;
    }

    public SetReader withExitTagName(String localPart) {
        this.exitTag = localPart;
        return this;
    }

    public void read() throws XMLStreamException {
        while(xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if(xmlEvent.isStartElement()) {
                String tagName = xmlEvent.asStartElement().getName().getLocalPart();
                if("item".equals(tagName)) {
                    DataEntity entity = new DataEntity(metaClass);
                    new AttributeReader(xmlEventReader)
                            .withRootEntity(entity)
                            .withExitTagName("item")
                            .read();
                    entitySet.add(entity);
                }


            } else if (xmlEvent.isEndElement()) {
                String localPart = xmlEvent.asEndElement().getName().getLocalPart();
                if(exitTag.equals(localPart))
                    return;
            }
        }

    }
}

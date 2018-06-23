package kz.bsbnb.reader;

import kz.bsbnb.DataComplexValue;
import kz.bsbnb.DataEntity;
import kz.bsbnb.DataValueCreator;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class AttributeReader {

    private final XMLEventReader xmlEventReader;
    protected DataEntity entity;
    protected String exitTag;
    protected MetaClass metaClass;

    public AttributeReader(XMLEventReader xmlEventReader) {
        this.xmlEventReader = xmlEventReader;
    }

    public AttributeReader withRootEntity(DataEntity entity) {
        this.entity = entity;
        this.metaClass = entity.getMeta();
        return this;
    }

    public AttributeReader withExitTagName(String localName) {
        this.exitTag = localName;
        return this;
    }

    public void read() throws XMLStreamException {
        while(xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if(xmlEvent.isStartElement()) {
                String localPart = xmlEvent.asStartElement().getName().getLocalPart();
                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(localPart);
                IMetaType metaType = metaAttribute.getMetaType();
                if(!metaType.isComplex()) {
                    XMLEvent valueReader = xmlEventReader.nextEvent();
                    String doubleValue = valueReader.asCharacters().getData();
                    entity.setDataValue(localPart, DataValueCreator.getValue(((MetaValue) metaType).getTypeCode(), doubleValue));
                } else {
                    if(!metaType.isSet()) {
                        DataEntity childEntity = new DataEntity(((MetaClass) metaType));
                        entity.setDataValue(localPart, new DataComplexValue(childEntity));
                        new AttributeReader(xmlEventReader)
                                .withRootEntity(childEntity)
                                .withExitTagName(localPart)
                                .read();
                    } else {
                        throw new RuntimeException("not implemented");
                    }
                }
            } else if(xmlEvent.isEndElement()) {
                String endTag = xmlEvent.asEndElement().getName().getLocalPart();
                if(endTag.equals(exitTag))
                    return;
            }
        }
    }
}

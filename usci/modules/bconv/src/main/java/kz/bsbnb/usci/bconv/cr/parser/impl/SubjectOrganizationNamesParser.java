package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class SubjectOrganizationNamesParser extends BatchParser {
    public SubjectOrganizationNamesParser() {
        super();
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("names")) {
        } else if (localName.equals("name")) {
            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("organization_name"),
                    batch.getRepDate());
            currentBaseEntity.put("lang", new BaseEntityStringValue(-1, batch, index,
                    event.asStartElement().getAttributeByName(new QName("lang")).getValue()));
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("name", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("names")) {
            hasMore = false;
            return true;
        } else if (localName.equals("name")) {
            hasMore = true;
            return true;
        } else {
            throw new UnknownTagException(localName);
        }
    }
}

package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class SubjectOrganizationHeadNamesParser extends BatchParser {
    public SubjectOrganizationHeadNamesParser() {
        super();
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if (localName.equals("names")) {
        } else if (localName.equals("name")) {
            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("person_name"), batch.getRepDate(), creditorId);
        } else if (localName.equals("firstname")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("firstname", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else if (localName.equals("lastname")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("lastname", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else if (localName.equals("middlename")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("middlename", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
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
        } else if (localName.equals("firstname")) {
        } else if (localName.equals("lastname")) {
        } else if (localName.equals("middlename")) {
        } else {
            throw new UnknownTagException(localName);
        }
        return false;
    }
}

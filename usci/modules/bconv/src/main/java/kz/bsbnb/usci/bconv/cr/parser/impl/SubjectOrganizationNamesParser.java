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
        switch (localName) {
            case "names":
                break;
            case "name":
                currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("organization_name"),
                        batch.getRepDate(), creditorId);

                currentBaseEntity.put("lang",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asStartElement().getAttributeByName(new QName("lang")).getValue(), false, true));

                event = (XMLEvent) xmlReader.next();

                currentBaseEntity.put("name",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asCharacters().getData(), false, true));
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "names":
                hasMore = false;
                return true;
            case "name":
                hasMore = true;
                return true;
            default:
                throw new UnknownTagException(localName);
        }
    }
}

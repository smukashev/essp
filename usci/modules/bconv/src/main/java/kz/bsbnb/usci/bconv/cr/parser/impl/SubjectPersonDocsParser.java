package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class SubjectPersonDocsParser extends BatchParser {
    public SubjectPersonDocsParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("document"), batch.getRepDate(), creditorId);
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        switch (localName) {
            case "docs":
                break;
            case "doc":
                BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), batch.getRepDate(), creditorId);

                docType.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asStartElement().getAttributeByName(new QName("doc_type")).getValue(), false, true));

                currentBaseEntity.put("doc_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), docType, false, true));
                break;
            case "name":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("name",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));
                break;
            case "no":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("no",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "docs":
                hasMore = false;
                return true;
            case "doc":
                hasMore = true;
                return true;
            case "name":
                break;
            case "no":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

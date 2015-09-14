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
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("document"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if (localName.equals("docs")) {
        } else if (localName.equals("doc")) {
            BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), batch.getRepDate());

            docType.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asStartElement().getAttributeByName(new QName("doc_type")).getValue(), false, true));

            currentBaseEntity.put("doc_type", new BaseEntityComplexValue(0, -1, batch.getRepDate(), docType, false, true));
        } else if (localName.equals("name")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("name", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else if (localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("no", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("docs")) {
            hasMore = false;
            return true;
        } else if (localName.equals("doc")) {
            hasMore = true;
            return true;
        } else if (localName.equals("name")) {
        } else if (localName.equals("no")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class SubjectCreditorParser extends BatchParser {
    public SubjectCreditorParser() {
        super();
    }

    private BaseSet docs;
    private BaseEntity currentDoc;

    private BaseEntity creditorInfo;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("subject"), batch.getRepDate(), creditorId);
        creditorInfo = new BaseEntity(metaClassRepository.getMetaClass("creditor_info"), batch.getRepDate(), creditorId);
        docs = null;
        currentDoc = null;
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "creditor":
                break;
            case "code":
                event = (XMLEvent) xmlReader.next();
                creditorInfo.put("code", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                        event.asCharacters().getData(), false, true));
                break;
            case "docs":
                docs = new BaseSet(metaClassRepository.getMetaClass("document"));
                break;
            case "doc":
                currentDoc = new BaseEntity(metaClassRepository.getMetaClass("document"),
                        batch.getRepDate(), creditorId);

                BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"),
                        batch.getRepDate(), creditorId);

                docType.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asStartElement().getAttributeByName(new QName("doc_type")).getValue(), false, true));

                currentDoc.put("doc_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), docType, false, true));
                break;
            case "name":
                event = (XMLEvent) xmlReader.next();

                currentDoc.put("name",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));
                break;
            case "no":
                event = (XMLEvent) xmlReader.next();

                currentDoc.put("no",
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
            case "creditor":
                currentBaseEntity.put("creditor_info",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), creditorInfo, false, true));

                currentBaseEntity.put("is_person",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), false, true, false));

                currentBaseEntity.put("is_organization",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), false, true, false));

                currentBaseEntity.put("is_creditor",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), true, true, false));
                return true;
            case "code":
                break;
            case "docs":
                currentBaseEntity.put("docs",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), docs, false, true));
                break;
            case "doc":
                docs.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentDoc, false, true));
                break;
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

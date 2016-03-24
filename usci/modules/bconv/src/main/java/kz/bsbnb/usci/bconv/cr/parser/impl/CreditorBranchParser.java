package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetComplexValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class CreditorBranchParser extends BatchParser {
    private BaseEntity currentDoc = null;
    private BaseSet currentDocSet = null;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_creditor_branch"),
                batch.getRepDate(), creditorId);
        currentDoc = null;
        currentDocSet = null;
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "creditor_branch":
                break;
            case "code":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));
                break;
            case "docs":
                currentDocSet = new BaseSet(metaClassRepository.getMetaClass("document"), creditorId);
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
            case "creditor_branch":
                return true;
            case "code":
                break;
            case "docs":
                currentBaseEntity.put("docs",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), currentDocSet, false, true));
                break;
            case "doc":
                currentDocSet.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentDoc, false, true));
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

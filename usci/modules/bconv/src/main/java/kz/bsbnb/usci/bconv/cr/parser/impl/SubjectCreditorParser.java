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
public class SubjectCreditorParser extends BatchParser {
    public SubjectCreditorParser() {
        super();
    }

    private BaseSet docs;
    private BaseEntity currentDoc;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("creditor"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("creditor")) {
        } else if (localName.equals("code")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("code", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
        } else if (localName.equals("docs")) {
            docs = new BaseSet(metaClassRepository.getMetaClass("document"));
        } else if (localName.equals("doc")) {
            currentDoc = new BaseEntity(metaClassRepository.getMetaClass("document"), batch.getRepDate());
            BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), batch.getRepDate());
            docType.put("code", new BaseEntityStringValue(-1, batch, index,
                    event.asStartElement().getAttributeByName(new QName("doc_type")).getValue()));
            currentDoc.put("doc_type", new BaseEntityComplexValue(-1, batch, index, docType));
        } else if (localName.equals("name")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("name", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
        } else if (localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("no", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("creditor")) {
            return true;
        } else if (localName.equals("code")) {
        } else if (localName.equals("docs")) {
            currentBaseEntity.put("docs", new BaseEntityComplexSet(-1, batch, index, docs));
        } else if (localName.equals("doc")) {
            docs.put(new BaseSetComplexValue(-1, batch, index, currentDoc));
        } else if (localName.equals("name")) {
        } else if (localName.equals("no")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

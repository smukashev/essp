package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
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
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("creditor"),batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("creditor")) {
        } else if(localName.equals("code")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("docs")) {
            //docs = new CtCreditor.Docs();
            docs = new BaseSet(metaClassRepository.getMetaClass("document"));
        } else if(localName.equals("doc")) {
             //ctDoc = new CtDoc();
            //ctDoc.setDocType(attributes.getValue("doc_type"));
            currentDoc = new BaseEntity(metaClassRepository.getMetaClass("document"),batch.getRepDate());
            BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), batch.getRepDate());
            docType.put("code",new BaseEntityIntegerValue(batch,index,
                    new Integer(event.asStartElement().getAttributeByName(new QName("doc_type")).getValue())));
            currentDoc.put("doc_type",new BaseEntityComplexValue(batch,index,docType));
            /*currentDoc.put("doc_type",new BaseValue(batch,index,
                    event.asStartElement().getAttributeByName(new QName("doc_type")).getValue())
            );*/
        } else if(localName.equals("name")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("name",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("no",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("creditor")) {
            //currentSubject.setCreditor(ctCreditor);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("code")) {
            //ctCreditor.setCode(contents.toString());
        } else if(localName.equals("docs")) {
            //ctCreditor.setDocs(docs);
            currentBaseEntity.put("docs",new BaseEntityComplexSet(batch,index,docs));
        } else if(localName.equals("doc")) {
            //docs.getDoc().add(ctDoc);
            docs.put(new BaseSetComplexValue(batch,index,currentDoc));
        } else if(localName.equals("name")) {
            //ctDoc.setName(contents.toString());
        } else if(localName.equals("no")) {
            //ctDoc.setNo(contents.toString());
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
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
public class CreditorBranchParser extends BatchParser {

    BaseEntity currentDoc = null;
    BaseSet currentDocSet = null;

    @Override
    public void init()
    {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("creditor_branch"), new Date());
    }

    @Override
    public void startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("creditor_branch")) {
        } else if(localName.equals("code")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("code", new BaseValue(batch, 0, event.asCharacters().getData()));
        } else if(localName.equals("docs")) {
            //ctCreditorDocs = new CtCreditor.Docs();
            currentDocSet = new BaseSet(metaClassRepository.getMetaClass("doc"));
        } else if(localName.equals("doc")) {
            //currentDoc = new CtDoc();
            //currentDoc.setDocType(attributes.getValue("doc_type"));
            currentDoc = new BaseEntity(metaClassRepository.getMetaClass("doc"), new Date());

            currentDoc.put("doc_type", new BaseValue(batch, 0,
                    event.asStartElement().getAttributeByName(new QName("doc_type")).getValue()));
        } else if(localName.equals("name")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("name", new BaseValue(batch, 0, event.asCharacters().getData()));
        } else if(localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("no", new BaseValue(batch, 0, event.asCharacters().getData()));
        } else {
            throw new UnknownTagException(localName);
        }
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("creditor_branch")) {
            //ctCredit.setCreditorBranch(ctCreditor);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("code")) {
            //ctCreditor.setCode(contents.toString());
        } else if(localName.equals("docs")) {
            //ctCreditor.setDocs(ctCreditorDocs);
            currentBaseEntity.put("docs", new BaseValue(batch, 0, currentDocSet));
        } else if(localName.equals("doc")) {
            //ctCreditorDocs.getDoc().add(currentDoc);
            currentDocSet.put(new BaseValue(batch, 0, currentDoc));
        } else if(localName.equals("name")) {
            //currentDoc.setName(contents.toString());
        } else if(localName.equals("no")) {
            //currentDoc.setNo(contents.toString());
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

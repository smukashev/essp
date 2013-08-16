package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author k.tulbassiyev
 */
@Component
public class SubjectCreditorParser extends BatchParser {
    public SubjectCreditorParser() {
        super();        
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("creditor")) {
        } else if(localName.equals("code")) {
        } else if(localName.equals("docs")) {
            //docs = new CtCreditor.Docs();
        } else if(localName.equals("doc")) {
            //ctDoc = new CtDoc();
            //ctDoc.setDocType(attributes.getValue("doc_type"));
        } else if(localName.equals("name")) {
        } else if(localName.equals("no")) {
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
        } else if(localName.equals("doc")) {
            //docs.getDoc().add(ctDoc);
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

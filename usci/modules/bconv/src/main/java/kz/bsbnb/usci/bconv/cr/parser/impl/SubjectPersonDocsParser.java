package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
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
public class SubjectPersonDocsParser extends BatchParser {
    public SubjectPersonDocsParser() {
        super();
    }

    @Override
    public void init()
    {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("doc1"), new Date());
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("docs")) {
        } else if(localName.equals("doc")) {
            //ctDoc = new CtDoc();
            //ctDoc.setDocType(attributes.getValue("doc_type"));
            BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date());

            docType.put("code", new BaseValue(batch, index,
                    new Integer(event.asStartElement().getAttributeByName(new QName("doc_type")).getValue())));

            currentBaseEntity.put("doc_type", new BaseValue(batch, index,
                    docType));
        } else if(localName.equals("name")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("name", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("no", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("docs")) {
            /*if(duplicateCtDocList.size() > 0) {
                String str = "Duplicate type: ";

                for(CtDoc cd : duplicateCtDocList)
                    str += "DocType: " + cd.getDocType() + ", DocNo: " + cd.getNo() + "\n";

                protocolBean.writeMessageToProtocol(inputInfo, null, MessageCode.SUBJECT_DUPLICATE_DOCS,
                        ProtocolType.SUBJECT, MessageType.CRITICAL_ERROR, null, null, str);
            }
            
            ctPerson.setDocs(docs);*/
            //xmlReader.setContentHandler(contentHandler);
            hasMore = false;
            return true;
        } else if(localName.equals("doc")) {
            //docs.getDoc().add(ctDoc);
            hasMore = true;
            return true;
        } else if(localName.equals("name")) {
            /*if(ctDoc.getDocType().equals(DocTypes.OTHER.getCode()))
                ctDoc.setName(contents.toString());*/
        } else if(localName.equals("no")) {
            //ctDoc.setNo(contents.toString());
            
            /*boolean isDuplicateDebtor = false;
            
            for(CtDoc ct : tmpList) {
                if(!ctDoc.getDocType().equals(DocTypes.OTHER.getCode()) &&
                    ctDoc.getDocType().equals(ct.getDocType())) {
                    duplicateCtDocList.add(ct);
                    isDuplicateDebtor = true;
                }
            }
            
            if(!isDuplicateDebtor)
                tmpList.add(ctDoc);*/
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

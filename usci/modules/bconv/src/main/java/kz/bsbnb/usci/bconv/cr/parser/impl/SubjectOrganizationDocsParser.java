package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.util.ArrayList;
import java.util.List;

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
public class SubjectOrganizationDocsParser extends BatchParser {
    public SubjectOrganizationDocsParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("docs")) {
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
        if(localName.equals("docs")) {
            /*if(duplicateCtDocList.size() > 0) {
                String str = "Duplicate type: ";

                for(CtDoc cd : duplicateCtDocList)
                    str += "DocType: " + cd.getDocType() + ", DocNo: " + cd.getNo() + "\n";
                
                protocolBean.writeMessageToProtocol(inputInfo, null, MessageCode.SUBJECT_DUPLICATE_DOCS,
                        ProtocolType.SUBJECT, MessageType.CRITICAL_ERROR, null, null, str);


            }
            
            ctOrganization.setDocs(docs);*/
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("doc")) {
            //docs.getDoc().add(ctDoc);
        } else if(localName.equals("name")) {
            /*if(ctDoc.getDocType().equals(DocTypes.OTHER.getCode()))
                ctDoc.setName(contents.toString());*/
        } else if(localName.equals("no")) {
            /*ctDoc.setNo(contents.toString());
            
            boolean isDuplicateDebtor = false;
            
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

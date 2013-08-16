package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SubjectOrganizationHeadParser extends BatchParser {
    @Autowired
    private SubjectOrganizationHeadNamesParser subjectOrganizationHeadNamesParser;

    @Autowired
    private SubjectOrganizationHeadDocsParser subjectOrganizationHeadDocsParser;
        
    public SubjectOrganizationHeadParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("head")) {
        } else if(localName.equals("names")) {
            subjectOrganizationHeadNamesParser.parse(xmlReader, batch);
        } else if(localName.equals("docs")) {
            subjectOrganizationHeadDocsParser.parse(xmlReader, batch);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("head")) {
            //ctOrganization.setHead(ctPersonHead);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("names")) {
        } else if(localName.equals("docs")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

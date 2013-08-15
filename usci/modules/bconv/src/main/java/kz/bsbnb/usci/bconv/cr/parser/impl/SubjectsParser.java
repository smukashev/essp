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
public class SubjectsParser extends BatchParser {
    @Autowired
    private SubjectPersonParser subjectPersonParser;

    @Autowired
    private SubjectOrganizationParser subjectOrganizationParser;

    @Autowired
    private SubjectCreditorParser subjectCreditorParser;
    
    public SubjectsParser() {
        super();
    }
    
    @Override
    public void startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("subjects")) {
        } else if(localName.equals("subject")) {
            //currentSubject = new CtSubject();
        } else if(localName.equals("person")) {
            subjectPersonParser.parse(xmlReader, batch);
        } else if(localName.equals("organization")) {
            subjectOrganizationParser.parse(xmlReader, batch);
        } else if(localName.equals("creditor")) {
            subjectCreditorParser.parse(xmlReader, batch);
        } else {
            throw new UnknownTagException(localName);
        }
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("subjects")) {
            //currentPackage.setSubjects(subjects);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("subject")) {
            //subjects.setSubject(currentSubject);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }    
}

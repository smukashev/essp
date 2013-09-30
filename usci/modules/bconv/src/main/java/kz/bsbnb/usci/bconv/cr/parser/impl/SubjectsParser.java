package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
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
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("subjects")) {
        } else if(localName.equals("subject")) {
            //currentSubject = new CtSubject();
        } else if(localName.equals("person")) {
            subjectPersonParser.parse(xmlReader, batch, index);
            currentBaseEntity = subjectPersonParser.getCurrentBaseEntity();
        } else if(localName.equals("organization")) {
            subjectOrganizationParser.parse(xmlReader, batch, index);
        } else if(localName.equals("creditor")) {
            subjectCreditorParser.parse(xmlReader, batch, index);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("subjects")) {
            //currentPackage.setSubjects(subjects);
            //xmlReader.setContentHandler(contentHandler);
            hasMore = false;
        } else if(localName.equals("subject")) {
            //subjects.setSubject(currentSubject);
            hasMore = true;
        } else {
            throw new UnknownTagException(localName);
        }

        return true;
    }    
}

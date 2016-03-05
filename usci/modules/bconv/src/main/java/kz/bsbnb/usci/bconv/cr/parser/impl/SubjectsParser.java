package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
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
        switch (localName) {
            case "subjects":
                break;
            case "subject":
                break;
            case "person":
                subjectPersonParser.parse(xmlReader, batch, index, creditorId);
                currentBaseEntity = subjectPersonParser.getCurrentBaseEntity();
                break;
            case "organization":
                subjectOrganizationParser.parse(xmlReader, batch, index, creditorId);
                currentBaseEntity = subjectOrganizationParser.getCurrentBaseEntity();
                break;
            case "creditor":
                subjectCreditorParser.parse(xmlReader, batch, index, creditorId);
                currentBaseEntity = subjectCreditorParser.getCurrentBaseEntity();
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "subjects":
                hasMore = false;
                break;
            case "subject":
                hasMore = true;
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return true;
    }    
}

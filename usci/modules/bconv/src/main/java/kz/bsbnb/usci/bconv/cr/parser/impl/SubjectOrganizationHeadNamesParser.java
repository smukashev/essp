package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
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
public class SubjectOrganizationHeadNamesParser extends BatchParser {
    public SubjectOrganizationHeadNamesParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("names")) {
        } else if(localName.equals("name")) {
            /*name = new CtPersonBase.Names.Name();
            if(attributes.getValue("lang") != null && attributes.getValue("lang").equals("RU")) {
                name.setLang(StLang.RU);
            } else if(attributes.getValue("lang").equals("EN")) {
                name.setLang(StLang.EN);
            } else if(attributes.getValue("lang").equals("KZ")) {
                name.setLang(StLang.KZ);
            } else {
                throw new UnknownValException(localName, attributes.getValue("lang"));
            } */
        } else if(localName.equals("firstname")) {
        } else if(localName.equals("lastname")) {
        } else if(localName.equals("middlename")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("names")) {
            //ctPersonHead.setNames(names);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("name")) {
            //names.getName().add(name);
        } else if(localName.equals("firstname")) {
            //name.setFirstname(contents.toString());
        } else if(localName.equals("lastname")) {
            //name.setLastname(contents.toString());
        } else if(localName.equals("middlename")) {
            //name.setMiddlename(contents.toString());
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

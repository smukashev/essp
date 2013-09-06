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
public class SubjectOrganizationNamesParser extends BatchParser {
    public SubjectOrganizationNamesParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("names")) {
        } else if(localName.equals("name")) {
            /*name = new CtOrganization.Names.Name();

            if(attributes.getValue("lang") != null && attributes.getValue("lang").equals("RU")) {
                name.setLang(StLang.RU);
            } else if(attributes.getValue("lang").equals("EN")) {
                name.setLang(StLang.EN);
            } else if(attributes.getValue("lang").equals("KZ")) {
                name.setLang(StLang.KZ);
            } else {
                throw new UnknownValException(localName, attributes.getValue("lang"));
            }*/
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }   
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("names")) {
            //ctOrganization.setNames(names);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("name")) {
            //name.setValue(contents.toString());
            //names.getName().add(name);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
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
            //my code
            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("name2"),new Date());


        } else if(localName.equals("firstname")) {
             event = (XMLEvent) xmlReader.next();
             currentBaseEntity.put("firstname", new BaseValue(batch,index,  event.asCharacters().getData()));
        } else if(localName.equals("lastname")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("lastname", new BaseValue(batch,index,  event.asCharacters().getData()));
        } else if(localName.equals("middlename")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("middlename", new BaseValue(batch,index,  event.asCharacters().getData()));
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
            hasMore = false;
            return true;
        } else if(localName.equals("name")) {
            hasMore = true;
            return true;
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

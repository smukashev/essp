package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
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
            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("organization_name"),batch.getRepDate());
            currentBaseEntity.put("lang",new BaseEntityStringValue(batch,index,
                    event.asStartElement().getAttributeByName(new QName("lang")).getValue()));
            event = (XMLEvent) xmlReader.next();
            //BaseSet baseSet = new BaseSet(new MetaValue(DataTypes.STRING));
            //baseSet.put(new BaseValue(batch,index,event.asCharacters().getData()));
            // TODO check attribute name
            currentBaseEntity.put("name", new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
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
            hasMore = false;
            return true;
        } else if(localName.equals("name")) {
            //name.setValue(contents.toString());
            //names.getName().add(name);
            hasMore = true;
            return true;
        } else {
            throw new UnknownTagException(localName);
        }

        //return false;
    }
}

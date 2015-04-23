package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

/**
 * Created by maksat on 4/23/15.
 */
@Component
@Scope("prototype")
public class ChangeRemainsDiscountedValueParser extends BatchParser {

    public ChangeRemainsDiscountedValueParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains_discounted_value"),new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("discounted_value")) {
        } else if (localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("value", new BaseValue(batch, index, new Double(event.asCharacters().getData())));
        } else {
            throw new UnknownTagException(localName);
        }
        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("discounted_value")) {
            return true;
        } else if (localName.equals("value")) {
        } else {
            throw new UnknownTagException(localName);
        }
        return false;
    }
}

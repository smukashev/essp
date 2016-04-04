package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class ChangeRemainsDiscountedValueParser extends BatchParser {

    public ChangeRemainsDiscountedValueParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains_discounted_value"),
                batch.getRepDate(), creditorId);
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "discounted_value":
                break;
            case "value":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("value", new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(),
                        new Double(event.asCharacters().getData()), false, true));
                break;
            default:
                throw new UnknownTagException(localName);
        }
        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "discounted_value":
                return true;
            case "value":
                break;
            default:
                throw new UnknownTagException(localName);
        }
        return false;
    }
}

package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class ChangeRemainsLimitParser extends BatchParser {
    public ChangeRemainsLimitParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains_limit"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("limit")) {
        } else if (localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("value", new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true));
        } else if (localName.equals("value_currency")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("value_currency", new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true));
        } else if (localName.equals("balance_account")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity baseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),
                    batch.getRepDate());
            baseEntity.put("no_", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(),
                    false, true));
            currentBaseEntity.put("balance_account", new BaseEntityComplexValue(0, -1, batch.getRepDate(), baseEntity,
                    false, true));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("limit")) {
            return true;
        } else if (localName.equals("value")) {
        } else if (localName.equals("value_currency")) {
        } else if (localName.equals("balance_account")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

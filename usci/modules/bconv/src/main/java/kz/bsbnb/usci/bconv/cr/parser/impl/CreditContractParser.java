package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;

@Component
@Scope("prototype")
public class CreditContractParser extends BatchParser {
    public CreditContractParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("contract"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        try {
            if (localName.equals("contract")) {
            } else if (localName.equals("no")) {
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("no", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                        event.asCharacters().getData(), false, true));
            } else if (localName.equals("date")) {
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("date", new BaseEntityDateValue(0, -1, batch.getRepDate(),
                        dateFormat.parse(event.asCharacters().getData()), false, true));
            } else {
                throw new UnknownTagException(localName);
            }
        } catch (ParseException parseException) {
            throw new UnknownValException(localName, event.asCharacters().getData());
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("contract")) {
            return true;
        } else if (localName.equals("no")) {
        } else if (localName.equals("date")) {
        } else {
            throw new UnknownTagException(localName);
        }
        return false;
    }
}

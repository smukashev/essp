package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
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
public class PrimaryContractParser extends BatchParser {
    public PrimaryContractParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("primary_contract"),
                batch.getRepDate(), creditorId);
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "primary_contract":
                break;
            case "no":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("no", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                        event.asCharacters().getData(), false, true));
                break;
            case "date":
                event = (XMLEvent) xmlReader.next();
                String dateRaw = event.asCharacters().getData();
                try {
                    currentBaseEntity.put("date", new BaseEntityDateValue(0, creditorId, batch.getRepDate(),
                            dateFormat.parse(dateRaw), false, true));
                } catch (ParseException e) {
                    currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            default:
                currentBaseEntity.addValidationError("Нет такого тега: " + localName);
                break;
        }


        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "primary_contract":
                return true;
            case "no":
                break;
            case "date":
                break;
            default:
                throw new UnknownTagException(localName);
        }
        return false;
    }
}

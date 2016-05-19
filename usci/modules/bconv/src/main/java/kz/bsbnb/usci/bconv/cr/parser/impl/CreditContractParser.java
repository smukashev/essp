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
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("contract"), batch.getRepDate(), creditorId);
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        try {
            switch (localName) {
                case "contract":
                    break;
                case "no":
                    event = (XMLEvent) xmlReader.next();
                    currentBaseEntity.put("no",
                            new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));
                    break;
                case "date":
                    event = (XMLEvent) xmlReader.next();
                    currentBaseEntity.put("date",
                            new BaseEntityDateValue(0, creditorId, batch.getRepDate(),
                                    dateFormat.parse(trim(event.asCharacters().getData())), false, true));
                    break;
                default:
                    throw new UnknownTagException(localName);
            }
        } catch (ParseException parseException) {
            throw new UnknownValException(localName, trim(event.asCharacters().getData()));
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "contract":
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

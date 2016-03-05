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
public class PledgesParser extends BatchParser {
    public PledgesParser() {
        super();
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "pledges":
                break;
            case "pledge":
                currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("pledge"),
                        batch.getRepDate(), creditorId);
                break;
            case "pledge_type":
                event = (XMLEvent) xmlReader.next();

                BaseEntity pledgeType = new BaseEntity(metaClassRepository.getMetaClass("ref_pledge_type"),
                        batch.getRepDate(), creditorId);

                pledgeType.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                currentBaseEntity.put("pledge_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), pledgeType, false, true));
                break;
            case "contract":
                break;
            case "no":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("contract",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));
                break;
            case "value":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("value",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(),
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
            case "pledges":
                hasMore = false;
                return true;
            case "pledge":
                hasMore = true;
                return true;
            case "pledge_type":
                break;
            case "contract":
                break;
            case "no":
                break;
            case "value":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

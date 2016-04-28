package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class ChangeRemainsLimitParser extends BatchParser {
    private MetaClass refBalanceAccountMeta;

    public ChangeRemainsLimitParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains_limit"), batch.getRepDate(), creditorId);
        refBalanceAccountMeta = metaClassRepository.getMetaClass("ref_balance_account");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "limit":
                break;
            case "value":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("value",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(),
                                new Double(event.asCharacters().getData()), false, true));
                break;
            case "value_currency":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("value_currency",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(),
                                new Double(event.asCharacters().getData()), false, true));
                break;
            case "balance_account":
                event = (XMLEvent) xmlReader.next();

                BaseEntity baseEntity = new BaseEntity(refBalanceAccountMeta, batch.getRepDate(), creditorId);

                baseEntity.put("no_",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                currentBaseEntity.put("balance_account",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), baseEntity, false, true));
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "limit":
                return true;
            case "value":
                break;
            case "value_currency":
                break;
            case "balance_account":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

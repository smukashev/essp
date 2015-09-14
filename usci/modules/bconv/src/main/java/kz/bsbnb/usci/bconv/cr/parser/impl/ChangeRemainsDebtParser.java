package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;

@Component
@Scope("prototype")
public class ChangeRemainsDebtParser extends BatchParser {
    private String debtWay;

    public ChangeRemainsDebtParser() {
        super();
    }

    private BaseEntity fieldCurrent;
    private BaseEntity fieldPastDue;
    private BaseEntity fieldWriteOf;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains_debt"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("debt")) {
        } else if (localName.equals("current")) {
            fieldCurrent = new BaseEntity(metaClassRepository.getMetaClass("remains_debt_current"),
                    batch.getRepDate());
            debtWay = localName;
        } else if (localName.equals("pastdue")) {
            fieldPastDue = new BaseEntity(metaClassRepository.getMetaClass("remains_debt_pastdue"),
                    batch.getRepDate());
            debtWay = localName;
        } else if (localName.equals("write_off")) {
            fieldWriteOf = new BaseEntity(metaClassRepository.getMetaClass("remains_debt_write_off"),
                    batch.getRepDate());
            debtWay = localName;
        } else if (localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true);
            if (debtWay.equals("current")) {
                fieldCurrent.put("value", baseValue);
            } else if (debtWay.equals("pastdue")) {
                fieldPastDue.put("value", baseValue);
            } else if (debtWay.equals("write_off")) {
                fieldWriteOf.put("value", baseValue);
            }
        } else if (localName.equals("value_currency")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true);
            if (debtWay.equals("current")) {
                fieldCurrent.put("value_currency", baseValue);
            } else if (debtWay.equals("pastdue")) {
                fieldPastDue.put("value_currency", baseValue);
            } else if (debtWay.equals("write_off")) {
                fieldWriteOf.put("value_currency", baseValue);
            }
        } else if (localName.equals("balance_account")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity baseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),
                    batch.getRepDate());
            baseEntity.put("no_", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(), false, true));
            BaseValue baseValue = new BaseEntityComplexValue(0, -1, batch.getRepDate(), baseEntity, false, true);
            if (debtWay.equals("current")) {
                fieldCurrent.put("balance_account", new BaseEntityComplexValue(0, -1, batch.getRepDate(), baseEntity, false, true));
            } else if (debtWay.equals("pastdue")) {
                fieldPastDue.put("balance_account", baseValue);
            } else if (debtWay.equals("write_off")) {
                fieldWriteOf.put("balance_account", baseValue);
            }
        } else if (localName.equals("open_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                fieldPastDue.put("open_date", new BaseEntityDateValue(0, -1, batch.getRepDate(), dateFormat.parse(dateRaw), false, true));
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if (localName.equals("close_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                fieldPastDue.put("close_date", new BaseEntityDateValue(0, -1, batch.getRepDate(), dateFormat.parse(dateRaw), false, true));
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if (localName.equals("date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                fieldWriteOf.put("date", new BaseEntityDateValue(0, -1, batch.getRepDate(), dateFormat.parse(dateRaw), false, true));
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("debt")) {
            return true;
        } else if (localName.equals("current")) {
            currentBaseEntity.put("current", new BaseEntityComplexValue(0, -1, batch.getRepDate(), fieldCurrent, false, true));
        } else if (localName.equals("pastdue")) {
            currentBaseEntity.put("pastdue", new BaseEntityComplexValue(0, -1, batch.getRepDate(), fieldPastDue, false, true));
        } else if (localName.equals("write_off")) {
            currentBaseEntity.put("write_off", new BaseEntityComplexValue(0, -1, batch.getRepDate(), fieldWriteOf, false, true));
        } else if (localName.equals("value")) {
            if (debtWay.equals("current")) {
            } else if (debtWay.equals("pastdue")) {
            } else if (debtWay.equals("write_off")) {
            } else {
            }
        } else if (localName.equals("value_currency")) {

        } else if (localName.equals("balance_account")) {
            if (debtWay.equals("current")) {
            } else if (debtWay.equals("pastdue")) {
            } else if (debtWay.equals("write_off")) {
            } else {
            }
        } else if (localName.equals("open_date")) {
        } else if (localName.equals("close_date")) {
        } else if (localName.equals("date")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

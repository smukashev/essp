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
public class ChangeRemainsInterestParser extends BatchParser {
    private String interestWay;

    public ChangeRemainsInterestParser() {
        super();
    }

    private BaseEntity fieldCurrent;
    private BaseEntity fieldPastDue;
    private BaseEntity fieldWriteOf;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains_interest"),
                batch.getRepDate(), creditorId);

        fieldCurrent = null;
        fieldPastDue = null;
        fieldWriteOf = null;
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "interest":
                break;
            case "current":
                fieldCurrent = new BaseEntity(metaClassRepository.getMetaClass("remains_interest_current"),
                        batch.getRepDate(), creditorId);
                interestWay = localName;
                break;
            case "pastdue":
                fieldPastDue = new BaseEntity(metaClassRepository.getMetaClass("remains_interest_pastdue"),
                        batch.getRepDate(), creditorId);
                interestWay = localName;
                break;
            case "write_off":
                fieldWriteOf = new BaseEntity(metaClassRepository.getMetaClass("remains_interest_write_off"),
                        batch.getRepDate(), creditorId);
                interestWay = localName;
                break;
            case "value": {
                event = (XMLEvent) xmlReader.next();

                BaseValue baseValue = new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(),
                        new Double(event.asCharacters().getData()), false, true);

                switch (interestWay) {
                    case "current":
                        fieldCurrent.put("value", baseValue);
                        break;
                    case "pastdue":
                        fieldPastDue.put("value", baseValue);
                        break;
                    case "write_off":
                        fieldWriteOf.put("value", baseValue);
                        break;
                }
                break;
            }
            case "value_currency": {
                event = (XMLEvent) xmlReader.next();

                BaseValue baseValue = new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(),
                        new Double(event.asCharacters().getData()), false, true);

                switch (interestWay) {
                    case "current":
                        fieldCurrent.put("value_currency", baseValue);
                        break;
                    case "pastdue":
                        fieldPastDue.put("value_currency", baseValue);
                        break;
                    case "write_off":
                        fieldWriteOf.put("value_currency", baseValue);
                        break;
                }
                break;
            }
            case "balance_account": {
                event = (XMLEvent) xmlReader.next();
                BaseEntity baseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),
                        batch.getRepDate(), creditorId);

                baseEntity.put("no_",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asCharacters().getData(), false, true));

                BaseValue baseValue = new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), baseEntity, false, true);

                switch (interestWay) {
                    case "current":
                        fieldCurrent.put("balance_account", baseValue);
                        break;
                    case "pastdue":
                        fieldPastDue.put("balance_account", baseValue);
                        break;
                    case "write_off":
                        fieldWriteOf.put("balance_account", baseValue);
                        break;
                }
                break;
            }
            case "open_date": {
                event = (XMLEvent) xmlReader.next();

                String dateRaw = event.asCharacters().getData();

                try {
                    fieldPastDue.put("open_date",
                            new BaseEntityDateValue(0, creditorId, batch.getRepDate(), dateFormat.parse(dateRaw), false, true));
                } catch (ParseException e) {
                    getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            }
            case "close_date": {
                event = (XMLEvent) xmlReader.next();
                String dateRaw = event.asCharacters().getData();
                try {
                    fieldPastDue.put("close_date",
                            new BaseEntityDateValue(0, creditorId, batch.getRepDate(), dateFormat.parse(dateRaw), false, true));
                } catch (ParseException e) {
                    getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            }
            case "date": {
                event = (XMLEvent) xmlReader.next();
                String dateRaw = event.asCharacters().getData();
                try {
                    fieldWriteOf.put("date", new BaseEntityDateValue(0, creditorId, batch.getRepDate(),
                            dateFormat.parse(dateRaw), false, true));
                } catch (ParseException e) {
                    getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            }
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "interest":
                return true;
            case "current":
                currentBaseEntity.put("current",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), fieldCurrent, false, true));
                break;
            case "pastdue":
                currentBaseEntity.put("pastdue",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), fieldPastDue, false, true));
                break;
            case "write_off":
                currentBaseEntity.put("write_off",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), fieldWriteOf, false, true));
                break;
            case "value":
                switch (interestWay) {
                    case "current":
                        break;
                    case "pastdue":
                        break;
                    case "write_off":
                        break;
                    default:
                        break;
                }
                break;
            case "value_currency":
                switch (interestWay) {
                    case "current":
                        break;
                    case "pastdue":
                        break;
                    case "write_off":
                        break;
                    default:
                        break;
                }
                break;
            case "balance_account":
                switch (interestWay) {
                    case "current":
                        break;
                    case "pastdue":
                        break;
                    default:
                        break;
                }
                break;
            case "open_date":
                break;
            case "close_date":
                break;
            case "date":
                break;
            default:
                break;
        }

        return false;
    }
}

package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;

@Component
@Scope("prototype")
public class ChangeParser extends BatchParser {
    @Autowired
    private ChangeTurnoverParser changeTurnoverParser;

    @Autowired
    private ChangeRemainsParser changeRemainsParser;

    @Autowired
    private ChangeCreditFlowParser changeCreditFlowParser;

    private BaseEntityDateValue maturityDate;
    private BaseEntityDateValue prolongationDate;

    public ChangeParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("change"), batch.getRepDate(), creditorId);
        maturityDate = null;
        prolongationDate = null;
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "change":
                break;
            case "turnover":
                changeTurnoverParser.parse(xmlReader, batch, index, creditorId);
                currentBaseEntity.put("turnover",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), changeTurnoverParser.getCurrentBaseEntity(), false, true));
                break;
            case "remains":
                changeRemainsParser.parse(xmlReader, batch, index, creditorId);
                currentBaseEntity.put("remains",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), changeRemainsParser.getCurrentBaseEntity(), false, true));
                break;
            case "credit_flow":
                changeCreditFlowParser.parse(xmlReader, batch, index, creditorId);
                currentBaseEntity.put("credit_flow",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), changeCreditFlowParser.getCurrentBaseEntity(), false, true));
                break;
            case "maturity_date": {
                event = (XMLEvent) xmlReader.next();
                String dateRaw = trim(event.asCharacters().getData());
                try {
                    maturityDate = new BaseEntityDateValue(0, creditorId, batch.getRepDate(), dateFormat.parse(dateRaw),
                            false, true);
                } catch (ParseException e) {
                    currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            }
            case "prolongation_date": {
                event = (XMLEvent) xmlReader.next();
                String dateRaw = trim(event.asCharacters().getData());
                try {
                    prolongationDate = new BaseEntityDateValue(0, creditorId, batch.getRepDate(), dateFormat.parse(dateRaw), false, true);
                } catch (ParseException e) {
                    currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
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
            case "change":
                return true;
            case "turnover":
                break;
            case "remains":
                for (String e : changeRemainsParser.getCurrentBaseEntity().getValidationErrors())
                    getCurrentBaseEntity().addValidationError(e);

                changeRemainsParser.getCurrentBaseEntity().clearValidationErrors();
                break;
            case "credit_flow":
                break;
            case "maturity_date":
                break;
            case "prolongation_date":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    public BaseEntityDateValue getMaturityDate() {
        return maturityDate;
    }

    public BaseEntityDateValue getProlongationDate() {
        return prolongationDate;
    }
}

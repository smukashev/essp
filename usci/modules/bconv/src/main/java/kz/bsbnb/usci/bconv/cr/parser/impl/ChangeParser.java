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
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("change"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("change")) {
        } else if (localName.equals("turnover")) {
            changeTurnoverParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("turnover", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    changeTurnoverParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("remains")) {
            changeRemainsParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("remains", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    changeRemainsParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("credit_flow")) {
            changeCreditFlowParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("credit_flow", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    changeCreditFlowParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("maturity_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                maturityDate = new BaseEntityDateValue(0, -1, batch.getRepDate(), dateFormat.parse(dateRaw), false, true);
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if (localName.equals("prolongation_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                prolongationDate = new BaseEntityDateValue(0, -1, batch.getRepDate(), dateFormat.parse(dateRaw), false, true);
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
        if (localName.equals("change")) {
            return true;
        } else if (localName.equals("turnover")) {
        } else if (localName.equals("remains")) {
            for (String e : changeRemainsParser.getCurrentBaseEntity().getValidationErrors()) {
                getCurrentBaseEntity().addValidationError(e);
            }
            changeRemainsParser.getCurrentBaseEntity().clearValidationErrors();
        } else if (localName.equals("credit_flow")) {
        } else if (localName.equals("maturity_date")) {
        } else if (localName.equals("prolongation_date")) {
        } else {
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

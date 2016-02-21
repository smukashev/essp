package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class ChangeRemainsParser extends BatchParser {
    @Autowired
    private ChangeRemainsDebtParser changeRemainsDebtParser;

    @Autowired
    private ChangeRemainsInterestParser changeRemainsInterestParser;

    @Autowired
    private ChangeRemainsDiscountParser changeRemainsDiscountParser;

    @Autowired
    private ChangeRemainsCorrectionParser changeRemainsCorrectionParser;

    @Autowired
    private ChangeRemainsLimitParser changeRemainsLimitParser;

    @Autowired
    private ChangeRemainsDiscountedValueParser changeRemainsDiscountedValueParser;

    public ChangeRemainsParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains"), batch.getRepDate(), creditorId);
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("remains")) {
        } else if (localName.equals("debt")) {
            changeRemainsDebtParser.parse(xmlReader, batch, index, creditorId);
            currentBaseEntity.put("debt", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    changeRemainsDebtParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("interest")) {
            changeRemainsInterestParser.parse(xmlReader, batch, index, creditorId);
            currentBaseEntity.put("interest", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    changeRemainsInterestParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("discount")) {
            changeRemainsDiscountParser.parse(xmlReader, batch, index, creditorId);
            currentBaseEntity.put("discount", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    changeRemainsDiscountParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("correction")) {
            changeRemainsCorrectionParser.parse(xmlReader, batch, index, creditorId);
            currentBaseEntity.put("correction", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    changeRemainsCorrectionParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("discounted_value")) {
            changeRemainsDiscountedValueParser.parse(xmlReader, batch, index, creditorId);
            currentBaseEntity.put("discounted_value",
                    new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                            changeRemainsDiscountedValueParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("limit")) {
            changeRemainsLimitParser.parse(xmlReader, batch, index, creditorId);
            currentBaseEntity.put("limit", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    changeRemainsLimitParser.getCurrentBaseEntity(), false, true));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("remains")) {
            return true;
        } else if (localName.equals("debt")) {
            for (String e : changeRemainsDebtParser.getCurrentBaseEntity().getValidationErrors()) {
                getCurrentBaseEntity().addValidationError(e);
            }
            changeRemainsDebtParser.getCurrentBaseEntity().clearValidationErrors();
        } else if (localName.equals("interest")) {
            for (String e : changeRemainsInterestParser.getCurrentBaseEntity().getValidationErrors()) {
                getCurrentBaseEntity().addValidationError(e);
            }
            changeRemainsInterestParser.getCurrentBaseEntity().clearValidationErrors();
        } else if (localName.equals("discount")) {
        } else if (localName.equals("correction")) {
        } else if (localName.equals("discounted_value")) {
        } else if (localName.equals("value")) {
        } else if (localName.equals("limit")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

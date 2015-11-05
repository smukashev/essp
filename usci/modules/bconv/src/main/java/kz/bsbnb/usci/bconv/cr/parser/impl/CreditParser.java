package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.Map;
import java.util.Stack;

@Component
@Scope("prototype")
public class CreditParser extends BatchParser {
    private int portfolioCount;
    private BaseEntity currentPortfolio;

    @Autowired
    private CreditContractParser creditContractParser;

    @Autowired
    private CreditorBranchParser creditorBranchParser;

    public CreditParser() {
        super();
    }

    public void setCurrentBaseEntity(BaseEntity baseEntity) {
        currentBaseEntity = baseEntity;
    }

    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {

        if (localName.equals("credit")) {
        } else if (localName.equals("contract")) {
            creditContractParser.parse(xmlReader, batch, index);
            BaseEntity creditContract = creditContractParser.getCurrentBaseEntity();
            currentBaseEntity.put("contract", new BaseEntityComplexValue(0, -1, batch.getRepDate(), creditContract,
                    false, true));
        } else if (localName.equals("currency")) {

            RefListResponse refListResponse = refProcessorDao.getRefListResponse(
                    metaClassRepository.getMetaClass("ref_currency").getId(), batch.getRepDate(), false);

            boolean found = false;
            event = (XMLEvent) xmlReader.next();
            String crCode = event.asCharacters().getData();

            for (Map<String, Object> o : refListResponse.getData())
                if (o.get("SHORT_NAME") != null && o.get("SHORT_NAME").equals(crCode)) {
                    BaseEntity currency = new BaseEntity(metaClassRepository.getMetaClass("ref_currency"),
                            batch.getRepDate());
                    currency.put("code", new BaseValue(0, -1, batch.getRepDate(), o.get("CODE")));
                    currentBaseEntity.put("currency", new BaseEntityComplexValue(0, -1, batch.getRepDate(), currency,
                            false, true));
                    found = true;
                    break;
                }

            if (!found)
                currentBaseEntity.addValidationError(String.format("Валюта с кодом %s не найдена", crCode));

        } else if (localName.equals("interest_rate_yearly")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("interest_rate_yearly", new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true));
        } else if (localName.equals("contract_maturity_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                currentBaseEntity.put("contract_maturity_date", new BaseEntityDateValue(0, -1, batch.getRepDate(),
                        dateFormat.parse(dateRaw), false, true));
            } catch (ParseException e) {
                getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if (localName.equals("actual_issue_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                currentBaseEntity.put("actual_issue_date", new BaseEntityDateValue(0, -1, batch.getRepDate(),
                        dateFormat.parse(event.asCharacters().getData()), false, true));
            } catch (ParseException e) {
                getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if (localName.equals("credit_purpose")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity creditPurpose = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_purpose"),
                    batch.getRepDate());

            creditPurpose.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));

            currentBaseEntity.put("credit_purpose", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    creditPurpose, false, true));
        } else if (localName.equals("credit_object")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity creditObject = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_object"),
                    batch.getRepDate());

            creditObject.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));

            currentBaseEntity.put("credit_object", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    creditObject, false, true));
        } else if (localName.equals("amount")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("amount", new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true));
        } else if (localName.equals("finance_source")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity financeSource = new BaseEntity(metaClassRepository.getMetaClass("ref_finance_source"),
                    batch.getRepDate());

            financeSource.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));

            currentBaseEntity.put("finance_source", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    financeSource, false, true));
        } else if (localName.equals("has_currency_earn")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("has_currency_earn", new BaseEntityBooleanValue(0, -1, batch.getRepDate(),
                    new Boolean(event.asCharacters().getData()), false, true));
        } else if (localName.equals("creditor_branch")) {
            creditorBranchParser.parse(xmlReader, batch, index);
            BaseEntity creditorBranch = creditorBranchParser.getCurrentBaseEntity();
            currentBaseEntity.put("creditor_branch", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    creditorBranch, false, true));
        } else if (localName.equals("portfolio")) {
            portfolioCount++;

            if (portfolioCount == 2) {
                String value = getNullableTagValue(localName, event, xmlReader);

                if (value != null) {
                    BaseEntity portfolio = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),
                            batch.getRepDate());
                    portfolio.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), value, false, true));
                    currentPortfolio.put("portfolio", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                            portfolio, false, true));
                } else {
                    currentPortfolio.put("portfolio",new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                            null, false, true));
                }
            } else{
                currentPortfolio = new BaseEntity(metaClassRepository.getMetaClass("portfolio"), batch.getRepDate());
            }

        } else if (localName.equals("portfolio_msfo")) {
            String value = getNullableTagValue(localName, event, xmlReader);

            if (value != null) {
                BaseEntity portfolioMSFO = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),
                        batch.getRepDate());
                portfolioMSFO.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), value, false, true));
                currentPortfolio.put("portfolio_msfo", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                        portfolioMSFO, false, true));
            } else {
                currentPortfolio.put("portfolio_msfo", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                        null, false, true));
            }
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    private String getNullableTagValue(String localName, XMLEvent event, XMLEventReader xmlReader) {
        Attribute attrNullify = event.asStartElement().getAttributeByName(QName.valueOf("nullify"));

        if (attrNullify == null || !"true".equals(attrNullify.getValue())) {
            event = (XMLEvent) xmlReader.next();
            return event.asCharacters().getData();
        }

        return null;
    }

    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("credit")) {
            return true;
        } else if (localName.equals("contract")) {
        } else if (localName.equals("currency")) {
        } else if (localName.equals("interest_rate_yearly")) {
        } else if (localName.equals("contract_maturity_date")) {
        } else if (localName.equals("actual_issue_date")) {
        } else if (localName.equals("credit_purpose")) {
        } else if (localName.equals("credit_object")) {
        } else if (localName.equals("amount")) {
        } else if (localName.equals("finance_source")) {
        } else if (localName.equals("has_currency_earn")) {
        } else if (localName.equals("creditor_branch")) {
        } else if (localName.equals("portfolio")) {
            portfolioCount--;
            if (portfolioCount == 0)
                currentBaseEntity.put("portfolio", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                        currentPortfolio, false, true));

        } else if (localName.equals("portfolio_msfo")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

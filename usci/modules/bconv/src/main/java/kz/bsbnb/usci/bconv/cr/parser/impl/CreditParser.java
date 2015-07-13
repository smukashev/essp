package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import org.apache.log4j.Logger;
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
import java.util.*;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class CreditParser extends BatchParser {
    private int portfolioCount;
    private BaseEntity currentPortfolio;

    private Logger logger = Logger.getLogger(CreditParser.class);

    @Autowired
    private CreditContractParser creditContractParser;

    @Autowired
    private CreditorBranchParser creditorBranchParser;

    public CreditParser() {
        super();
    }

    public void setCurrentBaseEntity(BaseEntity baseEntity)
    {
        currentBaseEntity = baseEntity;
    }

    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("credit")) {
        } else if(localName.equals("contract")) {
            creditContractParser.parse(xmlReader, batch, index);
            BaseEntity creditContract = creditContractParser.getCurrentBaseEntity();
            currentBaseEntity.put("contract", new BaseEntityComplexValue(batch, index, creditContract));
        } else if(localName.equals("currency")) {

            RefListResponse refListResponse =  baseEntityRepository.getBaseEntityProcessorDao()
                    .getRefListResponse(metaClassRepository.getMetaClass("ref_currency").getId(),
                            batch.getRepDate(), false);

            boolean found = false;
            event = (XMLEvent) xmlReader.next();
            String crCode = event.asCharacters().getData();

            for(Map<String,Object> o : refListResponse.getData())
                if(o.get("SHORT_NAME")!=null && o.get("SHORT_NAME").equals(crCode)){
                    BaseEntity currency = new BaseEntity(metaClassRepository.getMetaClass("ref_currency"),batch.getRepDate());
                    currency.put("code", new BaseValue(batch, index, o.get("CODE")));
                    currentBaseEntity.put("currency", new BaseEntityComplexValue(batch, index, currency));
                    found = true;
                    break;
                }

            if(!found)
                currentBaseEntity.addValidationError(String.format("Валюта с кодом %s не найдена", crCode));

        } else if(localName.equals("interest_rate_yearly")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("interest_rate_yearly", new BaseEntityDoubleValue(batch, index,
                    new Double(event.asCharacters().getData())
                ));
        } else if(localName.equals("contract_maturity_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                currentBaseEntity.put("contract_maturity_date", new BaseEntityDateValue(batch, index,
                        dateFormat.parse(dateRaw)
                ));
            } catch (ParseException e) {
                getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if(localName.equals("actual_issue_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try{
                currentBaseEntity.put("actual_issue_date", new BaseEntityDateValue(batch, index,
                        dateFormat.parse(event.asCharacters().getData())
                ));
            } catch (ParseException e) {
                getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if(localName.equals("credit_purpose")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity creditPurpose = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_purpose"), batch.getRepDate());

            creditPurpose.put("code", new BaseEntityStringValue(batch, index, event.asCharacters().getData()));

            currentBaseEntity.put("credit_purpose", new BaseEntityComplexValue(batch, index,
                    creditPurpose
                ));
        } else if(localName.equals("credit_object")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity creditObject = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_object"), batch.getRepDate());

            creditObject.put("code", new BaseEntityStringValue(batch, index, event.asCharacters().getData()));

            currentBaseEntity.put("credit_object", new BaseEntityComplexValue(batch, index,
                    creditObject
                ));
        } else if(localName.equals("amount")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("amount", new BaseEntityDoubleValue(batch, index,
                    new Double(event.asCharacters().getData())
                ));
        } else if(localName.equals("finance_source")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity financeSource = new BaseEntity(metaClassRepository.getMetaClass("ref_finance_source"), batch.getRepDate());

            financeSource.put("code", new BaseEntityStringValue(batch, index, event.asCharacters().getData()));

            currentBaseEntity.put("finance_source", new BaseEntityComplexValue(batch, index,
                    financeSource
                ));
        } else if(localName.equals("has_currency_earn")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("has_currency_earn", new BaseEntityBooleanValue(batch, index,
                    new Boolean(event.asCharacters().getData())
            ));
        } else if(localName.equals("creditor_branch")) {
            creditorBranchParser.parse(xmlReader, batch, index);
            BaseEntity creditorBranch = creditorBranchParser.getCurrentBaseEntity();
            currentBaseEntity.put("creditor_branch", new BaseEntityComplexValue(batch, index, creditorBranch));
        } else if(localName.equals("portfolio")) {
            portfolioCount++;

            if(portfolioCount == 2){
                String value = getNullableTagValue(localName, event, xmlReader);

                if (value != null) {
                    BaseEntity portfolio = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),batch.getRepDate());
                    portfolio.put("code",new BaseEntityStringValue(batch,index, value));
                    currentPortfolio.put("portfolio",new BaseEntityComplexValue(batch,index,portfolio));
                } else {
                    currentPortfolio.put("portfolio",new BaseEntityComplexValue(batch,index,null));
                }
            } else{
                currentPortfolio = new BaseEntity(metaClassRepository.getMetaClass("portfolio"),batch.getRepDate());
            }

        } else if(localName.equals("portfolio_msfo")) {
            String value = getNullableTagValue(localName, event, xmlReader);

            if (value != null) {
                BaseEntity portfolioMSFO = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),batch.getRepDate());
                portfolioMSFO.put("code", new BaseEntityStringValue(batch, index, value));
                currentPortfolio.put("portfolio_msfo", new BaseEntityComplexValue(batch,index,portfolioMSFO));
            } else {
                currentPortfolio.put("portfolio_msfo", new BaseEntityComplexValue(batch,index, null));
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
        //try {
            if(localName.equals("credit")) {
                //currentPackage.setCredit(ctCredit);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("contract")) {
            } else if(localName.equals("currency")) {
                //ctCredit.setCurrency(contents.toString());
            } else if(localName.equals("interest_rate_yearly")) {
                //ctCredit.setInterestRateYearly(new BigDecimal(contents.toString()));
            } else if(localName.equals("contract_maturity_date")) {
                //ctCredit.setContractMaturityDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("actual_issue_date")) {
                //ctCredit.setActualIssueDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("credit_purpose")) {
                //ctCredit.setCreditPurpose(contents.toString());
            } else if(localName.equals("credit_object")) {
                //ctCredit.setCreditObject(contents.toString());
            } else if(localName.equals("amount")) {
                //ctCredit.setAmount(new BigDecimal(contents.toString()));
            } else if(localName.equals("finance_source")) {
                //ctCredit.setFinanceSource(contents.toString());
            } else if(localName.equals("has_currency_earn")) {
                //ctCredit.setHasCurrencyEarn(ParserUtils.parseBoolean(contents.toString()));
            } else if(localName.equals("creditor_branch")) {
            } else if(localName.equals("portfolio")) {
                portfolioCount--;
                if(portfolioCount ==0 )
                    currentBaseEntity.put("portfolio",new BaseEntityComplexValue(batch,index,currentPortfolio));

            } else if(localName.equals("portfolio_msfo")) {
                logger.info("portfolio_msfo end");
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch(ParseException parseException) {
            throw new TypeErrorException(localName);
        }*/
        return false;
    }
}

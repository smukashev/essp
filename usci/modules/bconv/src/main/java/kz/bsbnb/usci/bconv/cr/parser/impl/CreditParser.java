package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.TypeErrorException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class CreditParser extends BatchParser {
    private Stack stack = new Stack();
    private int portfolioCount;
    private BaseEntity currentPortfolio;

    private Logger logger = Logger.getLogger(CreditParser.class);

    @Autowired
    private CreditContractParser creditContractParser;

    @Autowired
    private CreditorBranchParser creditorBranchParser;

    private MetaClass currencyMetaClass;

    private List<IBaseEntity> refCurrencyList = new ArrayList<>();

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
                if(currencyMetaClass == null) {
                    try {
                        currencyMetaClass = metaClassRepository.getMetaClass("ref_currency");
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                if(currencyMetaClass != null && refCurrencyList.size() == 0) {
                    List<Long> refCurrencyIds = baseEntityRepository.getBaseEntityProcessorDao().
                            getEntityIDsByMetaclass(currencyMetaClass.getId());

                    for (Long refCurrencyId : refCurrencyIds) {
                        refCurrencyList.add(baseEntityRepository.getBaseEntityProcessorDao().load(refCurrencyId));
                    }
                }

                event = (XMLEvent) xmlReader.next();
                BaseEntity currency = new BaseEntity(currencyMetaClass, batch.getRepDate());

                String currencyCode = null;

                for(IBaseEntity entity : refCurrencyList) {
                    IBaseValue shortName = entity.getBaseValue("short_name");
                    if(shortName.getValue().toString().equals(event.asCharacters().getData())) {
                        currencyCode = entity.getBaseValue("code").getValue().toString();
                        break;
                    }
                }

                currency.put("code", new BaseEntityStringValue(batch, index, currencyCode));

                currentBaseEntity.put("currency", new BaseEntityComplexValue(batch, index, currency));
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
                    event = (XMLEvent) xmlReader.next();
                    BaseEntity portfolio = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),batch.getRepDate());
                    portfolio.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
                    currentPortfolio.put("portfolio",new BaseEntityComplexValue(batch,index,portfolio));
                    //if(!stack.pop().equals("portfolio")) {}
                    //portfolio = new Portfolio();
                } else{
                    currentPortfolio = new BaseEntity(metaClassRepository.getMetaClass("portfolio"),batch.getRepDate());
                }

            } else if(localName.equals("portfolio_msfo")) {
                event = (XMLEvent) xmlReader.next();
                BaseEntity portfolioMSFO = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),batch.getRepDate());
                portfolioMSFO.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
                currentPortfolio.put("portfolio_msfo",new BaseEntityComplexValue(batch,index,portfolioMSFO));
                //currentBaseEntity.put("portfolio",new BaseEntityComplexValue(batch,index,currentPortfolio));
            } else {
                throw new UnknownTagException(localName);
            }
        
            stack.push(localName);

        return false;
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
                if(stack.pop().equals("portfolio")) {
                    //portfolio.setPortfolio(contents.toString());
                } else {
                    //ctCredit.setPortfolio(portfolio);
                }
            } else if(localName.equals("portfolio_msfo")) {
                //portfolio.setPortfolioMsfo(contents.toString());
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch(ParseException parseException) {
            throw new TypeErrorException(localName);
        }*/
        return false;
    }
}

package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.type.DataTypes;
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

    private MetaClass refCurrencyMeta, refCreditPurposeMeta, refCreditObjectMeta, refFinanceSourceMeta, refPortfolioMeta,
                    portfolioMeta;

    public CreditParser() {
        super();
    }

    @Override
    public void init() {
        refCurrencyMeta = metaClassRepository.getMetaClass("ref_currency");
        refCreditPurposeMeta = metaClassRepository.getMetaClass("ref_credit_purpose");
        refCreditObjectMeta = metaClassRepository.getMetaClass("ref_credit_object");
        refFinanceSourceMeta = metaClassRepository.getMetaClass("ref_finance_source");
        refPortfolioMeta = metaClassRepository.getMetaClass("ref_portfolio");
        portfolioMeta = metaClassRepository.getMetaClass("portfolio");
    }

    public void setCurrentBaseEntity(BaseEntity baseEntity) {
        currentBaseEntity = baseEntity;
    }

    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {

        switch (localName) {
            case "credit":
                break;
            case "contract":
                creditContractParser.parse(xmlReader, batch, index, creditorId);

                BaseEntity creditContract = creditContractParser.getCurrentBaseEntity();

                currentBaseEntity.put("contract",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), creditContract, false, true));
                break;
            case "currency":
                event = (XMLEvent) xmlReader.next();

                BaseEntity currency = new BaseEntity(refCurrencyMeta, batch.getRepDate(), creditorId);

                currency.put("short_name",
                        new BaseValue<>(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData())));

                currentBaseEntity.put("currency",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currency, false, true));
                break;
            case "interest_rate_yearly":
                event = (XMLEvent) xmlReader.next();

                currentBaseEntity.put("interest_rate_yearly",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(),
                                new Double(trim(event.asCharacters().getData())), false, true));
                break;
            case "contract_maturity_date": {
                event = (XMLEvent) xmlReader.next();

                String dateRaw = trim(event.asCharacters().getData());

                try {
                    currentBaseEntity.put("contract_maturity_date",
                            new BaseEntityDateValue(0, creditorId, batch.getRepDate(), dateFormat.parse(dateRaw), false, true));
                } catch (ParseException e) {
                    getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            }
            case "actual_issue_date": {
                event = (XMLEvent) xmlReader.next();
                String dateRaw = trim(event.asCharacters().getData());
                try {
                    currentBaseEntity.put("actual_issue_date",
                            new BaseEntityDateValue(0, creditorId, batch.getRepDate(),
                                    dateFormat.parse(trim(event.asCharacters().getData())), false, true));
                } catch (ParseException e) {
                    getCurrentBaseEntity().addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            }
            case "credit_purpose":
                event = (XMLEvent) xmlReader.next();
                BaseEntity creditPurpose = new BaseEntity(refCreditPurposeMeta, batch.getRepDate(), creditorId);

                creditPurpose.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                currentBaseEntity.put("credit_purpose",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), creditPurpose, false, true));
                break;
            case "credit_object":
                event = (XMLEvent) xmlReader.next();
                BaseEntity creditObject = new BaseEntity(refCreditObjectMeta, batch.getRepDate(), creditorId);

                creditObject.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                currentBaseEntity.put("credit_object",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), creditObject, false, true));
                break;
            case "amount":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("amount",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(trim(event.asCharacters().getData())), false, true));
                break;
            case "finance_source":
                event = (XMLEvent) xmlReader.next();
                BaseEntity financeSource = new BaseEntity(refFinanceSourceMeta, batch.getRepDate(), creditorId);

                financeSource.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                currentBaseEntity.put("finance_source",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), financeSource, false, true));
                break;
            case "has_currency_earn":
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("has_currency_earn",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(),
                                (boolean)DataTypes.getCastObject(DataTypes.BOOLEAN, trim(event.asCharacters().getData())), false, true));
                break;
            case "creditor_branch":
                creditorBranchParser.parse(xmlReader, batch, index, creditorId);
                BaseEntity creditorBranch = creditorBranchParser.getCurrentBaseEntity();

                currentBaseEntity.put("creditor_branch",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), creditorBranch, false, true));
                break;
            case "portfolio":
                portfolioCount++;

                if (portfolioCount == 2) {
                    String value = getNullableTagValue(localName, event, xmlReader);

                    if (value != null) {
                        BaseEntity portfolio = new BaseEntity(refPortfolioMeta, batch.getRepDate(), creditorId);

                        portfolio.put("code",
                                new BaseEntityStringValue(0, creditorId, batch.getRepDate(), value, false, true));

                        currentPortfolio.put("portfolio",
                                new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), portfolio, false, true));
                    } else {
                        currentPortfolio.put("portfolio",
                                new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), null, false, true));
                    }
                } else {
                    currentPortfolio = new BaseEntity(portfolioMeta, batch.getRepDate(), creditorId);
                }
                break;
            case "portfolio_msfo":
                String value = getNullableTagValue(localName, event, xmlReader);

                if (value != null) {
                    BaseEntity portfolioMSFO = new BaseEntity(refPortfolioMeta, batch.getRepDate(), creditorId);

                    portfolioMSFO.put("code",
                            new BaseEntityStringValue(0, creditorId, batch.getRepDate(), value, false, true));

                    currentPortfolio.put("portfolio_msfo",
                            new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), portfolioMSFO, false, true));
                } else {
                    currentPortfolio.put("portfolio_msfo",
                            new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), null, false, true));
                }
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    private String getNullableTagValue(String localName, XMLEvent event, XMLEventReader xmlReader) {
        Attribute attrNullify = event.asStartElement().getAttributeByName(QName.valueOf("nullify"));

        if (attrNullify == null || !"true".equals(attrNullify.getValue())) {
            event = (XMLEvent) xmlReader.next();
            return trim(event.asCharacters().getData());
        }

        return null;
    }

    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "credit":
                return true;
            case "contract":
                break;
            case "currency":
                break;
            case "interest_rate_yearly":
                break;
            case "contract_maturity_date":
                break;
            case "actual_issue_date":
                break;
            case "credit_purpose":
                break;
            case "credit_object":
                break;
            case "amount":
                break;
            case "finance_source":
                break;
            case "has_currency_earn":
                break;
            case "creditor_branch":
                break;
            case "portfolio":
                portfolioCount--;
                if (portfolioCount == 0)
                    currentBaseEntity.put("portfolio",
                            new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentPortfolio, false, true));

                break;
            case "portfolio_msfo":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

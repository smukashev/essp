package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class ChangeTurnoverParser extends BatchParser {
    public ChangeTurnoverParser() {
        super();
    }

    private BaseEntity currentIssue;

    private BaseEntity currentInterest;
    boolean interestFlag = false;

    private BaseEntity currentDebt;
    boolean debtFlag = false;

    private MetaClass refTurnOverIssueMeta, refTurnOverIssueDebtMeta, refTurnOverissueInterestMeta;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("turnover"), batch.getRepDate(), creditorId);

        currentIssue = null;
        currentInterest = null;
        interestFlag = false;
        currentDebt = null;
        debtFlag = false;

        refTurnOverIssueMeta = metaClassRepository.getMetaClass("turnover_issue");
        refTurnOverIssueDebtMeta = metaClassRepository.getMetaClass("turnover_issue_debt");
        refTurnOverissueInterestMeta = metaClassRepository.getMetaClass("turnover_issue_interest");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "turnover":
                break;
            case "issue":
                currentIssue = new BaseEntity(refTurnOverIssueMeta, batch.getRepDate(), creditorId);
                break;
            case "debt":
                currentDebt = new BaseEntity(refTurnOverIssueDebtMeta, batch.getRepDate(), creditorId);
                debtFlag = true;
                break;
            case "interest":
                currentInterest = new BaseEntity(refTurnOverissueInterestMeta, batch.getRepDate(), creditorId);
                interestFlag = true;
                break;
            case "amount":
                if (interestFlag) {
                    event = (XMLEvent) xmlReader.next();
                    currentInterest.put("amount",
                            new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()), false, true));
                } else if (debtFlag) {
                    event = (XMLEvent) xmlReader.next();
                    currentDebt.put("amount",
                            new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()), false, true));
                }

                break;
            case "amount_currency":
                if (interestFlag) {
                    event = (XMLEvent) xmlReader.next();

                    currentInterest.put("amount_currency",
                            new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()), false, true));
                } else if (debtFlag) {
                    event = (XMLEvent) xmlReader.next();

                    currentDebt.put("amount_currency",
                            new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()), false, true));
                }
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "turnover":
                return true;
            case "issue":
                currentBaseEntity.put("issue",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentIssue, false, true));
                break;
            case "debt":
                debtFlag = false;
                currentIssue.put("debt",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentDebt, false, true));
                break;
            case "interest":
                interestFlag = false;
                currentIssue.put("interest",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentInterest, false, true));
                break;
            case "amount":
                break;
            case "amount_currency":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

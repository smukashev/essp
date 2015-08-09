package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
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

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("turnover"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("turnover")) {
        } else if (localName.equals("issue")) {
            currentIssue = new BaseEntity(metaClassRepository.getMetaClass("turnover_issue"), batch.getRepDate());
        } else if (localName.equals("debt")) {
            currentDebt = new BaseEntity(metaClassRepository.getMetaClass("turnover_issue_debt"), batch.getRepDate());
            debtFlag = true;
        } else if (localName.equals("interest")) {
            currentInterest = new BaseEntity(metaClassRepository.getMetaClass("turnover_issue_interest"),
                    batch.getRepDate());
            interestFlag = true;
        } else if (localName.equals("amount")) {
            if (interestFlag) {
                event = (XMLEvent) xmlReader.next();
                currentInterest.put("amount", new BaseEntityDoubleValue(-1, batch, index,
                        new Double(event.asCharacters().getData())));
            } else if (debtFlag) {
                event = (XMLEvent) xmlReader.next();
                currentDebt.put("amount", new BaseEntityDoubleValue(-1, batch, index,
                        new Double(event.asCharacters().getData())));
            }

        } else if (localName.equals("amount_currency")) {
            if (interestFlag) {
                event = (XMLEvent) xmlReader.next();
                currentInterest.put("amount_currency", new BaseEntityDoubleValue(-1, batch, index,
                        new Double(event.asCharacters().getData())));
            } else if (debtFlag) {
                event = (XMLEvent) xmlReader.next();
                currentDebt.put("amount_currency", new BaseEntityDoubleValue(-1, batch, index,
                        new Double(event.asCharacters().getData())));
            }
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("turnover")) {
            return true;
        } else if (localName.equals("issue")) {
            currentBaseEntity.put("issue", new BaseEntityComplexValue(-1, batch, index, currentIssue));
        } else if (localName.equals("debt")) {
            debtFlag = false;
            currentIssue.put("debt", new BaseEntityComplexValue(-1, batch, index, currentDebt));
        } else if (localName.equals("interest")) {
            interestFlag = false;
            currentIssue.put("interest", new BaseEntityComplexValue(-1, batch, index, currentInterest));
        } else if (localName.equals("amount")) {
        } else if (localName.equals("amount_currency")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

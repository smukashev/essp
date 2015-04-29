package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import java.util.Date;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class ChangeTurnoverParser extends BatchParser {
    public ChangeTurnoverParser() {
        super();
    }

    private BaseEntity currentInterest;
    boolean interestFlag = false;

    private BaseEntity currentDebt;
    boolean debtFlag = false;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("turnover_issue"),batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("turnover")) {
        } else if(localName.equals("issue")) {
            //ctTurnoverTypeBase = new CtTurnoverTypeBase();
        } else if(localName.equals("debt")) {
            currentDebt = new BaseEntity(metaClassRepository.getMetaClass("turnover_issue_debt"),batch.getRepDate());
            debtFlag = true;
            //ctTurnoverAmount = new CtTurnoverAmount();
        } else if(localName.equals("interest")) {
            currentInterest = new BaseEntity(metaClassRepository.getMetaClass("turnover_issue_interest"), batch.getRepDate());
            interestFlag = true;
            //ctTurnoverAmount = new CtTurnoverAmount();
        } else if(localName.equals("amount")) {
             if(interestFlag){
                 event = (XMLEvent) xmlReader.next();
                 currentInterest.put("amount",new BaseEntityDoubleValue(batch, index,new Double(event.asCharacters().getData())));
             }else if(debtFlag){
                 event = (XMLEvent) xmlReader.next();
                 currentDebt.put("amount",new BaseEntityDoubleValue(batch, index,new Double(event.asCharacters().getData())));
             }

        } else if(localName.equals("amount_currency")) {
            if(interestFlag){
                event = (XMLEvent) xmlReader.next();
                currentInterest.put("amount_currency",new BaseEntityDoubleValue(batch, index,new Double(event.asCharacters().getData())));
            }else if(debtFlag){
                event = (XMLEvent) xmlReader.next();
                currentDebt.put("amount_currency",new BaseEntityDoubleValue(batch, index,new Double(event.asCharacters().getData())));
            }
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("turnover")) {
            //ctChange.setTurnover(ctTurnover);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("issue")) {
           //ctTurnover.setIssue(ctTurnoverTypeBase);
        } else if(localName.equals("debt")) {
            //ctTurnoverTypeBase.setDebt(ctTurnoverAmount);
            debtFlag = false;
            currentBaseEntity.put("debt",new BaseEntityComplexValue(batch,index,currentDebt));
        } else if(localName.equals("interest")) {
            interestFlag = false;
            currentBaseEntity.put("interest",new BaseEntityComplexValue(batch,index,currentInterest));
            //ctTurnoverTypeBase.setInterest(ctTurnoverAmount);
        } else if(localName.equals("amount")) {
            //ctTurnoverAmount.setAmount(new BigDecimal(contents.toString()));
        } else if(localName.equals("amount_currency")) {
            //ctTurnoverAmount.setAmountCurrency(new BigDecimal(contents.toString()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

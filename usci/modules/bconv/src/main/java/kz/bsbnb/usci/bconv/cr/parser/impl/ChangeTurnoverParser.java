package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
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
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("turnover")) {
        } else if(localName.equals("issue")) {
            //ctTurnoverTypeBase = new CtTurnoverTypeBase();
        } else if(localName.equals("debt")) {
            //ctTurnoverAmount = new CtTurnoverAmount();
        } else if(localName.equals("interest")) {
            //ctTurnoverAmount = new CtTurnoverAmount();
        } else if(localName.equals("amount")) {
        } else if(localName.equals("amount_currency")) {
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
        } else if(localName.equals("interest")) {
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

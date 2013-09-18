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
public class ChangeRemainsDiscountParser extends BatchParser {
    public ChangeRemainsDiscountParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("discount")) {
        } else if(localName.equals("value")) {
        } else if(localName.equals("value_currency")) {
        } else if(localName.equals("balance_account")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("discount")) {
            //ctRemains.setDiscount(ctRemainsTypeDiscount);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("value")) {
            //ctRemainsTypeDiscount.setValue(new BigDecimal(contents.toString()));
        } else if(localName.equals("value_currency")) {
            //ctRemainsTypeDiscount.setValueCurrency(new BigDecimal(contents.toString()));
        } else if(localName.equals("balance_account")) {
            //ctRemainsTypeDiscount.setBalanceAccount(contents.toString());
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

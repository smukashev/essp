package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
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
public class ChangeRemainsLimitParser extends BatchParser {
    public ChangeRemainsLimitParser() {
        super();
    }
    
    @Override
    public void startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("limit")) {
        } else if(localName.equals("value")) {
        } else if(localName.equals("value_currency")) {
        } else if(localName.equals("balance_account")) {
        } else {
            throw new UnknownTagException(localName);
        }
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("limit")) {
            //ctRemains.setLimit(ctRemainsTypeLimit);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("value")) {
            //ctRemainsTypeLimit.setValue(new BigDecimal(contents.toString()));
        } else if(localName.equals("value_currency")) {
            //ctRemainsTypeLimit.setValueCurrency(new BigDecimal(contents.toString()));
        } else if(localName.equals("balance_account")) {
            //ctRemainsTypeLimit.setBalanceAccount(contents.toString());
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

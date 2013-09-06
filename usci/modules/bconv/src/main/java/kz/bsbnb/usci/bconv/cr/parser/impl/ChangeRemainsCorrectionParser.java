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
public class ChangeRemainsCorrectionParser extends BatchParser {
    public ChangeRemainsCorrectionParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("correction")) {
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
        if(localName.equals("correction")) {
            //ctRemains.setCorrection(ctRemainsTypeCorrection);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("value")) {
            //ctRemainsTypeCorrection.setValue(new BigDecimal(contents.toString()));
        } else if(localName.equals("value_currency")) {
            //ctRemainsTypeCorrection.setValueCurrency(new BigDecimal(contents.toString()));
        } else if(localName.equals("balance_account")) {
            //ctRemainsTypeCorrection.setBalanceAccount(contents.toString());
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

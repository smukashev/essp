package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ChangeRemainsParser extends BatchParser {
    @Autowired
    private ChangeRemainsDebtParser changeRemainsDebtParser;

    @Autowired
    private ChangeRemainsInterestParser changeRemainsInterestParser;

    @Autowired
    private ChangeRemainsDiscountParser changeRemainsDiscountParser;

    @Autowired
    private ChangeRemainsCorrectionParser changeRemainsCorrectionParser;

    @Autowired
    private ChangeRemainsLimitParser changeRemainsLimitParser;

    public ChangeRemainsParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("remains")) {
        } else if(localName.equals("debt")) {
            changeRemainsDebtParser.parse(xmlReader, batch, index);
        } else if(localName.equals("interest")) {
            changeRemainsInterestParser.parse(xmlReader, batch, index);
        } else if(localName.equals("discount")) {
            changeRemainsDiscountParser.parse(xmlReader, batch, index);
        } else if(localName.equals("correction")) {
            changeRemainsCorrectionParser.parse(xmlReader, batch, index);
        } else if(localName.equals("discounted_value")) {
            //ctRemainsTypeDiscountedValue = new CtRemainsTypeDiscountedValue();
        } else if(localName.equals("value")) {
        } else if(localName.equals("limit")) {
            changeRemainsLimitParser.parse(xmlReader, batch, index);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("remains")) {
            //ctChange.setRemains(ctRemains);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("debt")) {
        } else if(localName.equals("interest")) {
        } else if(localName.equals("discount")) {
        } else if(localName.equals("correction")) {
        } else if(localName.equals("discounted_value")) {
            //ctRemains.setDiscountedValue(ctRemainsTypeDiscountedValue);
        } else if(localName.equals("value")) {
            //ctRemainsTypeDiscountedValue.setValue(new BigDecimal(contents.toString()));
        } else if(localName.equals("limit")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

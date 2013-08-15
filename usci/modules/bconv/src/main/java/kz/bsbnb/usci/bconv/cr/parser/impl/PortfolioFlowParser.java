package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
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
public class PortfolioFlowParser extends BatchParser {
    public PortfolioFlowParser() {
        super();
    }
    
    @Override
    public void startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("portfolio_flow")) {
            //ctPortfolioFlowBase = new CtPortfolioFlowBase();
        } else if(localName.equals("portfolio")) {
        } else if(localName.equals("details")) {
            //details = new Details();
        } else if(localName.equals("detail")) {
            //detail = new Detail();
        } else if(localName.equals("balance_account")) {
        } else if(localName.equals("value")) {
        } else {
            throw new UnknownTagException(localName);
        }
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("portfolio_flow")) {
            //portfolioData.getPortfolioFlow().add(ctPortfolioFlowBase);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("portfolio")) {
            //ctPortfolioFlowBase.setPortfolio(contents.toString());
        } else if(localName.equals("details")) {
            //ctPortfolioFlowBase.setDetails(details);
        } else if(localName.equals("detail")) {
            //details.getDetail().add(detail);
        } else if(localName.equals("balance_account")) {
            //detail.setBalanceAccount(contents.toString());
        } else if(localName.equals("value")) {
            //detail.setValue(new BigDecimal(contents.toString()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

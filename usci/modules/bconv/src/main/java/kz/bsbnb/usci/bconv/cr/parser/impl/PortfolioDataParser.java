package kz.bsbnb.usci.bconv.cr.parser.impl;

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
public class PortfolioDataParser extends BatchParser {
    @Autowired
    private PortfolioFlowParser portfolioFlowParser = new PortfolioFlowParser();

    @Autowired
    private PortfolioFlowMsfoParser portfolioFlowMsfoParser = new PortfolioFlowMsfoParser();
    
    public PortfolioDataParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("portfolio_data")) {
        } else if(localName.equals("portfolio_flow")) {
            portfolioFlowParser.parse(xmlReader, batch);
        } else if(localName.equals("portfolio_flow_msfo")) {
            portfolioFlowMsfoParser.parse(xmlReader, batch);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("portfolio_data")) {
            //batch.setPortfolioData(portfolioData);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("portfolio_flow")) {
        } else if(localName.equals("portfolio_flow_msfo")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

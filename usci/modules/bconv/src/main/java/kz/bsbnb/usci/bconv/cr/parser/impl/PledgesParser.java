package kz.bsbnb.usci.bconv.cr.parser.impl;

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
import java.math.BigDecimal;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class PledgesParser extends BatchParser {
    public PledgesParser() {
        super();
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("pledges")) {
        } else if(localName.equals("pledge")) {
            //ctPledge = new CtPledge();
        } else if(localName.equals("pledge_type")) {
        } else if(localName.equals("contract")) {
            //ctContractBase = new CtContractBase();
        } else if(localName.equals("no")) {
        } else if(localName.equals("value")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("pledges")) {
            //currentPackage.setPledges(pledges);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("pledge")) {
            //pledges.getPledge().add(ctPledge);
        } else if(localName.equals("pledge_type")) {
            //ctPledge.setPledgeType(contents.toString());
        } else if(localName.equals("contract")) {
            //ctPledge.setContract(ctContractBase);
        } else if(localName.equals("no")) {
            //ctContractBase.setNo(contents.toString());
        } else if(localName.equals("value")) {
            //ctPledge.setValue(new BigDecimal(contents.toString()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

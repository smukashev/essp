package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import java.util.Date;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
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
public class PortfolioFlowMsfoParser extends BatchParser {
    public PortfolioFlowMsfoParser() {
        super();
    }

    private BaseSet currentDetails;
    private BaseEntity currentDetail;


    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("ct_portfolio_flow_msfo"),new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("portfolio_flow_msfo")) {
        } else if(localName.equals("portfolio")) {
              BaseEntity portfolio = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),new Date());
              event = (XMLEvent) xmlReader.next();
              portfolio.put("code",new BaseValue(batch,index,event.asCharacters().getData()));
              currentBaseEntity.put("portfolio",new BaseValue(batch,index,portfolio));
        } else if(localName.equals("details")) {
            currentDetails = new BaseSet(metaClassRepository.getMetaClass("detail"));
            //details = new Details();
        } else if(localName.equals("detail")) {
            currentDetail = new BaseEntity(metaClassRepository.getMetaClass("detail"),new Date());
            //detail = new Detail();
        } else if(localName.equals("balance_account")) {
            BaseEntity ba = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),new Date());
            event = (XMLEvent) xmlReader.next();
            ba.put("no_",new BaseValue(batch,index,event.asCharacters().getData()));
            currentDetail.put(localName,new BaseValue(batch,index,ba));
        } else if(localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            currentDetail.put(localName,new BaseValue(batch,index,new Double(event.asCharacters().getData())));
        } else if(localName.equals("discounted_value")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put(localName,new BaseValue(batch,index,new Double(event.asCharacters().getData())));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("portfolio_data")){
            hasMore = false;
            return true;
        } else if(localName.equals("portfolio_flow_msfo")) {
            //portfolioData.getPortfolioFlowMsfo().add(ctPortfolioFlowMsfo);
            //xmlReader.setContentHandler(contentHandler);
            hasMore = true;
            return true;
        } else if(localName.equals("portfolio")) {
            //ctPortfolioFlowMsfo.setPortfolio(contents.toString());
        } else if(localName.equals("details")) {
            //ctPortfolioFlowMsfo.setDetails(details);
            currentBaseEntity.put(localName,new BaseValue(batch,index,currentDetails));
        } else if(localName.equals("detail")) {
            //details.getDetail().add(detail);
            currentDetails.put(new BaseValue(batch,index,currentDetail));
        } else if(localName.equals("balance_account")) {
            //detail.setBalanceAccount(contents.toString());
        } else if(localName.equals("value")) {
            //detail.setValue(new BigDecimal(contents.toString()));
        } else if(localName.equals("discounted_value")) {
            //ctPortfolioFlowMsfo.setDiscountedValue(new BigDecimal(contents.toString()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

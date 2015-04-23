package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetComplexValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class PortfolioDataParser extends BatchParser {
    @Autowired
    private PortfolioFlowParser portfolioFlowParser = new PortfolioFlowParser();

    @Autowired
    private PortfolioFlowMsfoParser portfolioFlowMsfoParser = new PortfolioFlowMsfoParser();
    
    public PortfolioDataParser() {
        super();
    }

    private BaseSet portfolioFlow;
    private BaseSet portfolioFlowMsfo;

    private InfoParser infoParser;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("portfolio_data"),batch.getRepDate());
        portfolioFlow = new BaseSet(metaClassRepository.getMetaClass("ct_portfolio_flow_base"));
        portfolioFlowMsfo = new BaseSet(metaClassRepository.getMetaClass("ct_portfolio_flow_msfo"));

    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("portfolio_data")) {
        } else if(localName.equals("portfolio_flow")) {
            portfolioFlowParser.parse(xmlReader, batch, index);
            hasMore = portfolioFlowParser.hasMore();
            //currentBaseEntity = portfolioFlowParser.getCurrentBaseEntity();
            BaseEntity t = portfolioFlowParser.getCurrentBaseEntity();
            t.put("creditor",new BaseEntityComplexValue(batch,index,infoParser.getCurrentBaseEntity()));
            t.put("account_date",infoParser.getAccountDate());
            t.put("actual_credit_count",infoParser.getActualCreditCount());
            t.put("report_date",infoParser.getReportDate());
            portfolioFlow.put(new BaseSetComplexValue(batch,index,portfolioFlowParser.getCurrentBaseEntity()));

            //return true;
        } else if(localName.equals("portfolio_flow_msfo")) {
            portfolioFlowMsfoParser.parse(xmlReader, batch, index);
            hasMore = portfolioFlowMsfoParser.hasMore();
            //currentBaseEntity = portfolioFlowMsfoParser.getCurrentBaseEntity();
            BaseEntity t = portfolioFlowMsfoParser.getCurrentBaseEntity();
            t.put("creditor",new BaseEntityComplexValue(batch,index,infoParser.getCurrentBaseEntity()));
            t.put("account_date",infoParser.getAccountDate());
            t.put("actual_credit_count",infoParser.getActualCreditCount());
            t.put("report_date",infoParser.getReportDate());
            portfolioFlowMsfo.put(new BaseSetComplexValue(batch,index,t));

            //return true;
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
            currentBaseEntity.put("portfolio_flow",new BaseEntityComplexSet(batch,index,portfolioFlow));
            currentBaseEntity.put("portfolio_flow_msfo", new BaseEntityComplexSet(batch,index,portfolioFlowMsfo));
            return true;
        } else if(localName.equals("portfolio_flow")) {
        } else if(localName.equals("portfolio_flow_msfo")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    public void setInfoParser(InfoParser infoParser) {
        this.infoParser = infoParser;
    }
}

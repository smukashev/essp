package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetComplexValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

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

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("portfolio_data"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("portfolio_data")) {
            // do nothing
        } else if (localName.equals("portfolio_flow")) {
            portfolioFlowParser.parse(xmlReader, batch, index);
            getPortfolioFlow().put(new BaseSetComplexValue(-1, batch, index,
                    portfolioFlowParser.getCurrentBaseEntity()));

        } else if (localName.equals("portfolio_flow_msfo")) {
            portfolioFlowMsfoParser.parse(xmlReader, batch, index);
            getPortfolioFlowMsfo().put(new BaseSetComplexValue(-1, batch, index,
                    portfolioFlowMsfoParser.getCurrentBaseEntity()));

        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("portfolio_data")) {
            if (portfolioFlow != null) {
                currentBaseEntity.put("portfolio_flows_kfn", new BaseEntityComplexSet(-1, batch, index,
                        portfolioFlow));
            }
            if (portfolioFlowMsfo != null) {
                currentBaseEntity.put("portfolio_flows_msfo", new BaseEntityComplexSet(-1, batch, index,
                        portfolioFlowMsfo));
            }
            return true;
        } else {
            throw new UnknownTagException(localName);
        }
    }

    private BaseSet getPortfolioFlow() {
        if (portfolioFlow == null) {
            portfolioFlow = new BaseSet(metaClassRepository.getMetaClass("portfolio_flow_kfn"));
        }
        return portfolioFlow;
    }

    private BaseSet getPortfolioFlowMsfo() {
        if (portfolioFlowMsfo == null) {
            portfolioFlowMsfo = new BaseSet(metaClassRepository.getMetaClass("portfolio_flow_msfo"));
        }
        return portfolioFlowMsfo;
    }
}

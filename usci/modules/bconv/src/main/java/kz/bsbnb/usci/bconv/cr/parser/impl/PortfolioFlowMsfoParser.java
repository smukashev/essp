package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class PortfolioFlowMsfoParser extends BatchParser {
    public PortfolioFlowMsfoParser() {
        super();
    }

    private BaseSet currentDetails;
    private BaseEntity currentDetail;

    private MetaClass refPortfolioMeta, portfolioFlowDetailMeta, refBalanceAccountMeta;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("portfolio_flow_msfo"), batch.getRepDate(), creditorId);

        refPortfolioMeta = metaClassRepository.getMetaClass("ref_portfolio");
        portfolioFlowDetailMeta = metaClassRepository.getMetaClass("portfolio_flow_detail");
        refBalanceAccountMeta = metaClassRepository.getMetaClass("ref_balance_account");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "portfolio_flow_msfo":
                break;
            case "portfolio":
                BaseEntity portfolio = new BaseEntity(refPortfolioMeta, batch.getRepDate(), creditorId);

                event = (XMLEvent) xmlReader.next();

                portfolio.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                currentBaseEntity.put("portfolio",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), portfolio, false, true));
                break;
            case "details":
                currentDetails = new BaseSet(portfolioFlowDetailMeta, creditorId);
                break;
            case "detail":
                currentDetail = new BaseEntity(portfolioFlowDetailMeta, batch.getRepDate(), creditorId);
                break;
            case "balance_account":
                BaseEntity ba = new BaseEntity(refBalanceAccountMeta, batch.getRepDate(), creditorId);

                event = (XMLEvent) xmlReader.next();

                ba.put("no_",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                currentDetail.put(localName, new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), ba, false, true));
                break;
            case "value":
                event = (XMLEvent) xmlReader.next();

                currentDetail.put(localName,
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()), false, true));
                break;
            case "discounted_value":
                event = (XMLEvent) xmlReader.next();

                currentBaseEntity.put(localName,
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()), false, true));
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "portfolio_flow_msfo":
                return true;
            case "portfolio":
                break;
            case "details":
                currentBaseEntity.put(localName,
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), currentDetails, false, true));
                break;
            case "detail":
                currentDetails.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentDetail, false, true));
                break;
            case "balance_account":
                break;
            case "value":
                break;
            case "discounted_value":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

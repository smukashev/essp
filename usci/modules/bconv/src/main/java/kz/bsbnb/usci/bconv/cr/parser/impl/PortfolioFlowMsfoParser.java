package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
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


    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("portfolio_flow_msfo"),
                batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("portfolio_flow_msfo")) {
        } else if (localName.equals("portfolio")) {
            BaseEntity portfolio = new BaseEntity(metaClassRepository.getMetaClass("ref_portfolio"),
                    batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            portfolio.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            currentBaseEntity.put("portfolio", new BaseEntityComplexValue(0, -1,
                    batch.getRepDate(), portfolio, false, true));
        } else if (localName.equals("details")) {
            currentDetails = new BaseSet(metaClassRepository.getMetaClass("portfolio_flow_detail"));
        } else if (localName.equals("detail")) {
            currentDetail = new BaseEntity(metaClassRepository.getMetaClass("portfolio_flow_detail"),
                    batch.getRepDate());
        } else if (localName.equals("balance_account")) {
            BaseEntity ba = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),
                    batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            ba.put("no_", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            currentDetail.put(localName, new BaseEntityComplexValue(0, -1, batch.getRepDate(), ba, false, true));
        } else if (localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            currentDetail.put(localName, new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true));
        } else if (localName.equals("discounted_value")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put(localName, new BaseEntityDoubleValue(0, -1, batch.getRepDate(),
                    new Double(event.asCharacters().getData()), false, true));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("portfolio_flow_msfo")) {
            return true;
        } else if (localName.equals("portfolio")) {
        } else if (localName.equals("details")) {
            currentBaseEntity.put(localName, new BaseEntityComplexSet(0, -1, batch.getRepDate(), currentDetails,
                    false, true));
        } else if (localName.equals("detail")) {
            currentDetails.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), currentDetail, false, true));
        } else if (localName.equals("balance_account")) {
        } else if (localName.equals("value")) {
        } else if (localName.equals("discounted_value")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

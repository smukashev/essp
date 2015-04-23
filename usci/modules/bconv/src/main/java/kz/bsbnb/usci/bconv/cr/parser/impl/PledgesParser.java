package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.math.BigDecimal;
import java.util.Date;

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

            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("pledge"), batch.getRepDate());


        } else if(localName.equals("pledge_type")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity pledgeType = new BaseEntity(metaClassRepository.getMetaClass("ref_pledge_type"),batch.getRepDate());
            pledgeType.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
            currentBaseEntity.put("pledge_type",new BaseEntityComplexValue(batch,index,pledgeType));
        } else if(localName.equals("contract")) {
            //ctContractBase = new CtContractBase();
        } else if(localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("contract",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("value",new BaseEntityDoubleValue(batch,index,new Double(event.asCharacters().getData())));
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
            hasMore = false;
            return true;
        } else if(localName.equals("pledge")) {
            //pledges.getPledge().add(ctPledge);
            hasMore = true;
            return true;
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

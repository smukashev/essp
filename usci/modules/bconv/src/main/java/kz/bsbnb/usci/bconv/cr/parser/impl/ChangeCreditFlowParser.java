package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import java.math.BigDecimal;
import java.util.Date;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
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
public class ChangeCreditFlowParser extends BatchParser {
    public ChangeCreditFlowParser() {
        super();
    }

    private BaseEntity currentProvision;
    private BaseEntity currentProvisionKfn;
    private BaseEntity currentProvisionMsfo;
    private BaseEntity currentProvisionMsfoOverB;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("credit_flow"),new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("credit_flow")) {
        } else if(localName.equals("classification")) {
            BaseEntity classification = new BaseEntity(
                    metaClassRepository.getMetaClass("ref_classification"),
                    new Date()
            );
            event = (XMLEvent) xmlReader.next();
            classification.put("code",new BaseValue(batch,index, event.asCharacters().getData()));
            currentBaseEntity.put("classification",new BaseValue(batch,index,classification));
        } else if(localName.equals("provision")) {
            currentProvision = new BaseEntity(metaClassRepository.getMetaClass("provision"),new Date());
            currentProvisionKfn = null;
            currentProvisionMsfo = null;
            currentProvisionMsfoOverB = null;
            //ctProvision = new CtProvision();
        } else if(localName.equals("balance_account")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity t = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),new Date());
            t.put("no_",new BaseValue(batch,index,event.asCharacters().getData()));
            getCurrentProvisionKfn().put("balance_account",new BaseValue(batch,index,t));
            //currentProvision.put("balance_account",new BaseValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("balance_account_msfo")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity t = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),new Date());
            t.put("no_",new BaseValue(batch,index,event.asCharacters().getData()));
            getCurrentProvisionMsfo().put("balance_account",new BaseValue(batch,index,t));
            //currentProvision.put("balance_account_msfo",new BaseValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("balance_account_msfo_over_balance")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity t = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),new Date());
            t.put("no_",new BaseValue(batch,index,event.asCharacters().getData()));
            getCurrentProvisionMsfoOverB().put("balance_account",new BaseValue(batch,index,t));
//            currentProvision.put("balance_account_msfo_over_balance",
//                    new BaseValue(batch,index,event.asCharacters().getData())
//            );
        } else if(localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            getCurrentProvisionKfn().put("value",
                    new BaseValue(batch,index, new Double(event.asCharacters().getData())));
            /*currentProvision.put("value",
                    new BaseValue(batch,index,new Double(event.asCharacters().getData()))
            );*/
        } else if(localName.equals("value_msfo")) {
            event = (XMLEvent) xmlReader.next();
            getCurrentProvisionMsfo().put("value",
                    new BaseValue(batch,index, new Double(event.asCharacters().getData())));
//            currentProvision.put("value_msfo",
//                    new BaseValue(batch,index,new Double(event.asCharacters().getData()))
//            );
        } else if(localName.equals("value_msfo_over_balance")) {
            event = (XMLEvent) xmlReader.next();
            getCurrentProvisionMsfoOverB().put("value",
                    new BaseValue(batch,index, new Double(event.asCharacters().getData())));
//            currentProvision.put("value_msfo_over_balance",
//                    new BaseValue(batch,index,new Double(event.asCharacters().getData()))
//            );
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    /**
     * Sample xml :
     * <credit_flow>
     * <classification>...</classification>
     *  <provision>
     *    <balance_account>EE25</balance_account>
     *    <value>777</value>
     *    <balance_account_msfo>ERSX</balance_account_msfo>
     *    <value_msfo>778</value_msfo>
     *    <balance_account_msfo_over_balance>TTCCTT</balance_account_msfo_over_balance>
     *  </provision>
     * </credit_flow>
     */
    @Override
    public boolean endElement(String localName) throws SAXException {
        if(localName.equals("credit_flow")) {
            //ctChange.setCreditFlow(ctCreditFlow);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("classification")) {
            //ctCreditFlow.setClassification(contents.toString());
        } else if(localName.equals("provision")) {
            //ctCreditFlow.setProvision(ctProvision);
            if(currentProvisionKfn!=null)
               currentProvision.put("provision_kfn",new BaseValue(batch,index,currentProvisionKfn));

            if(currentProvisionMsfo!=null)
               currentProvision.put("provision_msfo",new BaseValue(batch,index,currentProvisionMsfo));

            if(currentProvisionMsfoOverB!=null)
               currentProvision.put("provision_msfo_over_balance",new BaseValue(batch,index,currentProvisionMsfoOverB));

            currentBaseEntity.put("provision",new BaseValue(batch,index,currentProvision));
        } else if(localName.equals("balance_account")) {
            //ctProvision.setBalanceAccount(contents.toString());
        } else if(localName.equals("balance_account_msfo")) {
            //ctProvision.setBalanceAccountMsfo(contents.toString());
        } else if(localName.equals("balance_account_msfo_over_balance")) {
            //ctProvision.setBalanceAccountMsfoOverBalance(contents.toString());
        } else if(localName.equals("value")) {
            //ctProvision.setValue(new BigDecimal(contents.toString()));
        } else if(localName.equals("value_msfo")) {
            //ctProvision.setValueMsfo(new BigDecimal(contents.toString()));
        } else if(localName.equals("value_msfo_over_balance")) {
            //ctProvision.setValueMsfoOverBalance(new BigDecimal(contents.toString()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    public BaseEntity getCurrentProvisionKfn() {
        if(currentProvisionKfn == null)
            currentProvisionKfn = new BaseEntity(metaClassRepository.getMetaClass("prov_h"),new Date());
        return currentProvisionKfn;
    }

    public BaseEntity getCurrentProvisionMsfo() {
        if(currentProvisionMsfo == null)
            currentProvisionMsfo = new BaseEntity(metaClassRepository.getMetaClass("prov_h"),new Date());
        return currentProvisionMsfo;
    }

    public BaseEntity getCurrentProvisionMsfoOverB() {
        if(currentProvisionMsfoOverB == null)
            currentProvisionMsfoOverB = new BaseEntity(metaClassRepository.getMetaClass("prov_h"),new Date());
        return currentProvisionMsfoOverB;
    }
}

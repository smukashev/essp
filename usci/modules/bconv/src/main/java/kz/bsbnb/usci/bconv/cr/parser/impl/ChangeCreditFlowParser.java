package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class ChangeCreditFlowParser extends BatchParser {
    public ChangeCreditFlowParser() {
        super();
    }

    private BaseEntity currentProvisionGroup;

    private BaseEntity currentProvisionKfn;
    private BaseEntity currentProvisionMsfo;
    private BaseEntity currentProvisionMsfoOverB;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("credit_flow"), batch.getRepDate(), creditorId);
        currentProvisionGroup = null;
        currentProvisionKfn = null;
        currentProvisionMsfo = null;
        currentProvisionMsfoOverB = null;
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if (localName.equals("credit_flow")) {
        } else if (localName.equals("classification")) {
            BaseEntity classification = new BaseEntity(metaClassRepository.getMetaClass("ref_classification"),
                    batch.getRepDate(), creditorId);
            event = (XMLEvent) xmlReader.next();
            classification.put("code", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            currentBaseEntity.put("classification", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    classification, false, true));
        } else if (localName.equals("provision")) {
            currentProvisionGroup = new BaseEntity(metaClassRepository.getMetaClass("provision_group"),
                    batch.getRepDate(), creditorId);

            currentProvisionKfn = null;
            currentProvisionMsfo = null;
            currentProvisionMsfoOverB = null;
        } else if (localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            getCurrentProvisionKfn().put("value",
                    new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()),
                            false, true));
        } else if (localName.equals("balance_account")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity balanceAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),
                    batch.getRepDate(), creditorId);

            balanceAccount.put("no_", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            getCurrentProvisionKfn().put("balance_account", new BaseEntityComplexValue(0, creditorId,
                    batch.getRepDate(), balanceAccount, false, true));
        } else if (localName.equals("value_msfo")) {
            event = (XMLEvent) xmlReader.next();
            getCurrentProvisionMsfo().put("value",
                    new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()),
                            false, true));
        } else if (localName.equals("balance_account_msfo")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity balanceAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),
                    batch.getRepDate(), creditorId);
            balanceAccount.put("no_", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            getCurrentProvisionMsfo().put("balance_account", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    balanceAccount, false, true));
        } else if (localName.equals("value_msfo_over_balance")) {
            event = (XMLEvent) xmlReader.next();
            getCurrentProvisionMsfoOverB().put("value",
                    new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(event.asCharacters().getData()),
                            false, true));
        } else if (localName.equals("balance_account_msfo_over_balance")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity balanceAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),
                    batch.getRepDate(), creditorId);
            balanceAccount.put("no_", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            getCurrentProvisionMsfoOverB().put("balance_account", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    balanceAccount, false, true));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("credit_flow")) {
            return true;
        } else if (localName.equals("classification")) {
        } else if (localName.equals("provision")) {
            if (currentProvisionKfn != null)
                currentProvisionGroup.put("provision_kfn", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                        currentProvisionKfn, false, true));

            if (currentProvisionMsfo != null)
                currentProvisionGroup.put("provision_msfo", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                        currentProvisionMsfo, false, true));

            if (currentProvisionMsfoOverB != null)
                currentProvisionGroup.put("provision_msfo_over_balance", new BaseEntityComplexValue(0, creditorId,
                        batch.getRepDate(), currentProvisionMsfoOverB, false, true));

            currentBaseEntity.put("provision", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                    currentProvisionGroup, false, true));

        } else if (localName.equals("balance_account")) {
        } else if (localName.equals("balance_account_msfo")) {
        } else if (localName.equals("balance_account_msfo_over_balance")) {
        } else if (localName.equals("value")) {
        } else if (localName.equals("value_msfo")) {
        } else if (localName.equals("value_msfo_over_balance")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    private BaseEntity getCurrentProvisionKfn() {
        if (currentProvisionKfn == null)
            currentProvisionKfn = new BaseEntity(metaClassRepository.getMetaClass("provision"),
                    batch.getRepDate(), creditorId);
        return currentProvisionKfn;
    }

    private BaseEntity getCurrentProvisionMsfo() {
        if (currentProvisionMsfo == null)
            currentProvisionMsfo = new BaseEntity(metaClassRepository.getMetaClass("provision"),
                    batch.getRepDate(), creditorId);
        return currentProvisionMsfo;
    }

    private BaseEntity getCurrentProvisionMsfoOverB() {
        if (currentProvisionMsfoOverB == null)
            currentProvisionMsfoOverB = new BaseEntity(metaClassRepository.getMetaClass("provision"),
                    batch.getRepDate(), creditorId);
        return currentProvisionMsfoOverB;
    }
}

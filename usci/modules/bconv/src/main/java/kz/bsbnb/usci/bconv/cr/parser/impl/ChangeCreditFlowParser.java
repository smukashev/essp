package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
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

    private MetaClass refClassificationMeta, refBalanceAccountMeta, refProvisionGroupMeta, refProvisionMeta;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("credit_flow"), batch.getRepDate(), creditorId);

        currentProvisionGroup = null;
        currentProvisionKfn = null;
        currentProvisionMsfo = null;
        currentProvisionMsfoOverB = null;

        refClassificationMeta = metaClassRepository.getMetaClass("ref_classification");
        refBalanceAccountMeta = metaClassRepository.getMetaClass("ref_balance_account");
        refProvisionGroupMeta = metaClassRepository.getMetaClass("provision_group");
        refProvisionMeta = metaClassRepository.getMetaClass("provision");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        switch (localName) {
            case "credit_flow":
                break;
            case "classification":
                BaseEntity classification = new BaseEntity(refClassificationMeta, batch.getRepDate(), creditorId);

                event = (XMLEvent) xmlReader.next();

                classification.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                currentBaseEntity.put("classification",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), classification, false, true));
                break;
            case "provision":
                currentProvisionGroup = new BaseEntity(refProvisionGroupMeta, batch.getRepDate(), creditorId);

                currentProvisionKfn = null;
                currentProvisionMsfo = null;
                currentProvisionMsfoOverB = null;
                break;
            case "value":
                event = (XMLEvent) xmlReader.next();
                getCurrentProvisionKfn().put("value",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(trim(event.asCharacters().getData())), false, true));
                break;
            case "balance_account": {
                event = (XMLEvent) xmlReader.next();
                BaseEntity balanceAccount = new BaseEntity(refBalanceAccountMeta, batch.getRepDate(), creditorId);

                balanceAccount.put("no_",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                getCurrentProvisionKfn().put("balance_account",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), balanceAccount, false, true));
                break;
            }
            case "value_msfo":
                event = (XMLEvent) xmlReader.next();
                getCurrentProvisionMsfo().put("value",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(trim(event.asCharacters().getData())),
                                false, true));
                break;
            case "balance_account_msfo": {
                event = (XMLEvent) xmlReader.next();

                BaseEntity balanceAccount = new BaseEntity(refBalanceAccountMeta, batch.getRepDate(), creditorId);

                balanceAccount.put("no_",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                getCurrentProvisionMsfo().put("balance_account",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), balanceAccount, false, true));
                break;
            }
            case "value_msfo_over_balance":
                event = (XMLEvent) xmlReader.next();
                getCurrentProvisionMsfoOverB().put("value",
                        new BaseEntityDoubleValue(0, creditorId, batch.getRepDate(), new Double(trim(event.asCharacters().getData())), false, true));
                break;
            case "balance_account_msfo_over_balance": {
                event = (XMLEvent) xmlReader.next();
                BaseEntity balanceAccount = new BaseEntity(refBalanceAccountMeta, batch.getRepDate(), creditorId);

                balanceAccount.put("no_",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                getCurrentProvisionMsfoOverB().put("balance_account",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), balanceAccount, false, true));
                break;
            }
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "credit_flow":
                return true;
            case "classification":
                break;
            case "provision":
                if (currentProvisionKfn != null)
                    currentProvisionGroup.put("provision_kfn",
                            new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentProvisionKfn, false, true));

                if (currentProvisionMsfo != null)
                    currentProvisionGroup.put("provision_msfo",
                            new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentProvisionMsfo, false, true));

                if (currentProvisionMsfoOverB != null)
                    currentProvisionGroup.put("provision_msfo_over_balance",
                            new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentProvisionMsfoOverB, false, true));

                currentBaseEntity.put("provision",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), currentProvisionGroup, false, true));

                break;
            case "balance_account":
                break;
            case "balance_account_msfo":
                break;
            case "balance_account_msfo_over_balance":
                break;
            case "value":
                break;
            case "value_msfo":
                break;
            case "value_msfo_over_balance":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    private BaseEntity getCurrentProvisionKfn() {
        if (currentProvisionKfn == null)
            currentProvisionKfn = new BaseEntity(refProvisionMeta, batch.getRepDate(), creditorId);
        return currentProvisionKfn;
    }

    private BaseEntity getCurrentProvisionMsfo() {
        if (currentProvisionMsfo == null)
            currentProvisionMsfo = new BaseEntity(refProvisionMeta, batch.getRepDate(), creditorId);
        return currentProvisionMsfo;
    }

    private BaseEntity getCurrentProvisionMsfoOverB() {
        if (currentProvisionMsfoOverB == null)
            currentProvisionMsfoOverB = new BaseEntity(refProvisionMeta, batch.getRepDate(), creditorId);
        return currentProvisionMsfoOverB;
    }
}

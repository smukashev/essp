package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetComplexValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class PackageParser extends BatchParser {
    @Autowired
    private PrimaryContractParser primaryContractParser;

    @Autowired
    private CreditParser creditParser;

    @Autowired
    private SubjectsParser subjectsParser;

    @Autowired
    private ChangeParser changeParser;

    @Autowired
    private PledgesParser pledgesParser;

    private int totalCount = 0;

    private MetaClass creditMeta, refCreditTypeMeta, pledgeMeta;

    @Override
    public void init() {
        creditMeta = metaClassRepository.getMetaClass("credit");
        refCreditTypeMeta = metaClassRepository.getMetaClass("ref_credit_type");
        pledgeMeta = metaClassRepository.getMetaClass("pledge");
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setCurrentBaseEntity(BaseEntity currentBaseEntity) {
        this.currentBaseEntity = currentBaseEntity;
        creditParser.setCurrentBaseEntity(currentBaseEntity);
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {

        switch (localName) {
            case "packages":
                break;
            case "package":
                currentBaseEntity = new BaseEntity(creditMeta, batch.getRepDate(), creditorId);
                // TODO: set index
                /*currentBaseEntity.setIndex(Long.parseLong(
                    event.asStartElement().getAttributeByName(new QName("no")).getValue()));*/

                creditParser.setCurrentBaseEntity(currentBaseEntity);
                break;
            case "primary_contract":
                primaryContractParser.parse(xmlReader, batch, index, creditorId);

                BaseEntity primaryContract = primaryContractParser.getCurrentBaseEntity();

                currentBaseEntity.put("primary_contract",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), primaryContract, false, true));

                for (String e : primaryContractParser.getCurrentBaseEntity().getValidationErrors())
                    getCurrentBaseEntity().addValidationError(e);

                primaryContractParser.getCurrentBaseEntity().clearValidationErrors();
                break;
            case "credit":
                creditParser.parse(xmlReader, batch, index, creditorId);
                BaseEntity credit = creditParser.getCurrentBaseEntity();

                if(event.asStartElement().getAttributeByName(new QName("credit_type")) != null) {
                    String creditTypeCode = event.asStartElement().getAttributeByName(new QName("credit_type")).getValue();
                    BaseEntity creditType = new BaseEntity(refCreditTypeMeta, batch.getRepDate(), creditorId);

                    creditType.put("code",
                            new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                    creditTypeCode, false, true));

                    credit.put("credit_type",
                            new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), creditType, false, true));
                }

                break;
            case "subjects":
                while (true) {
                    subjectsParser.parse(xmlReader, batch, index, creditorId);
                    if (subjectsParser.hasMore()) {
                        BaseEntity subject = subjectsParser.getCurrentBaseEntity();

                        if (subject != null)
                            currentBaseEntity.put("subject",
                                    new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), subject, false, true));
                    } else break;
                }
                break;
            case "pledges":
                BaseSet pledges = new BaseSet(pledgeMeta, creditorId);

                while (true) {
                    pledgesParser.parse(xmlReader, batch, index, creditorId);
                    if (pledgesParser.hasMore())
                        pledges.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), pledgesParser.getCurrentBaseEntity(), false, true));
                    else break;
                }

                currentBaseEntity.put("pledges", new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), pledges,
                        false, true));
                break;
            case "change":
                changeParser.parse(xmlReader, batch, index, creditorId);

                currentBaseEntity.put("change",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), changeParser.getCurrentBaseEntity(), false, true));

                for (String e : changeParser.getCurrentBaseEntity().getValidationErrors())
                    getCurrentBaseEntity().addValidationError(e);

                changeParser.getCurrentBaseEntity().clearValidationErrors();

                if (changeParser.getMaturityDate() != null)
                    currentBaseEntity.put("maturity_date", changeParser.getMaturityDate());

                if (changeParser.getProlongationDate() != null)
                    currentBaseEntity.put("prolongation_date", changeParser.getProlongationDate());
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "packages":
                hasMore = false;
                break;
            case "package":
                totalCount++;
                hasMore = true;
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return true;
    }
}

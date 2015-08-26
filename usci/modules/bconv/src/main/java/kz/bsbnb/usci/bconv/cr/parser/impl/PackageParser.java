package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetComplexValue;
import org.apache.log4j.Logger;
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

        if (localName.equals("packages")) {
        } else if (localName.equals("package")) {
            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("credit"), batch.getRepDate());
            currentBaseEntity.setIndex(Long.parseLong(event.asStartElement().
                    getAttributeByName(new QName("no")).getValue()));

            creditParser.setCurrentBaseEntity(currentBaseEntity);
        } else if (localName.equals("primary_contract")) {
            primaryContractParser.parse(xmlReader, batch, index);
            BaseEntity primaryContract = primaryContractParser.getCurrentBaseEntity();
            currentBaseEntity.put("primary_contract", new BaseEntityComplexValue(-1, batch, index, primaryContract));
            for (String e : primaryContractParser.getCurrentBaseEntity().getValidationErrors()) {
                getCurrentBaseEntity().addValidationError(e);
            }
            primaryContractParser.getCurrentBaseEntity().clearValidationErrors();
        } else if (localName.equals("credit")) {
            creditParser.parse(xmlReader, batch, index);
            BaseEntity credit = creditParser.getCurrentBaseEntity();

            BaseEntity creditType = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_type"),
                    batch.getRepDate());

            creditType.put("code", new BaseEntityStringValue(-1, batch, index,
                    event.asStartElement().getAttributeByName(new QName("credit_type")).getValue()));

            credit.put("credit_type", new BaseEntityComplexValue(-1, batch, index,
                    creditType));
        } else if (localName.equals("subjects")) {
            BaseSet organizations = new BaseSet(metaClassRepository.getMetaClass("organization"));
            BaseSet persons = new BaseSet(metaClassRepository.getMetaClass("person"));
            BaseSet creditors = new BaseSet(metaClassRepository.getMetaClass("creditor"));

            while (true) {
                subjectsParser.parse(xmlReader, batch, index);
                if (subjectsParser.hasMore()) {
                    BaseEntity subject = subjectsParser.getCurrentBaseEntity();
                    if (subject != null) {
                        if (subject.getMeta().getClassName().equals("person")) {
                            persons.put(subject.getMeta().getClassName(),
                                    new BaseSetComplexValue(-1, batch, index, subject));
                        } else if(subject.getMeta().getClassName().equals("organization")) {
                            organizations.put(subject.getMeta().getClassName(),
                                    new BaseSetComplexValue(-1, batch, index, subject));
                        } else if(subject.getMeta().getClassName().equals("creditor")) {
                            creditors.put(subject.getMeta().getClassName(),
                                    new BaseSetComplexValue(-1, batch, index, subject));
                        } else {
                            throw new IllegalStateException("Тип субъекта не определен(" +
                                    subject.getMeta().getClassName() + ");");
                        }
                    }
                } else {
                    break;
                }
            }

            if (organizations.get().size() > 0)
                currentBaseEntity.put("organizations", new BaseEntityComplexSet(-1, batch, index, organizations));

            if (persons.get().size() > 0)
                currentBaseEntity.put("persons", new BaseEntityComplexSet(-1, batch, index, persons));

            if (creditors.get().size() > 0)
                currentBaseEntity.put("creditors", new BaseEntityComplexSet(-1, batch, index, creditors));
        } else if (localName.equals("pledges")) {
            BaseSet pledges = new BaseSet(metaClassRepository.getMetaClass("pledge"));
            while (true) {
                pledgesParser.parse(xmlReader, batch, index);
                if (pledgesParser.hasMore()) {
                    pledges.put(new BaseSetComplexValue(-1, batch, index, pledgesParser.getCurrentBaseEntity()));
                } else break;
            }
            currentBaseEntity.put("pledges", new BaseEntityComplexSet(-1, batch, index, pledges));

        } else if (localName.equals("change")) {
            changeParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("change", new BaseEntityComplexValue(-1, batch, index,
                    changeParser.getCurrentBaseEntity()));

            for (String e : changeParser.getCurrentBaseEntity().getValidationErrors()) {
                getCurrentBaseEntity().addValidationError(e);
            }
            changeParser.getCurrentBaseEntity().clearValidationErrors();

            if (changeParser.getMaturityDate() != null) {
                currentBaseEntity.put("maturity_date", changeParser.getMaturityDate());
            }
            if (changeParser.getProlongationDate() != null) {
                currentBaseEntity.put("prolongation_date", changeParser.getProlongationDate());
            }

        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("packages")) {
            hasMore = false;
        } else if (localName.equals("package")) {
            totalCount++;
            hasMore = true;
        } else {
            throw new UnknownTagException(localName);
        }

        return true;
    }
}

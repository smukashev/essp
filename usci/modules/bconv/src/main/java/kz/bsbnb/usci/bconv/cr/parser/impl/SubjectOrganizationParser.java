package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

@Component
@Scope("prototype")
public class SubjectOrganizationParser extends BatchParser {
    @Autowired
    private SubjectOrganizationNamesParser subjectOrganizationNamesParser;

    @Autowired
    private SubjectOrganizationDocsParser subjectOrganizationDocsParser;

    @Autowired
    private SubjectOrganizationHeadParser subjectOrganizationHeadParser;

    public SubjectOrganizationParser() {
        super();
    }

    private BaseSet bankRelations;
    private BaseSet addresses;
    private BaseSet contacts;

    private BaseEntity currentContact;
    private BaseEntity currentAddress;
    private BaseEntity organizationInfo;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("subject"), batch.getRepDate());
        organizationInfo = new BaseEntity(metaClassRepository.getMetaClass("organization_info"), batch.getRepDate());

        bankRelations = null;
        addresses = null;
        contacts = null;
        currentContact = null;
        currentAddress = null;
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if (localName.equals("organization")) {
        } else if (localName.equals("country")) {
            event = (XMLEvent) xmlReader.next();

            BaseEntity country = new BaseEntity(metaClassRepository.getMetaClass("ref_country"), batch.getRepDate());

            country.put("code_numeric", new BaseEntityIntegerValue(0, -1, batch.getRepDate(),
                    new Integer(event.asCharacters().getData()), false, true));

            organizationInfo.put("country", new BaseEntityComplexValue(0, -1, batch.getRepDate(), country,
                    false, true));
        } else if (localName.equals("offshore")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity offshore = new BaseEntity(metaClassRepository.getMetaClass("ref_offshore"), batch.getRepDate());

            offshore.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(),
                    false, true));

            organizationInfo.put("offshore", new BaseEntityComplexValue(0, -1, batch.getRepDate(), offshore,
                    false, true));
        } else if (localName.equals("bank_relations")) {
            bankRelations = new BaseSet(metaClassRepository.getMetaClass("bank_relation"));
        } else if (localName.equals("bank_relation")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity bankRelation = new BaseEntity(metaClassRepository.getMetaClass("bank_relation"),
                    batch.getRepDate());

            BaseEntity refBankRelation = new BaseEntity(metaClassRepository.getMetaClass("ref_bank_relation"),
                    batch.getRepDate());

            refBankRelation.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            bankRelation.put("bank_relation", new BaseEntityComplexValue(0, -1, batch.getRepDate(), refBankRelation,
                    false, true));
            bankRelations.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), bankRelation,
                    false, true));
        } else if (localName.equals("addresses")) {
            addresses = new BaseSet(metaClassRepository.getMetaClass("address"));

        } else if (localName.equals("address")) {
            currentAddress = new BaseEntity(metaClassRepository.getMetaClass("address"), batch.getRepDate());
            currentAddress.put("type", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asStartElement().getAttributeByName(new QName("type")).getValue(), false, true));

        } else if (localName.equals("region")) {
            BaseEntity region = new BaseEntity(metaClassRepository.getMetaClass("ref_region"), batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            region.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(),
                    false, true));
            currentAddress.put("region", new BaseEntityComplexValue(0, -1, batch.getRepDate(), region, false, true));
        } else if (localName.equals("details")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("details", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else if (localName.equals("contacts")) {
            contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));
        } else if (localName.equals("contact")) {
            currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), batch.getRepDate());

            BaseEntity contactType = new BaseEntity(metaClassRepository.getMetaClass("ref_contact_type"), new Date());

            contactType.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asStartElement().getAttributeByName(new QName("contact_type")).getValue(), false, true));

            currentContact.put("contact_type", new BaseEntityComplexValue(0, -1, batch.getRepDate(), contactType,
                    false, true));

            BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

            event = (XMLEvent) xmlReader.next();
            contactDetails.put(new BaseSetStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(),
                    false, true));
            currentContact.put("details", new BaseEntitySimpleSet(0, -1, batch.getRepDate(), contactDetails,
                    false, true));

            contacts.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), currentContact, false, true));
        } else if (localName.equals("names")) {
            BaseSet organizationNames = new BaseSet(metaClassRepository.getMetaClass("organization_name"));

            while (true) {
                subjectOrganizationNamesParser.parse(xmlReader, batch, index);
                if (subjectOrganizationNamesParser.hasMore()) {
                    organizationNames.put(new BaseSetComplexValue(0, -1, batch.getRepDate(),
                            subjectOrganizationNamesParser.getCurrentBaseEntity(), false, true));
                } else break;
            }
            organizationInfo.put("names", new BaseEntityComplexSet(0, -1, batch.getRepDate(), organizationNames,
                    false, true));
        } else if (localName.equals("head")) {
            subjectOrganizationHeadParser.parse(xmlReader, batch, index);

            organizationInfo.put("head", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    subjectOrganizationHeadParser.getCurrentBaseEntity(), false, true));
        } else if (localName.equals("legal_form")) {
            BaseEntity legalForm = new BaseEntity(metaClassRepository.getMetaClass("ref_legal_form"),
                    batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            legalForm.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(),
                    false, true));
            organizationInfo.put("legal_form", new BaseEntityComplexValue(0, -1, batch.getRepDate(), legalForm,
                    false, true));
        } else if (localName.equals("enterprise_type")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity enterpriseType = new BaseEntity(metaClassRepository.getMetaClass("ref_enterprise_type"),
                    batch.getRepDate());
            enterpriseType.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));

            organizationInfo.put("enterprise_type", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    enterpriseType, false, true));
        } else if (localName.equals("econ_trade")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity econTrade = new BaseEntity(metaClassRepository.getMetaClass("ref_econ_trade"),
                    batch.getRepDate());
            econTrade.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(),
                    false, true));
            organizationInfo.put("econ_trade", new BaseEntityComplexValue(0, -1, batch.getRepDate(), econTrade,
                    false, true));
        } else if (localName.equals("is_se")) {
            event = (XMLEvent) xmlReader.next();
            organizationInfo.put("is_se", new BaseEntityBooleanValue(0, -1, batch.getRepDate(),
                    new Boolean(event.asCharacters().getData()), false, true));
        } else if (localName.equals("docs")) {
            BaseSet organizationDocs = new BaseSet(metaClassRepository.getMetaClass("document"));

            while (true) {
                subjectOrganizationDocsParser.parse(xmlReader, batch, index);
                if (subjectOrganizationDocsParser.hasMore()) {
                    organizationDocs.put(new BaseSetComplexValue(0, -1, batch.getRepDate(),
                            subjectOrganizationDocsParser.getCurrentBaseEntity(), false, true));
                } else break;
            }


            currentBaseEntity.put("docs", new BaseEntityComplexSet(0, -1, batch.getRepDate(), organizationDocs,
                    false, true));

        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("organization")) {
            currentBaseEntity.put("organization_info", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    organizationInfo, false, true));
            currentBaseEntity.put("is_person", new BaseEntityBooleanValue(0, -1, batch.getRepDate(), false, true,
                    false));
            currentBaseEntity.put("is_organization", new BaseEntityBooleanValue(0, -1, batch.getRepDate(), true, true,
                    false));
            currentBaseEntity.put("is_creditor", new BaseEntityBooleanValue(0, -1, batch.getRepDate(), false, true,
                    false));
            return true;
        } else if (localName.equals("country")) {
        } else if (localName.equals("offshore")) {
        } else if (localName.equals("bank_relations")) {
            organizationInfo.put("bank_relations", new BaseEntityComplexSet(0, -1, batch.getRepDate(), bankRelations,
                    false, true));
        } else if (localName.equals("bank_relation")) {
        } else if (localName.equals("addresses")) {
            organizationInfo.put("addresses", new BaseEntityComplexSet(0, -1, batch.getRepDate(), addresses,
                    false, true));
        } else if (localName.equals("address")) {
            addresses.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), currentAddress, false, true));
        } else if (localName.equals("region")) {
        } else if (localName.equals("details")) {
        } else if (localName.equals("contacts")) {
        } else if (localName.equals("contact")) {
            organizationInfo.put("contacts", new BaseEntityComplexSet(0, -1, batch.getRepDate(), contacts,
                    false, true));
        } else if (localName.equals("names")) {
        } else if (localName.equals("head")) {
        } else if (localName.equals("legal_form")) {
        } else if (localName.equals("enterprise_type")) {
        } else if (localName.equals("econ_trade")) {
        } else if (localName.equals("is_se")) {
        } else if (localName.equals("docs")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

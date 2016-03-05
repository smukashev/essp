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
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("subject"),
                batch.getRepDate(), creditorId);

        organizationInfo = new BaseEntity(metaClassRepository.getMetaClass("organization_info"),
                batch.getRepDate(), creditorId);

        bankRelations = null;
        addresses = null;
        contacts = null;
        currentContact = null;
        currentAddress = null;
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "organization":
                break;
            case "country":
                event = (XMLEvent) xmlReader.next();

                BaseEntity country = new BaseEntity(metaClassRepository.getMetaClass("ref_country"),
                        batch.getRepDate(), creditorId);

                country.put("code_numeric",
                        new BaseEntityIntegerValue(0, creditorId, batch.getRepDate(),
                                new Integer(event.asCharacters().getData()), false, true));

                organizationInfo.put("country",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), country, false, true));
                break;
            case "offshore":
                event = (XMLEvent) xmlReader.next();
                BaseEntity offshore = new BaseEntity(metaClassRepository.getMetaClass("ref_offshore"),
                        batch.getRepDate(), creditorId);

                offshore.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                organizationInfo.put("offshore",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), offshore, false, true));
                break;
            case "bank_relations":
                bankRelations = new BaseSet(metaClassRepository.getMetaClass("bank_relation"));
                break;
            case "bank_relation":
                event = (XMLEvent) xmlReader.next();

                BaseEntity bankRelation = new BaseEntity(metaClassRepository.getMetaClass("bank_relation"),
                        batch.getRepDate(), creditorId);

                BaseEntity refBankRelation = new BaseEntity(metaClassRepository.getMetaClass("ref_bank_relation"),
                        batch.getRepDate(), creditorId);

                refBankRelation.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                bankRelation.put("bank_relation",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), refBankRelation, false, true));

                bankRelations.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), bankRelation, false, true));
                break;
            case "addresses":
                addresses = new BaseSet(metaClassRepository.getMetaClass("address"));

                break;
            case "address":
                currentAddress = new BaseEntity(metaClassRepository.getMetaClass("address"), batch.getRepDate(), creditorId);

                currentAddress.put("type",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asStartElement().getAttributeByName(new QName("type")).getValue(), false, true));
                break;
            case "region":
                BaseEntity region = new BaseEntity(metaClassRepository.getMetaClass("ref_region"), batch.getRepDate(), creditorId);

                event = (XMLEvent) xmlReader.next();

                region.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                currentAddress.put("region", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), region, false, true));
                break;
            case "details":
                event = (XMLEvent) xmlReader.next();
                currentAddress.put("details",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));
                break;
            case "contacts":
                contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));
                break;
            case "contact":
                currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), batch.getRepDate(), creditorId);

                BaseEntity contactType = new BaseEntity(metaClassRepository.getMetaClass("ref_contact_type"), new Date(), creditorId);

                contactType.put("code", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                        event.asStartElement().getAttributeByName(new QName("contact_type")).getValue(), false, true));

                currentContact.put("contact_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), contactType, false, true));

                BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

                event = (XMLEvent) xmlReader.next();

                contactDetails.put(new BaseSetStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                currentContact.put("details",
                        new BaseEntitySimpleSet(0, creditorId, batch.getRepDate(), contactDetails, false, true));

                contacts.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentContact, false, true));
                break;
            case "names":
                BaseSet organizationNames = new BaseSet(metaClassRepository.getMetaClass("organization_name"));

                while (true) {
                    subjectOrganizationNamesParser.parse(xmlReader, batch, index, creditorId);
                    if (subjectOrganizationNamesParser.hasMore()) {
                        organizationNames.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(),
                                subjectOrganizationNamesParser.getCurrentBaseEntity(), false, true));
                    } else break;
                }
                organizationInfo.put("names",
                         new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), organizationNames, false, true));
                break;
            case "head":
                subjectOrganizationHeadParser.parse(xmlReader, batch, index, creditorId);

                organizationInfo.put("head", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                        subjectOrganizationHeadParser.getCurrentBaseEntity(), false, true));
                break;
            case "legal_form":
                BaseEntity legalForm = new BaseEntity(metaClassRepository.getMetaClass("ref_legal_form"),
                        batch.getRepDate(), creditorId);

                event = (XMLEvent) xmlReader.next();

                legalForm.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                organizationInfo.put("legal_form",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), legalForm, false, true));
                break;
            case "enterprise_type":
                event = (XMLEvent) xmlReader.next();

                BaseEntity enterpriseType = new BaseEntity(metaClassRepository.getMetaClass("ref_enterprise_type"),
                        batch.getRepDate(), creditorId);

                enterpriseType.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                organizationInfo.put("enterprise_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), enterpriseType, false, true));
                break;
            case "econ_trade":
                event = (XMLEvent) xmlReader.next();

                BaseEntity econTrade = new BaseEntity(metaClassRepository.getMetaClass("ref_econ_trade"),
                        batch.getRepDate(), creditorId);

                econTrade.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), event.asCharacters().getData(), false, true));

                organizationInfo.put("econ_trade", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), econTrade, false, true));
                break;
            case "is_se":
                event = (XMLEvent) xmlReader.next();

                organizationInfo.put("is_se",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(),
                                Boolean.valueOf(event.asCharacters().getData()), false, true));
                break;
            case "docs":
                BaseSet organizationDocs = new BaseSet(metaClassRepository.getMetaClass("document"));

                while (true) {
                    subjectOrganizationDocsParser.parse(xmlReader, batch, index, creditorId);
                    if (subjectOrganizationDocsParser.hasMore()) {
                        organizationDocs.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(),
                                subjectOrganizationDocsParser.getCurrentBaseEntity(), false, true));
                    } else break;
                }


                currentBaseEntity.put("docs", new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), organizationDocs, false, true));

                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "organization":
                currentBaseEntity.put("organization_info",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), organizationInfo, false, true));

                currentBaseEntity.put("is_person",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), false, true, false));

                currentBaseEntity.put("is_organization",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), true, true, false));

                currentBaseEntity.put("is_creditor",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), false, true, false));
                return true;
            case "country":
                break;
            case "offshore":
                break;
            case "bank_relations":
                organizationInfo.put("bank_relations",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), bankRelations,
                        false, true));
                break;
            case "bank_relation":
                break;
            case "addresses":
                organizationInfo.put("addresses",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), addresses, false, true));
                break;
            case "address":
                addresses.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentAddress, false, true));
                break;
            case "region":
                break;
            case "details":
                break;
            case "contacts":
                break;
            case "contact":
                organizationInfo.put("contacts",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), contacts, false, true));
                break;
            case "names":
                break;
            case "head":
                break;
            case "legal_form":
                break;
            case "enterprise_type":
                break;
            case "econ_trade":
                break;
            case "is_se":
                break;
            case "docs":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

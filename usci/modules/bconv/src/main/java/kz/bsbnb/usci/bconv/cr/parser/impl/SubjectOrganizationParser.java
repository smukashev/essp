package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
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

    private MetaClass refCountryMeta, refOffshoreMeta, bankRelationMeta, refBankRelationMeta, addressMeta, refRegionMeta,
                    contactMeta, refContactType, organizationNameMeta, refLegalFormMeta, refEnterpriseTypeMeta, refEconTradeMeta,
                    documentMeta;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("subject"), batch.getRepDate(), creditorId);

        organizationInfo = new BaseEntity(metaClassRepository.getMetaClass("organization_info"), batch.getRepDate(), creditorId);

        bankRelations = null;
        addresses = null;
        contacts = null;
        currentContact = null;
        currentAddress = null;

        refCountryMeta = metaClassRepository.getMetaClass("ref_country");
        refOffshoreMeta = metaClassRepository.getMetaClass("ref_offshore");
        bankRelationMeta = metaClassRepository.getMetaClass("bank_relation");
        refBankRelationMeta = metaClassRepository.getMetaClass("ref_bank_relation");
        addressMeta = metaClassRepository.getMetaClass("address");
        refRegionMeta = metaClassRepository.getMetaClass("ref_region");
        contactMeta = metaClassRepository.getMetaClass("contact");
        refContactType = metaClassRepository.getMetaClass("ref_contact_type");
        organizationNameMeta = metaClassRepository.getMetaClass("organization_name");
        refLegalFormMeta = metaClassRepository.getMetaClass("ref_legal_form");
        refEnterpriseTypeMeta = metaClassRepository.getMetaClass("ref_enterprise_type");
        refEconTradeMeta = metaClassRepository.getMetaClass("ref_econ_trade");
        documentMeta = metaClassRepository.getMetaClass("document");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "organization":
                break;
            case "country":
                event = (XMLEvent) xmlReader.next();

                BaseEntity country = new BaseEntity(refCountryMeta,
                        batch.getRepDate(), creditorId);

                country.put("code_numeric",
                        new BaseEntityIntegerValue(0, creditorId, batch.getRepDate(),
                                new Integer(trim(event.asCharacters().getData())), false, true));

                organizationInfo.put("country",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), country, false, true));
                break;
            case "offshore":
                event = (XMLEvent) xmlReader.next();
                BaseEntity offshore = new BaseEntity(refOffshoreMeta, batch.getRepDate(), creditorId);

                offshore.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                organizationInfo.put("offshore",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), offshore, false, true));
                break;
            case "bank_relations":
                bankRelations = new BaseSet(bankRelationMeta, creditorId);
                break;
            case "bank_relation":
                event = (XMLEvent) xmlReader.next();

                BaseEntity bankRelation = new BaseEntity(bankRelationMeta,
                        batch.getRepDate(), creditorId);

                BaseEntity refBankRelation = new BaseEntity(refBankRelationMeta, batch.getRepDate(), creditorId);

                refBankRelation.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                bankRelation.put("bank_relation",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), refBankRelation, false, true));
                bankRelations.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), bankRelation, false, true));
                break;
            case "addresses":
                addresses = new BaseSet(addressMeta, creditorId);

                break;
            case "address":
                currentAddress = new BaseEntity(addressMeta, batch.getRepDate(), creditorId);

                currentAddress.put("type",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asStartElement().getAttributeByName(new QName("type")).getValue(), false, true));
                break;
            case "region":
                BaseEntity region = new BaseEntity(refRegionMeta, batch.getRepDate(), creditorId);

                event = (XMLEvent) xmlReader.next();

                region.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                currentAddress.put("region", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), region, false, true));
                break;
            case "details":
                event = (XMLEvent) xmlReader.next();
                currentAddress.put("details",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));
                break;
            case "contacts":
                contacts = new BaseSet(contactMeta, creditorId);
                break;
            case "contact":
                currentContact = new BaseEntity(contactMeta, batch.getRepDate(), creditorId);

                BaseEntity contactType = new BaseEntity(refContactType, new Date(), creditorId);

                contactType.put("code", new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                        event.asStartElement().getAttributeByName(new QName("contact_type")).getValue(), false, true));

                currentContact.put("contact_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), contactType, false, true));

                BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING), creditorId);

                event = (XMLEvent) xmlReader.next();

                contactDetails.put(new BaseSetStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                currentContact.put("details",
                        new BaseEntitySimpleSet(0, creditorId, batch.getRepDate(), contactDetails, false, true));

                contacts.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentContact, false, true));
                break;
            case "names":
                BaseSet organizationNames = new BaseSet(organizationNameMeta, creditorId);

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
                BaseEntity legalForm = new BaseEntity(refLegalFormMeta, batch.getRepDate(), creditorId);

                event = (XMLEvent) xmlReader.next();

                legalForm.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                organizationInfo.put("legal_form",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), legalForm, false, true));
                break;
            case "enterprise_type":
                event = (XMLEvent) xmlReader.next();

                BaseEntity enterpriseType = new BaseEntity(refEnterpriseTypeMeta, batch.getRepDate(), creditorId);

                enterpriseType.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                organizationInfo.put("enterprise_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), enterpriseType, false, true));
                break;
            case "econ_trade":
                event = (XMLEvent) xmlReader.next();

                BaseEntity econTrade = new BaseEntity(refEconTradeMeta, batch.getRepDate(), creditorId);

                econTrade.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                organizationInfo.put("econ_trade", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), econTrade, false, true));
                break;
            case "is_se":
                event = (XMLEvent) xmlReader.next();

                organizationInfo.put("is_se",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(),
                                (Boolean)DataTypes.getCastObject(DataTypes.BOOLEAN, trim(event.asCharacters().getData())), false, true));
                break;
            case "docs":
                BaseSet organizationDocs = new BaseSet(documentMeta, creditorId);

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

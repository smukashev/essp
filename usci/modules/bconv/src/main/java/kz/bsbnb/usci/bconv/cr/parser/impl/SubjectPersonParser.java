package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
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

@Component
@Scope("prototype")
public class SubjectPersonParser extends BatchParser {
    @Autowired
    private SubjectPersonDocsParser subjectPersonDocsParser;

    public SubjectPersonParser() {
        super();
    }

    private BaseSet names;
    private BaseEntity currentName;

    private BaseSet addresses;
    private BaseEntity currentAddress;

    private BaseSet contacts;

    private BaseSet bankRelations;

    private BaseEntity personInfo;

    private MetaClass refCountryMeta, refOffshoreMeta, bankRelationMeta, refBankRelationMeta, addressMeta,
                        refRegionMeta, contactMeta, refContactTypeMeta, personNameMeta, documentMeta;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("subject"), batch.getRepDate(), creditorId);
        personInfo = new BaseEntity(metaClassRepository.getMetaClass("person_info"), batch.getRepDate(), creditorId);

        names = null;
        currentName = null;
        addresses = null;
        currentAddress = null;
        bankRelations = null;

        refCountryMeta = metaClassRepository.getMetaClass("ref_country");
        refOffshoreMeta = metaClassRepository.getMetaClass("ref_offshore");
        bankRelationMeta = metaClassRepository.getMetaClass("bank_relation");
        refBankRelationMeta = metaClassRepository.getMetaClass("ref_bank_relation");
        addressMeta = metaClassRepository.getMetaClass("address");
        refRegionMeta = metaClassRepository.getMetaClass("ref_region");
        contactMeta = metaClassRepository.getMetaClass("contact");
        refContactTypeMeta = metaClassRepository.getMetaClass("ref_contact_type");
        personNameMeta = metaClassRepository.getMetaClass("person_name");
        documentMeta = metaClassRepository.getMetaClass("document");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        switch (localName) {
            case "person":
                break;
            case "country":
                event = (XMLEvent) xmlReader.next();
                BaseEntity country = new BaseEntity(refCountryMeta,
                        batch.getRepDate(), creditorId);

                country.put("code_numeric",
                        new BaseEntityIntegerValue(0, creditorId, batch.getRepDate(),
                                new Integer(trim(event.asCharacters().getData())), false, true));

                personInfo.put("country",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), country, false, true));
                break;
            case "offshore":
                event = (XMLEvent) xmlReader.next();

                BaseEntity ref_offshore = new BaseEntity(refOffshoreMeta, batch.getRepDate(), creditorId);

                ref_offshore.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));

                personInfo.put("offshore",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), ref_offshore, false, true));
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
                event = (XMLEvent) xmlReader.next();
                BaseEntity region = new BaseEntity(refRegionMeta, batch.getRepDate(), creditorId);

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
                BaseEntity currentContact = new BaseEntity(contactMeta,
                        batch.getRepDate(), creditorId);

                BaseEntity contactType = new BaseEntity(refContactTypeMeta, batch.getRepDate(), creditorId);

                contactType.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
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
                names = new BaseSet(personNameMeta, creditorId);
                break;
            case "name":
                currentName = new BaseEntity(personNameMeta, batch.getRepDate(), creditorId);

                currentName.put("lang",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asStartElement().getAttributeByName(new QName("lang")).getValue(), false, true));
                break;
            case "firstname":
                event = (XMLEvent) xmlReader.next();
                currentName.put("firstname",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));
                break;
            case "lastname":
                event = (XMLEvent) xmlReader.next();
                currentName.put("lastname",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));
                break;
            case "middlename":
                event = (XMLEvent) xmlReader.next();
                currentName.put("middlename",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));
                break;
            case "docs":
                BaseSet personDocs = new BaseSet(documentMeta, creditorId);

                while (true) {
                    subjectPersonDocsParser.parse(xmlReader, batch, index, creditorId);

                    if (subjectPersonDocsParser.hasMore()) {
                        personDocs.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(),
                                subjectPersonDocsParser.getCurrentBaseEntity(), false, true));
                    } else break;
                }

                currentBaseEntity.put("docs",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), personDocs, false, true));
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "person":
                currentBaseEntity.put("person_info",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), personInfo, false, true));

                currentBaseEntity.put("is_person",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), true, true, false));

                currentBaseEntity.put("is_organization",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), false, true, false));

                currentBaseEntity.put("is_creditor",
                        new BaseEntityBooleanValue(0, creditorId, batch.getRepDate(), false, true, false));
                return true;
            case "country":
                break;
            case "offshore":
                break;
            case "bank_relations":
                personInfo.put("bank_relations",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), bankRelations, false, true));
                break;
            case "bank_relation":
                break;
            case "addresses":
                personInfo.put("addresses",
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
                personInfo.put("contacts",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), contacts, false, true));
                break;
            case "contact":
                break;
            case "names":
                personInfo.put("names",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), names, false, true));
                break;
            case "name":
                names.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentName, false, true));
                break;
            case "firstname":
                break;
            case "lastname":
                break;
            case "middlename":
                break;
            case "docs":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

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
    private BaseEntity currentContact;

    private BaseSet bankRelations;

    private BaseEntity personInfo;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("subject"), batch.getRepDate());
        personInfo = new BaseEntity(metaClassRepository.getMetaClass("person_info"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if (localName.equals("person")) {
        } else if (localName.equals("country")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity country = new BaseEntity(metaClassRepository.getMetaClass("ref_country"), batch.getRepDate());

            country.put("code_numeric", new BaseEntityIntegerValue(0, -1, batch.getRepDate(),
                    new Integer(event.asCharacters().getData()), false, true));

            personInfo.put("country", new BaseEntityComplexValue(0, -1, batch.getRepDate(), country, false, true));
        } else if (localName.equals("offshore")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity ref_offshore = new BaseEntity(metaClassRepository.getMetaClass("ref_offshore"), batch.getRepDate());
            ref_offshore.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
            personInfo.put("offshore", new BaseEntityComplexValue(0, -1, batch.getRepDate(), ref_offshore, false, true));
        } else if (localName.equals("bank_relations")) {
            bankRelations = new BaseSet(metaClassRepository.getMetaClass("bank_relation"));
        } else if (localName.equals("bank_relation")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity bankRelation = new BaseEntity(metaClassRepository.getMetaClass("bank_relation"), batch.getRepDate());
            BaseEntity refBankRelation = new BaseEntity(metaClassRepository.getMetaClass("ref_bank_relation"), batch.getRepDate());
            refBankRelation.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(), false, true));
            bankRelation.put("bank_relation", new BaseEntityComplexValue(0, -1, batch.getRepDate(), refBankRelation, false, true));
            bankRelations.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), bankRelation, false, true));
        } else if (localName.equals("addresses")) {
            addresses = new BaseSet(metaClassRepository.getMetaClass("address"));
        } else if (localName.equals("address")) {
            currentAddress = new BaseEntity(metaClassRepository.getMetaClass("address"), batch.getRepDate());

            currentAddress.put("type", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asStartElement().getAttributeByName(new QName("type")).getValue(), false, true));
        } else if (localName.equals("region")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity region = new BaseEntity(metaClassRepository.getMetaClass("ref_region"), batch.getRepDate());

            region.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(), false, true));

            currentAddress.put("region", new BaseEntityComplexValue(0, -1, batch.getRepDate(), region, false, true));
        } else if (localName.equals("details")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("details", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(), false, true));
        } else if (localName.equals("contacts")) {
            contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));
        } else if (localName.equals("contact")) {
            currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), batch.getRepDate());

            BaseEntity contactType = new BaseEntity(metaClassRepository.getMetaClass("ref_contact_type"), batch.getRepDate());

            contactType.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asStartElement().getAttributeByName(new QName("contact_type")).getValue(), false, true));

            currentContact.put("contact_type", new BaseEntityComplexValue(0, -1, batch.getRepDate(), contactType, false, true));

            BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

            event = (XMLEvent) xmlReader.next();
            contactDetails.put(new BaseSetStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(), false, true));

            // TODO incorrect set attribute name
            currentContact.put("details", new BaseEntitySimpleSet(0, -1, batch.getRepDate(), contactDetails, false, true));

            contacts.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), currentContact, false, true));
        } else if (localName.equals("names")) {
            names = new BaseSet(metaClassRepository.getMetaClass("person_name"));
        } else if (localName.equals("name")) {
            currentName = new BaseEntity(metaClassRepository.getMetaClass("person_name"), batch.getRepDate());

            currentName.put("lang", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asStartElement().getAttributeByName(new QName("lang")).getValue(), false, true));
        } else if (localName.equals("firstname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("firstname", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else if (localName.equals("lastname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("lastname", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(), false, true));
        } else if (localName.equals("middlename")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("middlename", new BaseEntityStringValue(0, -1, batch.getRepDate(), event.asCharacters().getData(), false, true));
        } else if (localName.equals("docs")) {
            BaseSet personDocs = new BaseSet(metaClassRepository.getMetaClass("document"));

            while (true) {
                subjectPersonDocsParser.parse(xmlReader, batch, index);
                if (subjectPersonDocsParser.hasMore()) {
                    personDocs.put(new BaseSetComplexValue(0, -1, batch.getRepDate(),
                            subjectPersonDocsParser.getCurrentBaseEntity(), false, true));
                } else {
                    break;
                }
            }

            currentBaseEntity.put("docs", new BaseEntityComplexSet(0, -1, batch.getRepDate(), personDocs, false, true));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("person")) {
            currentBaseEntity.put("person_info", new BaseEntityComplexValue(0, -1, batch.getRepDate(), personInfo, false, true));
            currentBaseEntity.put("is_person", new BaseEntityBooleanValue(0, -1, batch.getRepDate(), true, true, false));
            currentBaseEntity.put("is_organization", new BaseEntityBooleanValue(0, -1, batch.getRepDate(), false, true, false));
            currentBaseEntity.put("is_creditor", new BaseEntityBooleanValue(0, -1, batch.getRepDate(), false, true, false));
            return true;
        } else if (localName.equals("country")) {
        } else if (localName.equals("offshore")) {
        } else if (localName.equals("bank_relations")) {
            personInfo.put("bank_relations", new BaseEntityComplexSet(0, -1, batch.getRepDate(), bankRelations, false, true));
        } else if (localName.equals("bank_relation")) {
        } else if (localName.equals("addresses")) {
            personInfo.put("addresses", new BaseEntityComplexSet(0, -1, batch.getRepDate(), addresses, false, true));
        } else if (localName.equals("address")) {
            addresses.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), currentAddress, false, true));
        } else if (localName.equals("region")) {
        } else if (localName.equals("details")) {
        } else if (localName.equals("contacts")) {
            personInfo.put("contacts", new BaseEntityComplexSet(0, -1, batch.getRepDate(), contacts, false, true));
        } else if (localName.equals("contact")) {
        } else if (localName.equals("names")) {
            personInfo.put("names", new BaseEntityComplexSet(0, -1, batch.getRepDate(), names, false, true));
        } else if (localName.equals("name")) {
            names.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), currentName, false, true));
        } else if (localName.equals("firstname")) {
        } else if (localName.equals("lastname")) {
        } else if (localName.equals("middlename")) {
        } else if (localName.equals("docs")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

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

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("organization"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if (localName.equals("organization")) {
        } else if (localName.equals("country")) {
            event = (XMLEvent) xmlReader.next();

            BaseEntity country = new BaseEntity(metaClassRepository.getMetaClass("ref_country"), batch.getRepDate());

            country.put("code_numeric", new BaseEntityIntegerValue(-1, batch, index,
                    new Integer(event.asCharacters().getData())));

            currentBaseEntity.put("country", new BaseEntityComplexValue(-1, batch, index, country));
        } else if (localName.equals("offshore")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity offshore = new BaseEntity(metaClassRepository.getMetaClass("ref_offshore"), batch.getRepDate());
            currentBaseEntity.put("offshore", new BaseEntityComplexValue(-1, batch, index, offshore));
        } else if (localName.equals("bank_relations")) {
            bankRelations = new BaseSet(metaClassRepository.getMetaClass("bank_relation"));
        } else if (localName.equals("bank_relation")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity bankRelation = new BaseEntity(metaClassRepository.getMetaClass("bank_relation"),
                    batch.getRepDate());

            BaseEntity refBankRelation = new BaseEntity(metaClassRepository.getMetaClass("ref_bank_relation"),
                    batch.getRepDate());

            refBankRelation.put("code", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
            bankRelation.put("bank_relation", new BaseEntityComplexValue(-1, batch, index, refBankRelation));
            bankRelations.put(new BaseSetComplexValue(-1, batch, index, bankRelation));
        } else if (localName.equals("addresses")) {
            addresses = new BaseSet(metaClassRepository.getMetaClass("address"));

        } else if (localName.equals("address")) {
            currentAddress = new BaseEntity(metaClassRepository.getMetaClass("address"), batch.getRepDate());
            currentAddress.put("type", new BaseEntityStringValue(-1, batch, index,
                    event.asStartElement().getAttributeByName(new QName("type")).getValue()));

        } else if (localName.equals("region")) {
            BaseEntity region = new BaseEntity(metaClassRepository.getMetaClass("ref_region"), batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            region.put("code", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
            currentAddress.put("region", new BaseEntityComplexValue(-1, batch, index, region));
        } else if (localName.equals("details")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("details", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
        } else if (localName.equals("contacts")) {
            //contacts = new Contacts();
            contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));

        } else if (localName.equals("contact")) {
            //ctContact = new CtContact();
            //ctContact.setContactType(attributes.getValue("contact_type"));
            currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), batch.getRepDate());

            BaseEntity contactType = new BaseEntity(metaClassRepository.getMetaClass("ref_contact_type"), new Date());

            contactType.put("code", new BaseEntityStringValue(-1, batch, index,
                    event.asStartElement().getAttributeByName(new QName("contact_type")).getValue()));

            currentContact.put("contact_type", new BaseEntityComplexValue(-1, batch, index, contactType));

            BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

            event = (XMLEvent) xmlReader.next();
            contactDetails.put(new BaseSetStringValue(-1, batch, index, event.asCharacters().getData()));
            currentContact.put("details", new BaseEntitySimpleSet(-1, batch, index, contactDetails));

            contacts.put(new BaseSetComplexValue(-1, batch, index, currentContact));
        } else if (localName.equals("names")) {
            BaseSet organizationNames = new BaseSet(metaClassRepository.getMetaClass("organization_name"));

            while (true) {
                subjectOrganizationNamesParser.parse(xmlReader, batch, index);
                if (subjectOrganizationNamesParser.hasMore()) {
                    organizationNames.put(new BaseSetComplexValue(-1, batch, index,
                            subjectOrganizationNamesParser.getCurrentBaseEntity()));
                } else break;
            }
            currentBaseEntity.put("names", new BaseEntityComplexSet(-1, batch, index, organizationNames));
        } else if (localName.equals("head")) {
            subjectOrganizationHeadParser.parse(xmlReader, batch, index);

            currentBaseEntity.put("head", new BaseEntityComplexValue(-1, batch, index,
                    subjectOrganizationHeadParser.getCurrentBaseEntity()));
        } else if (localName.equals("legal_form")) {
            BaseEntity legalForm = new BaseEntity(metaClassRepository.getMetaClass("ref_legal_form"),
                    batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            legalForm.put("code", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
            currentBaseEntity.put("legal_form", new BaseEntityComplexValue(-1, batch, index, legalForm));
        } else if (localName.equals("enterprise_type")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity enterpriseType = new BaseEntity(metaClassRepository.getMetaClass("ref_enterprise_type"),
                    batch.getRepDate());
            enterpriseType.put("code", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));

            currentBaseEntity.put("enterprise_type", new BaseEntityComplexValue(-1, batch, index, enterpriseType));
        } else if (localName.equals("econ_trade")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity econTrade = new BaseEntity(metaClassRepository.getMetaClass("ref_econ_trade"), batch.getRepDate());
            econTrade.put("code", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
            currentBaseEntity.put("econ_trade", new BaseEntityComplexValue(-1, batch, index, econTrade));
        } else if (localName.equals("is_se")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("is_se", new BaseEntityBooleanValue(-1, batch, index,
                    new Boolean(event.asCharacters().getData())));
        } else if (localName.equals("docs")) {
            BaseSet organizationDocs = new BaseSet(metaClassRepository.getMetaClass("document"));

            while (true) {
                subjectOrganizationDocsParser.parse(xmlReader, batch, index);
                if (subjectOrganizationDocsParser.hasMore()) {
                    organizationDocs.put(new BaseSetComplexValue(-1, batch, index,
                            subjectOrganizationDocsParser.getCurrentBaseEntity()));
                } else break;
            }


            currentBaseEntity.put("docs", new BaseEntityComplexSet(-1, batch, index, organizationDocs));

        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("organization")) {
            return true;
        } else if (localName.equals("country")) {
        } else if (localName.equals("offshore")) {
        } else if (localName.equals("bank_relations")) {
            currentBaseEntity.put("bank_relations", new BaseEntityComplexSet(-1, batch, index, bankRelations));
        } else if (localName.equals("bank_relation")) {
        } else if (localName.equals("addresses")) {
            currentBaseEntity.put("addresses", new BaseEntityComplexSet(-1, batch, index, addresses));
        } else if (localName.equals("address")) {
            addresses.put(new BaseSetComplexValue(-1, batch, index, currentAddress));
        } else if (localName.equals("region")) {
        } else if (localName.equals("details")) {
        } else if (localName.equals("contacts")) {
        } else if (localName.equals("contact")) {
            currentBaseEntity.put("contacts", new BaseEntityComplexSet(-1, batch, index, contacts));
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

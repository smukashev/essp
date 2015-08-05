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

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("person"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if (localName.equals("person")) {
        } else if (localName.equals("country")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity country = new BaseEntity(metaClassRepository.getMetaClass("ref_country"), batch.getRepDate());

            country.put("code_numeric", new BaseEntityIntegerValue(-1, batch, index,
                    new Integer(event.asCharacters().getData())));

            currentBaseEntity.put("country", new BaseEntityComplexValue(-1, batch, index, country));
        } else if (localName.equals("offshore")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("offshore", new BaseEntityStringValue(-1, batch, index,
                    event.asCharacters().getData()
            ));
        } else if (localName.equals("bank_relations")) {
            bankRelations = new BaseSet(metaClassRepository.getMetaClass("bank_relation"));
        } else if (localName.equals("bank_relation")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity bankRelation = new BaseEntity(metaClassRepository.getMetaClass("bank_relation"), batch.getRepDate());
            BaseEntity refBankRelation = new BaseEntity(metaClassRepository.getMetaClass("ref_bank_relation"), batch.getRepDate());
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
            event = (XMLEvent) xmlReader.next();
            BaseEntity region = new BaseEntity(metaClassRepository.getMetaClass("ref_region"), batch.getRepDate());

            region.put("code", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));

            currentAddress.put("region", new BaseEntityComplexValue(-1, batch, index,
                    region
            ));
        } else if (localName.equals("details")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("details", new BaseEntityStringValue(-1, batch, index,
                    event.asCharacters().getData()
            ));
        } else if (localName.equals("contacts")) {
            contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));
        } else if (localName.equals("contact")) {
            currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), batch.getRepDate());

            BaseEntity contactType = new BaseEntity(metaClassRepository.getMetaClass("ref_contact_type"), batch.getRepDate());

            contactType.put("code", new BaseEntityStringValue(-1, batch, index,
                    event.asStartElement().getAttributeByName(new QName("contact_type")).getValue()));

            currentContact.put("contact_type", new BaseEntityComplexValue(-1, batch, index, contactType));

            BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

            event = (XMLEvent) xmlReader.next();
            contactDetails.put(new BaseSetStringValue(-1, batch, index, event.asCharacters().getData()));

            // TODO incorrect set attribute name
            currentContact.put("details", new BaseEntitySimpleSet(-1, batch, index,
                    contactDetails
            ));

            contacts.put(new BaseSetComplexValue(-1, batch, index, currentContact));
        } else if (localName.equals("names")) {
            names = new BaseSet(metaClassRepository.getMetaClass("person_name"));
        } else if (localName.equals("name")) {
            currentName = new BaseEntity(metaClassRepository.getMetaClass("person_name"), batch.getRepDate());

            currentName.put("lang", new BaseEntityStringValue(-1, batch, index,
                    event.asStartElement().getAttributeByName(new QName("lang")).getValue()));
        } else if (localName.equals("firstname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("firstname", new BaseEntityStringValue(-1, batch, index,
                    event.asCharacters().getData()
            ));
        } else if (localName.equals("lastname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("lastname", new BaseEntityStringValue(-1, batch, index,
                    event.asCharacters().getData()
            ));
        } else if (localName.equals("middlename")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("middlename", new BaseEntityStringValue(-1, batch, index, event.asCharacters().getData()));
        } else if (localName.equals("docs")) {
            BaseSet personDocs = new BaseSet(metaClassRepository.getMetaClass("document"));

            while (true) {
                subjectPersonDocsParser.parse(xmlReader, batch, index);
                if (subjectPersonDocsParser.hasMore()) {
                    personDocs.put(new BaseSetComplexValue(-1, batch, index,
                            subjectPersonDocsParser.getCurrentBaseEntity()));
                } else {
                    break;
                }
            }

            currentBaseEntity.put("docs", new BaseEntityComplexSet(-1, batch, index, personDocs));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("person")) {
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
            currentBaseEntity.put("contacts", new BaseEntityComplexSet(-1, batch, index, contacts));
        } else if (localName.equals("contact")) {
        } else if (localName.equals("names")) {
            currentBaseEntity.put("names", new BaseEntityComplexSet(-1, batch, index, names));
        } else if (localName.equals("name")) {
            names.put(new BaseSetComplexValue(-1, batch, index, currentName));
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

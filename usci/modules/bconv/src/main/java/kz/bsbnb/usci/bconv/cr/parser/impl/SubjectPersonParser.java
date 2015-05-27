package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
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
    public void init()
    {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("person"), batch.getRepDate());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("person")) {
        } else if(localName.equals("country")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity country = new BaseEntity(metaClassRepository.getMetaClass("ref_country"), batch.getRepDate());

            country.put("code_numeric", new BaseEntityIntegerValue(batch, index, new Integer(event.asCharacters().getData())));

            currentBaseEntity.put("country", new BaseEntityComplexValue(batch, index, country));
        } else if(localName.equals("offshore")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("offshore", new BaseEntityStringValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("bank_relations")) {
            //bankRelations = new CtEntity.BankRelations();
            bankRelations = new BaseSet(metaClassRepository.getMetaClass("bank_relation"));
        } else if(localName.equals("bank_relation")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity bankRelation = new BaseEntity(metaClassRepository.getMetaClass("bank_relation"), batch.getRepDate());
            BaseEntity refBankRelation = new BaseEntity(metaClassRepository.getMetaClass("ref_bank_relation"),batch.getRepDate());
            refBankRelation.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
            bankRelation.put("bank_relation", new BaseEntityComplexValue(batch, index, refBankRelation));
            bankRelations.put(new BaseSetComplexValue(batch, index, bankRelation));
        } else if(localName.equals("addresses")) {
            //addresses = new CtEntity.Addresses();
            addresses = new BaseSet(metaClassRepository.getMetaClass("address"));
        } else if(localName.equals("address")) {
            /*ctAddress = new CtAddress();

            if(attributes.getValue("type").equals("FA")) {
                ctAddress.setType(StAddressType.FA);
            } else if(attributes.getValue("type").equals("RA")) {
                ctAddress.setType(StAddressType.RA);
            } else {
                throw new UnknownValException(localName, attributes.getValue("type"));
            } */
            currentAddress = new BaseEntity(metaClassRepository.getMetaClass("address"), batch.getRepDate());

            currentAddress.put("type", new BaseEntityStringValue(batch, index,
                    event.asStartElement().getAttributeByName(new QName("type")).getValue()));
        } else if(localName.equals("region")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity region = new BaseEntity(metaClassRepository.getMetaClass("ref_region"), batch.getRepDate());

            region.put("code", new BaseEntityStringValue(batch, index, event.asCharacters().getData()));

            currentAddress.put("region", new BaseEntityComplexValue(batch, index,
                    region
                ));
        } else if(localName.equals("details")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("details", new BaseEntityStringValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("contacts")) {
            //contacts = new Contacts();
            contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));
        } else if(localName.equals("contact")) {
            //ctContact = new CtContact();
            //ctContact.setContactType(attributes.getValue("contact_type"));
            currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), batch.getRepDate());

            BaseEntity contactType = new BaseEntity(metaClassRepository.getMetaClass("ref_contact_type"), batch.getRepDate());

            contactType.put("code", new BaseEntityStringValue(batch, index,
                    event.asStartElement().getAttributeByName(new QName("contact_type")).getValue()));

            currentContact.put("contact_type", new BaseEntityComplexValue(batch, index, contactType));

            BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

            event = (XMLEvent) xmlReader.next();
            contactDetails.put(new BaseSetStringValue(batch, index, event.asCharacters().getData()));

            // TODO incorrect set attribute name
            currentContact.put("details", new BaseEntitySimpleSet(batch, index,
                    contactDetails
                ));

            contacts.put(new BaseSetComplexValue(batch, index, currentContact));
        } else if(localName.equals("names")) {
            //names = new CtPerson.Names();
            names = new BaseSet(metaClassRepository.getMetaClass("person_name"));
        } else if(localName.equals("name")) {
            /*name = new CtPerson.Names.Name();

            if(attributes.getValue("lang") != null &&
                    attributes.getValue("lang").equals("RU")) {
                name.setLang(StLang.RU);
            } else if(attributes.getValue("lang").equals("EN")) {
                name.setLang(StLang.EN);
            } else if(attributes.getValue("lang").equals("KZ")) {
                name.setLang(StLang.KZ);
            } else {
                throw new UnknownValException(localName, attributes.getValue("lang"));
            } */
            currentName = new BaseEntity(metaClassRepository.getMetaClass("person_name"), batch.getRepDate());

            currentName.put("lang", new BaseEntityStringValue(batch, index,
                    event.asStartElement().getAttributeByName(new QName("lang")).getValue()));
        } else if(localName.equals("firstname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("firstname", new BaseEntityStringValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("lastname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("lastname", new BaseEntityStringValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("middlename")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("middlename", new BaseEntityStringValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("docs")) {
            BaseSet personDocs = new BaseSet(metaClassRepository.getMetaClass("document"));

            while(true) {
                subjectPersonDocsParser.parse(xmlReader, batch, index);
                if (subjectPersonDocsParser.hasMore()) {
                    personDocs.put(new BaseSetComplexValue(batch, index, subjectPersonDocsParser.getCurrentBaseEntity()));
                } else {
                    break;
                }
            }

            currentBaseEntity.put("docs", new BaseEntityComplexSet(batch, index, personDocs));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }   
    
    @Override
    public boolean endElement(String localName) throws SAXException
    {
        if(localName.equals("person")) {
            //currentSubject.setPerson(ctPerson);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("country")) {
            //ctPerson.setCountry(contents.toString());
        } else if(localName.equals("offshore")) {
            //ctPerson.setOffshore(contents.toString());
        } else if(localName.equals("bank_relations")) {
            //ctPerson.setBankRelations(bankRelations);
            currentBaseEntity.put("bank_relations", new BaseEntityComplexSet(batch, index, bankRelations));
        } else if(localName.equals("bank_relation")) {
            //bankRelations.getBankRelation().add(contents.toString());
        } else if(localName.equals("addresses")) {
            //ctPerson.setAddresses(addresses);
            currentBaseEntity.put("addresses", new BaseEntityComplexSet(batch, index, addresses));
        } else if(localName.equals("address")) {
            //addresses.getAddress().add(ctAddress);
            addresses.put(new BaseSetComplexValue(batch, index, currentAddress));
        } else if(localName.equals("region")) {
            //ctAddress.setRegion(contents.toString());
        } else if(localName.equals("details")) {
            //ctAddress.setDetails(contents.toString());
        } else if(localName.equals("contacts")) {
            //ctPerson.setContacts(contacts);
            currentBaseEntity.put("contacts", new BaseEntityComplexSet(batch, index, contacts));
        } else if(localName.equals("contact")) {
            //ctContact.setValue(contents.toString());
            //contacts.getContact().add(ctContact);
        } else if(localName.equals("names")) {
            //ctPerson.setNames(names);
            currentBaseEntity.put("names", new BaseEntityComplexSet(batch, index, names));
        } else if(localName.equals("name")) {
            //names.getName().add(name);
            names.put(new BaseSetComplexValue(batch, index, currentName));
        } else if(localName.equals("firstname")) {
            //name.setFirstname(contents.toString());
        } else if(localName.equals("lastname")) {
            //name.setLastname(contents.toString());
        } else if(localName.equals("middlename")) {
            //name.setMiddlename(contents.toString());
        } else if(localName.equals("docs")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

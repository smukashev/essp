package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
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
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("person"), new Date());
        names = new BaseSet(metaClassRepository.getMetaClass("name"));
        addresses = new BaseSet(metaClassRepository.getMetaClass("address"));
        bankRelations = new BaseSet(new MetaValue(DataTypes.STRING));
        contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("person")) {
        } else if(localName.equals("country")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("country", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("offshore")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("offshore", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("bank_relations")) {
            //bankRelations = new CtEntity.BankRelations();
        } else if(localName.equals("bank_relation")) {
            event = (XMLEvent) xmlReader.next();
            bankRelations.put(new BaseValue(batch, index, event.asCharacters().getData()));
        } else if(localName.equals("addresses")) {
            //addresses = new CtEntity.Addresses();
        } else if(localName.equals("address")) {
            /*ctAddress = new CtAddress();

            if(attributes.getValue("type").equals("FA")) {
                ctAddress.setType(StAddressType.FA);
            } else if(attributes.getValue("type").equals("RA")) {
                ctAddress.setType(StAddressType.RA);
            } else {
                throw new UnknownValException(localName, attributes.getValue("type"));
            } */
            currentAddress = new BaseEntity(metaClassRepository.getMetaClass("address"), new Date());

            currentAddress.put("type", new BaseValue(batch, index,
                    event.asStartElement().getAttributeByName(new QName("type")).getValue()));
        } else if(localName.equals("region")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("region", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("details")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("details", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("contacts")) {
            //contacts = new Contacts();
        } else if(localName.equals("contact")) {
            //ctContact = new CtContact();
            //ctContact.setContactType(attributes.getValue("contact_type"));
            currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), new Date());

            currentContact.put("contact_type", new BaseValue(batch, index,
                    event.asStartElement().getAttributeByName(new QName("contact_type")).getValue()));

            BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

            event = (XMLEvent) xmlReader.next();
            contactDetails.put(new BaseValue(batch, index, event.asCharacters().getData()));
            currentContact.put("st_contact_details", new BaseValue(batch, index,
                    contactDetails
                ));

            contacts.put(new BaseValue(batch, index, currentContact));
        } else if(localName.equals("names")) {
            //names = new CtPerson.Names();
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
            currentName = new BaseEntity(metaClassRepository.getMetaClass("name"), new Date());

            currentName.put("lang", new BaseValue(batch, index,
                    event.asStartElement().getAttributeByName(new QName("lang")).getValue()));
        } else if(localName.equals("firstname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("firstname", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("lastname")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("lastname", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("middlename")) {
            event = (XMLEvent) xmlReader.next();
            currentName.put("middlename", new BaseValue(batch, index,
                    event.asCharacters().getData()
                ));
        } else if(localName.equals("docs")) {
            BaseSet personDocs = new BaseSet(metaClassRepository.getMetaClass("doc1"));

            while(true) {
                subjectPersonDocsParser.parse(xmlReader, batch, index);
                if (subjectPersonDocsParser.hasMore()) {
                    personDocs.put(new BaseValue(batch, index, subjectPersonDocsParser.getCurrentBaseEntity()));
                } else {
                    break;
                }
            }

            currentBaseEntity.put("docs", new BaseValue(batch, index, personDocs));
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
            currentBaseEntity.put("bank_relations", new BaseValue(batch, index, bankRelations));
        } else if(localName.equals("bank_relation")) {
            //bankRelations.getBankRelation().add(contents.toString());
        } else if(localName.equals("addresses")) {
            //ctPerson.setAddresses(addresses);
            currentBaseEntity.put("addresses", new BaseValue(batch, index, addresses));
        } else if(localName.equals("address")) {
            //addresses.getAddress().add(ctAddress);
            addresses.put(new BaseValue(batch, index, currentAddress));
        } else if(localName.equals("region")) {
            //ctAddress.setRegion(contents.toString());
        } else if(localName.equals("details")) {
            //ctAddress.setDetails(contents.toString());
        } else if(localName.equals("contacts")) {
            //ctPerson.setContacts(contacts);
            currentBaseEntity.put("contacts", new BaseValue(batch, index, contacts));
        } else if(localName.equals("contact")) {
            //ctContact.setValue(contents.toString());
            //contacts.getContact().add(ctContact);
        } else if(localName.equals("names")) {
            //ctPerson.setNames(names);
            currentBaseEntity.put("names", new BaseValue(batch, index, names));
        } else if(localName.equals("name")) {
            //names.getName().add(name);
            names.put(new BaseValue(batch, index, currentName));
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

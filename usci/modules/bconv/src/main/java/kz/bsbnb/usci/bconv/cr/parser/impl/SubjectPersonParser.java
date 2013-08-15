package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

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
    
    @Override
    public void startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("person")) {
        } else if(localName.equals("country")) {
        } else if(localName.equals("offshore")) {
        } else if(localName.equals("bank_relations")) {
            //bankRelations = new CtEntity.BankRelations();
        } else if(localName.equals("bank_relation")) {
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
        } else if(localName.equals("region")) {
        } else if(localName.equals("details")) {
        } else if(localName.equals("contacts")) {
            //contacts = new Contacts();
        } else if(localName.equals("contact")) {
            //ctContact = new CtContact();
            //ctContact.setContactType(attributes.getValue("contact_type"));
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
        } else if(localName.equals("firstname")) {
        } else if(localName.equals("lastname")) {
        } else if(localName.equals("middlename")) {
        } else if(localName.equals("docs")) {
            subjectPersonDocsParser.parse(xmlReader, batch);
        } else {
            throw new UnknownTagException(localName);
        }
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
        } else if(localName.equals("bank_relation")) {
            //bankRelations.getBankRelation().add(contents.toString());
        } else if(localName.equals("addresses")) {
            //ctPerson.setAddresses(addresses);
        } else if(localName.equals("address")) {
            //addresses.getAddress().add(ctAddress);
        } else if(localName.equals("region")) {
            //ctAddress.setRegion(contents.toString());
        } else if(localName.equals("details")) {
            //ctAddress.setDetails(contents.toString());
        } else if(localName.equals("contacts")) {
            //ctPerson.setContacts(contacts);
        } else if(localName.equals("contact")) {
            //ctContact.setValue(contents.toString());
            //contacts.getContact().add(ctContact);
        } else if(localName.equals("names")) {
            //ctPerson.setNames(names);
        } else if(localName.equals("name")) {
            //names.getName().add(name);
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

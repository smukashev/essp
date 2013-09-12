package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.util.ParserUtils;
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
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("organization")) {
        } else if(localName.equals("country")) {
        } else if(localName.equals("offshore")) {
        } else if(localName.equals("bank_relations")) {
            //bankRelations = new BankRelations();
        } else if(localName.equals("bank_relation")) {
        } else if(localName.equals("addresses")) {
            //addresses = new Addresses();
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
            subjectOrganizationNamesParser.parse(xmlReader, batch, index);
        } else if(localName.equals("head")) {
            subjectOrganizationHeadParser.parse(xmlReader, batch, index);
        } else if(localName.equals("legal_form")) {
        } else if(localName.equals("enterprise_type")) {
        } else if(localName.equals("econ_trade")) {
        } else if(localName.equals("is_se")) {
        } else if(localName.equals("docs")) {
            subjectOrganizationDocsParser.parse(xmlReader, batch, index);
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }   
    
    @Override
    public boolean endElement(String localName) throws SAXException
    {
        if(localName.equals("organization")) {
            //currentSubject.setOrganization(ctOrganization);
            //xmlReader.setContentHandler(contentHandler);
            return true;
        } else if(localName.equals("country")) {
            //ctOrganization.setCountry(contents.toString());
        } else if(localName.equals("offshore")) {
            //ctOrganization.setOffshore(contents.toString());
        } else if(localName.equals("bank_relations")) {
            //ctOrganization.setBankRelations(bankRelations);
        } else if(localName.equals("bank_relation")) {
            //bankRelations.getBankRelation().add(contents.toString());
        } else if(localName.equals("addresses")) {
            //ctOrganization.setAddresses(addresses);
        } else if(localName.equals("address")) {
            //addresses.getAddress().add(ctAddress);
        } else if(localName.equals("region")) {
            //ctAddress.setRegion(contents.toString());
        } else if(localName.equals("details")) {
            //ctAddress.setDetails(contents.toString());
        } else if(localName.equals("contacts")) {
            //ctOrganization.setContacts(contacts);
        } else if(localName.equals("contact")) {
            //ctContact.setValue(contents.toString());
            //contacts.getContact().add(ctContact);
        } else if(localName.equals("names")) {
        } else if(localName.equals("head")) {
        } else if(localName.equals("legal_form")) {
            //ctOrganization.setLegalForm(contents.toString());
        } else if(localName.equals("enterprise_type")) {
            //ctOrganization.setEnterpriseType(contents.toString());
        } else if(localName.equals("econ_trade")) {
            //ctOrganization.setEconTrade(contents.toString());
        } else if(localName.equals("is_se")) {
            //ctOrganization.setIsSe(ParserUtils.parseBoolean(contents.toString()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

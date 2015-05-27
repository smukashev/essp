package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.util.ParserUtils;
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
    public void init()
    {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("organization"), batch.getRepDate());
    }


    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        if(localName.equals("organization")) {
        } else if(localName.equals("country")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity country = new BaseEntity(metaClassRepository.getMetaClass("ref_country"), batch.getRepDate());
            country.put("code_numeric", new BaseEntityIntegerValue(batch, index, new Integer(event.asCharacters().getData())));
            currentBaseEntity.put("country", new BaseEntityComplexValue(batch, index, country));
        } else if(localName.equals("offshore")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity offshore = new BaseEntity(metaClassRepository.getMetaClass("ref_offshore"), batch.getRepDate());
            currentBaseEntity.put("offshore", new BaseEntityComplexValue(batch, index, offshore));
        } else if(localName.equals("bank_relations")) {
            //bankRelations = new BankRelations();
            //my code
            bankRelations = new BaseSet(metaClassRepository.getMetaClass("bank_relation"));
        } else if(localName.equals("bank_relation")) {
            //my code
            event = (XMLEvent) xmlReader.next();
            BaseEntity bankRelation = new BaseEntity(metaClassRepository.getMetaClass("bank_relation"),batch.getRepDate());
            BaseEntity refBankRelation = new BaseEntity(metaClassRepository.getMetaClass("ref_bank_relation"),batch.getRepDate());
            refBankRelation.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
            bankRelation.put("bank_relation", new BaseEntityComplexValue(batch, index, refBankRelation));
            bankRelations.put(new BaseSetComplexValue(batch,index,bankRelation));
        } else if(localName.equals("addresses")) {
            //addresses = new Addresses();
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
            //my code
            currentAddress = new BaseEntity(metaClassRepository.getMetaClass("address"),batch.getRepDate());
            currentAddress.put("type",new BaseEntityStringValue(batch,index,
                    event.asStartElement().getAttributeByName(new QName("type")).getValue()));

        } else if(localName.equals("region")) {
            BaseEntity region = new BaseEntity(metaClassRepository.getMetaClass("ref_region"),batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            region.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
            currentAddress.put("region",new BaseEntityComplexValue(batch,index,region));
        } else if(localName.equals("details")) {
            event = (XMLEvent) xmlReader.next();
            currentAddress.put("details",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("contacts")) {
            //contacts = new Contacts();
            contacts = new BaseSet(metaClassRepository.getMetaClass("contact"));

        } else if(localName.equals("contact")) {
            //ctContact = new CtContact();
            //ctContact.setContactType(attributes.getValue("contact_type"));
            currentContact = new BaseEntity(metaClassRepository.getMetaClass("contact"), batch.getRepDate());

            BaseEntity contactType = new BaseEntity(metaClassRepository.getMetaClass("ref_contact_type"), new Date());

            contactType.put("code", new BaseEntityStringValue(batch, index,
                    event.asStartElement().getAttributeByName(new QName("contact_type")).getValue()));

            currentContact.put("contact_type", new BaseEntityComplexValue(batch, index, contactType));

            BaseSet contactDetails = new BaseSet(new MetaValue(DataTypes.STRING));

            event = (XMLEvent) xmlReader.next();
            contactDetails.put(new BaseSetStringValue(batch, index, event.asCharacters().getData()));
            currentContact.put("details", new BaseEntitySimpleSet(batch, index,
                    contactDetails
            ));

            contacts.put(new BaseSetComplexValue(batch, index, currentContact));
        } else if(localName.equals("names")) {
            BaseSet organizationNames = new BaseSet(metaClassRepository.getMetaClass("organization_name"));

            while(true){
                subjectOrganizationNamesParser.parse(xmlReader,batch,index);
                if(subjectOrganizationNamesParser.hasMore()){
                   organizationNames.put(new BaseSetComplexValue(batch,index,
                           subjectOrganizationNamesParser.getCurrentBaseEntity()));
                }else break;
            }
            currentBaseEntity.put("names",new BaseEntityComplexSet(batch,index,organizationNames));
        } else if(localName.equals("head")) {
            subjectOrganizationHeadParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("head",new BaseEntityComplexValue(batch,index,subjectOrganizationHeadParser.getCurrentBaseEntity()));
        } else if(localName.equals("legal_form")) {
            BaseEntity legalForm = new BaseEntity(metaClassRepository.getMetaClass("ref_legal_form"),batch.getRepDate());
            event = (XMLEvent) xmlReader.next();
            legalForm.put("code",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
            currentBaseEntity.put("legal_form",new BaseEntityComplexValue(batch,index,legalForm));
        } else if(localName.equals("enterprise_type")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity enterpriseType = new BaseEntity(metaClassRepository.getMetaClass("ref_enterprise_type"), batch.getRepDate());
            enterpriseType.put("code", new BaseEntityStringValue(batch, index,
                    event.asCharacters().getData()));

            currentBaseEntity.put("enterprise_type", new BaseEntityComplexValue(batch, index, enterpriseType));
        } else if(localName.equals("econ_trade")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity econTrade = new BaseEntity(metaClassRepository.getMetaClass("ref_econ_trade"), batch.getRepDate());
            econTrade.put("code", new BaseEntityStringValue(batch, index,
                    event.asCharacters().getData()));
            currentBaseEntity.put("econ_trade", new BaseEntityComplexValue(batch,index,econTrade));
        } else if(localName.equals("is_se")) {
            event = (XMLEvent) xmlReader.next();
            currentBaseEntity.put("is_se",new BaseEntityBooleanValue(batch,index,new Boolean(event.asCharacters().getData())));
        } else if(localName.equals("docs")) {
            BaseSet organizationDocs = new BaseSet(metaClassRepository.getMetaClass("document"));

            while(true){
               subjectOrganizationDocsParser.parse(xmlReader, batch, index);
               if(subjectOrganizationDocsParser.hasMore()){
                   organizationDocs.put(new BaseSetComplexValue(batch,index,
                           subjectOrganizationDocsParser.getCurrentBaseEntity()));
               }else break;
            }


            currentBaseEntity.put("docs",new BaseEntityComplexSet(batch,index,organizationDocs));

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
            //my code
            currentBaseEntity.put("bank_relations",new BaseEntityComplexSet(batch,index,bankRelations));
        } else if(localName.equals("bank_relation")) {
            //bankRelations.getBankRelation().add(contents.toString());
        } else if(localName.equals("addresses")) {
            //ctOrganization.setAddresses(addresses);
            currentBaseEntity.put("addresses",new BaseEntityComplexSet(batch,index,addresses));
        } else if(localName.equals("address")) {
            //addresses.getAddress().add(ctAddress);
            addresses.put(new BaseSetComplexValue(batch,index,currentAddress));
        } else if(localName.equals("region")) {
            //ctAddress.setRegion(contents.toString());
        } else if(localName.equals("details")) {
            //ctAddress.setDetails(contents.toString());
        } else if(localName.equals("contacts")) {
            //ctOrganization.setContacts(contacts);
        } else if(localName.equals("contact")) {
            //ctContact.setValue(contents.toString());
            //contacts.getContact().add(ctContact);
            currentBaseEntity.put("contacts", new BaseEntityComplexSet(batch, index, contacts));
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
        } else if(localName.equals("docs")){

        }else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
}

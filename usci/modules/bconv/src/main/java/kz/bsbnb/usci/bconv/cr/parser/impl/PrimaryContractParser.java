package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.TypeErrorException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class PrimaryContractParser extends BatchParser {
    private Logger logger = Logger.getLogger(PrimaryContractParser.class);

    public PrimaryContractParser() {
        super();
    }

    @Override
    public void init()
    {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("primary_contract"), new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        try {
            if(localName.equals("primary_contract")) {
            } else if(localName.equals("no")) {
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("no", new BaseValue(batch, index, event.asCharacters().getData()));
            } else if(localName.equals("date")) {
                event = (XMLEvent) xmlReader.next();
                currentBaseEntity.put("date", new BaseValue(batch, index,
                        dateFormat.parse(event.asCharacters().getData())
                    ));
            } else {
                throw new UnknownTagException(localName);
            }
        } catch (ParseException parseException) {
            throw new UnknownValException(localName, event.asCharacters().getData());
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        //try {
            if(localName.equals("primary_contract")) {
                //currentPackage.setPrimaryContract(ctContract);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("no")) {
                //ctContract.setNo(contents.toString());
            } else if(localName.equals("date")) {
                //ctContract.setDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch (ParseException parseException) {
            throw new TypeErrorException(localName);
        } */
        return false;
    }
}

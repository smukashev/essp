package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
@Component
public class CreditContractParser  extends BatchParser {
    public CreditContractParser() {
        super();
    }

    @Override
    public void init()
    {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("contract"), new Date());
    }
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        try {
            if(localName.equals("contract")) {
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
        } catch(ParseException parseException) {
            throw new UnknownValException(localName, event.asCharacters().getData());
        }

        return false;
    }       
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        //try {
            if(localName.equals("contract")) {
                //ctCredit.setContract(ctContract);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("no")) {
                //ctContract.setNo(contents.toString());
            } else if(localName.equals("date")) {
                //ctContract.setDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch(ParseException parseException) {
            throw new UnknownValException(localName, contents.toString());
        }*/
        return false;
    }  
}

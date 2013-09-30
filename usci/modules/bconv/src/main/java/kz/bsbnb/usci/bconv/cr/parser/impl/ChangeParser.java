package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import java.text.ParseException;
import java.util.Date;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class ChangeParser extends BatchParser {
    @Autowired
    private ChangeTurnoverParser changeTurnoverParser;

    @Autowired
    private ChangeRemainsParser changeRemainsParser;

    @Autowired
    private ChangeCreditFlowParser changeCreditFlowParser;
    
    public ChangeParser() {
        super();
    }

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("change"),new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("change")) {
        } else if(localName.equals("turnover")) {
            changeTurnoverParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("turnover",new BaseValue(batch,index,changeTurnoverParser.getCurrentBaseEntity()));
        } else if(localName.equals("remains")) {
            changeRemainsParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("remains",new BaseValue(batch,index,changeRemainsParser.getCurrentBaseEntity()));
        } else if(localName.equals("credit_flow")) {
            changeCreditFlowParser.parse(xmlReader, batch, index);
            currentBaseEntity.put("credit_flow",new BaseValue(batch,index,
                    changeCreditFlowParser.getCurrentBaseEntity()));
        } else if(localName.equals("maturity_date")) {
            event = (XMLEvent) xmlReader.next();
            try{
              currentBaseEntity.put("maturity_date",new BaseValue(batch,index,
                      dateFormat.parse(event.asCharacters().getData()))
              );
            } catch (ParseException e){
                System.out.println(e.getMessage());
            }
        } else if(localName.equals("prolongation_date")) {
            event = (XMLEvent) xmlReader.next();
            try{
                currentBaseEntity.put("prolongation_date",new BaseValue(batch,index,
                        dateFormat.parse(event.asCharacters().getData()))
                );
            } catch (ParseException e){
                System.out.println(e.getMessage());
            }
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }
    
    @Override
    public boolean endElement(String localName) throws SAXException {
        //try {
            if(localName.equals("change")) {
                //currentPackage.setChange(ctChange);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("turnover")) {
            } else if(localName.equals("remains")) {
            } else if(localName.equals("credit_flow")) {
            } else if(localName.equals("maturity_date")) {
                //ctChange.setMaturityDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("prolongation_date")) {
                //ctChange.setProlongationDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch(ParseException parseException) {
            throw new UnknownValException(localName, contents.toString());
        }*/

        return false;
    }
}

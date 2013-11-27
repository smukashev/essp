package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import org.springframework.context.annotation.Scope;
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
@Scope("prototype")
public class ChangeRemainsInterestParser extends BatchParser {
    private String interestWay;
    
    public ChangeRemainsInterestParser() {
        super();
    }

    private BaseEntity fieldCurrent;
    private BaseEntity fieldPastDue;
    private BaseEntity fieldWriteOf;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("interest1"),new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("interest")) {
        } else if(localName.equals("current")) {
            //ctRemainsTypeCurrent = new CtRemainsTypeCurrent();
            fieldCurrent = new BaseEntity(metaClassRepository.getMetaClass("current1"),new Date());
            interestWay = localName;
        } else if(localName.equals("pastdue")) {
            //ctRemainsTypePastdue = new CtRemainsTypePastdue();
            fieldPastDue = new BaseEntity(metaClassRepository.getMetaClass("pastdue1"),new Date());
            interestWay = localName;
        } else if(localName.equals("write_off")) {
            //ctRemainsTypeInterestWriteOff = new CtRemainsTypeInterestWriteOff();
            fieldWriteOf = new BaseEntity(metaClassRepository.getMetaClass("write_off1"),new Date());
            interestWay = localName;
        } else if(localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseValue(batch,index,new Double(event.asCharacters().getData()));
            if(interestWay.equals("current")){
                fieldCurrent.put("value",baseValue);
            }else if(interestWay.equals("pastdue")){
                fieldPastDue.put("value",baseValue);
            }else if(interestWay.equals("write_off")){
                fieldWriteOf.put("value",baseValue);
            }
        } else if(localName.equals("value_currency")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseValue(batch,index,new Double(event.asCharacters().getData()));
            if(interestWay.equals("current")){
                fieldCurrent.put("value_currency",baseValue);
            }else if(interestWay.equals("pastdue")){
                fieldPastDue.put("value_currency",baseValue);
            }else if(interestWay.equals("write_off")){
                fieldWriteOf.put("value_currency",baseValue);
            }
        } else if(localName.equals("balance_account")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity baseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),new Date());
            baseEntity.put("no_",new BaseValue(batch,index,event.asCharacters().getData()));
            BaseValue baseValue = new BaseValue(batch,index,baseEntity);
            if(interestWay.equals("current")){
                fieldCurrent.put("balance_account",baseValue);
            }else if(interestWay.equals("pastdue")){
                fieldPastDue.put("balance_account",baseValue);
            }else if(interestWay.equals("write_off")){
                fieldWriteOf.put("balance_account",baseValue);
            }
        } else if(localName.equals("open_date")) {
            event = (XMLEvent) xmlReader.next();
            try{
                fieldPastDue.put("open_date",new BaseValue(batch,index,dateFormat.parse(event.asCharacters().getData())));
            }catch(ParseException e){
                System.out.println(e.getMessage());
            }
        } else if(localName.equals("close_date")) {
            event = (XMLEvent) xmlReader.next();
            try {
                fieldPastDue.put("close_date",new BaseValue(batch,index,dateFormat.parse(event.asCharacters().getData())));
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
        } else if(localName.equals("date")) {
            event = (XMLEvent) xmlReader.next();
            try {
                fieldWriteOf.put("date",new BaseValue(batch,index,dateFormat.parse(event.asCharacters().getData())));
            } catch (ParseException e) {
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
            if(localName.equals("interest")) {
                //ctRemains.setInterest(interest);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("current")) {
                //interest.setCurrent(ctRemainsTypeCurrent);
                currentBaseEntity.put("current",new BaseValue(batch,index,fieldCurrent));
            } else if(localName.equals("pastdue")) {
                //interest.setPastdue(ctRemainsTypePastdue);
                currentBaseEntity.put("pastdue",new BaseValue(batch,index,fieldPastDue));
            } else if(localName.equals("write_off")) {
                //interest.setWriteOff(ctRemainsTypeInterestWriteOff);
                currentBaseEntity.put("write_off",new BaseValue(batch,index,fieldWriteOf));
            } else if(localName.equals("value")) {
                if(interestWay.equals("current")) {
                    //ctRemainsTypeCurrent.setValue(new BigDecimal(contents.toString()));
                } else if(interestWay.equals("pastdue")) {
                    //ctRemainsTypePastdue.setValue(new BigDecimal(contents.toString()));
                } else if(interestWay.equals("write_off")) {
                    //ctRemainsTypeInterestWriteOff.setValue(new BigDecimal(contents.toString()));
                } else {
                    //throw new UnknownValException(localName, contents.toString());
                }
            } else if(localName.equals("value_currency")) {
                if(interestWay.equals("current")) {
                    //ctRemainsTypeCurrent.setValueCurrency(new BigDecimal(contents.toString()));
                } else if(interestWay.equals("pastdue")) {
                    //ctRemainsTypePastdue.setValueCurrency(new BigDecimal(contents.toString()));
                } else if(interestWay.equals("write_off")) {
                    //ctRemainsTypeInterestWriteOff.setValueCurrency(new BigDecimal(contents.toString()));
                } else {
                    //throw new UnknownValException(localName, contents.toString());
                }
            } else if(localName.equals("balance_account")) {
                if(interestWay.equals("current")) {
                    //ctRemainsTypeCurrent.setBalanceAccount(contents.toString());
                } else if(interestWay.equals("pastdue")) {
                    //ctRemainsTypePastdue.setBalanceAccount(contents.toString());
                } else {
                    //throw new UnknownValException(localName, contents.toString());
                }
            } else if(localName.equals("open_date")) {
                //ctRemainsTypePastdue.setOpenDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("close_date")) {
                //ctRemainsTypePastdue.setCloseDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("date")) {
                //ctRemainsTypeInterestWriteOff.setDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else {
                //throw new UnknownTagException(localName);
            }
        /*} catch(ParseException parseException) {
            throw new UnknownValException(localName, contents.toString());
        } */

        return false;
    }
}

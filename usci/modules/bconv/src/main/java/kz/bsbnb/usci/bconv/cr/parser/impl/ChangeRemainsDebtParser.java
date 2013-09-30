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
public class ChangeRemainsDebtParser extends BatchParser {
    private String debtWay;
    
    public ChangeRemainsDebtParser() {
        super();
    }

    private BaseEntity fieldCurrent;
    private BaseEntity fieldPastDue;
    private BaseEntity fieldWriteOf;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("debt1"),new Date());
        fieldCurrent = new BaseEntity(metaClassRepository.getMetaClass("current"),new Date());
        fieldPastDue = new BaseEntity(metaClassRepository.getMetaClass("pastdue"),new Date());
        fieldWriteOf = new BaseEntity(metaClassRepository.getMetaClass("write_off"),new Date());
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("debt")) {
        } else if(localName.equals("current")) {
            //ctRemainsTypeCurrent = new CtRemainsTypeCurrentNonNegative();
            debtWay = localName;
        } else if(localName.equals("pastdue")) {
            //ctRemainsTypePastdue = new CtRemainsTypePastdueNonNegative();
            debtWay = localName;
        } else if(localName.equals("write_off")) {
            //ctRemainsTypeDebtWriteOff = new CtRemainsTypeDebtWriteOff();
            debtWay = localName;
        } else if(localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseValue(batch,index,new Double(event.asCharacters().getData()));
            if(debtWay.equals("current")){
                fieldCurrent.put("value",baseValue);
            }else if(debtWay.equals("pastdue")){
                fieldPastDue.put("value",baseValue);
            }else if(debtWay.equals("write_off")){
                fieldWriteOf.put("value",baseValue);
            }
        } else if(localName.equals("value_currency")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseValue(batch,index,new Double(event.asCharacters().getData()));
            if(debtWay.equals("current")){
                fieldCurrent.put("value_currency",baseValue);
            }else if(debtWay.equals("pastdue")){
                fieldPastDue.put("value_currency",baseValue);
            }else if(debtWay.equals("write_off")){
                fieldWriteOf.put("value_currency",baseValue);
            }
        } else if(localName.equals("balance_account")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseValue(batch,index,new String(event.asCharacters().getData()));
            if(debtWay.equals("current")){
                fieldCurrent.put("balance_account",baseValue);
            }else if(debtWay.equals("pastdue")){
                fieldPastDue.put("balance_account",baseValue);
            }else if(debtWay.equals("write_off")){
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
            if(localName.equals("debt")) {
                //ctRemains.setDebt(debt);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("current")) {
                //debt.setCurrent(ctRemainsTypeCurrent);
            } else if(localName.equals("pastdue")) {
                //debt.setPastdue(ctRemainsTypePastdue);
            } else if(localName.equals("write_off")) {
                //debt.setWriteOff(ctRemainsTypeDebtWriteOff);
            } else if(localName.equals("value")) {
                if(debtWay.equals("current")) {
                    //ctRemainsTypeCurrent.setValue(new BigDecimal(contents.toString()));
                } else if(debtWay.equals("pastdue")) {
                    //ctRemainsTypePastdue.setValue(new BigDecimal(contents.toString()));
                } else if(debtWay.equals("write_off")) {
                    //ctRemainsTypeDebtWriteOff.setValue(new BigDecimal(contents.toString()));
                } else {
                    //throw new UnknownValException(localName, contents.toString());
                }
            } else if(localName.equals("value_currency")) {
                if(debtWay.equals("current")) {
                    //ctRemainsTypeCurrent.setValueCurrency(new BigDecimal(contents.toString()));
                    currentBaseEntity.put("current",new BaseValue(batch,index,fieldCurrent));
                } else if(debtWay.equals("pastdue")) {
                    //ctRemainsTypePastdue.setValueCurrency(new BigDecimal(contents.toString()));
                    currentBaseEntity.put("pastdue",new BaseValue(batch,index,fieldPastDue));
                } else if(debtWay.equals("write_off")) {
                    //ctRemainsTypeDebtWriteOff.setValueCurrency(new BigDecimal(contents.toString()));
                    currentBaseEntity.put("write_off",new BaseValue(batch,index,fieldWriteOf));
                } else {
                    //throw new UnknownValException(localName, contents.toString());
                }
            } else if(localName.equals("balance_account")) {
                if(debtWay.equals("current")) {
                    //ctRemainsTypeCurrent.setBalanceAccount(contents.toString());
                } else if(debtWay.equals("pastdue")) {
                    //ctRemainsTypePastdue.setBalanceAccount(contents.toString());
                } else if(debtWay.equals("write_off")) {
                    //ctRemainsTypeDebtWriteOff.setBalanceAccount(contents.toString());
                } else {
                    //throw new UnknownValException(localName, contents.toString());
                }
            } else if(localName.equals("open_date")) {
                //ctRemainsTypePastdue.setOpenDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("close_date")) {
                //ctRemainsTypePastdue.setCloseDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("date")) {
                //ctRemainsTypeDebtWriteOff.setDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch(ParseException parseException) {
            throw new UnknownValException(localName, contents.toString());
        }*/

        return false;
    }
}

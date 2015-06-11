package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDoubleValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityStringValue;
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
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("remains_debt"),batch.getRepDate());

    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("debt")) {
        } else if(localName.equals("current")) {
            //ctRemainsTypeCurrent = new CtRemainsTypeCurrentNonNegative();
            fieldCurrent = new BaseEntity(metaClassRepository.getMetaClass("remains_debt_current"),batch.getRepDate());
            debtWay = localName;
        } else if(localName.equals("pastdue")) {
            //ctRemainsTypePastdue = new CtRemainsTypePastdueNonNegative();
            fieldPastDue = new BaseEntity(metaClassRepository.getMetaClass("remains_debt_pastdue"),batch.getRepDate());
            debtWay = localName;
        } else if(localName.equals("write_off")) {
            //ctRemainsTypeDebtWriteOff = new CtRemainsTypeDebtWriteOff();
            fieldWriteOf = new BaseEntity(metaClassRepository.getMetaClass("remains_debt_write_off"),batch.getRepDate());
            debtWay = localName;
        } else if(localName.equals("value")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseEntityDoubleValue(batch,index,new Double(event.asCharacters().getData()));
            if(debtWay.equals("current")){
                fieldCurrent.put("value",baseValue);
            }else if(debtWay.equals("pastdue")){
                fieldPastDue.put("value",baseValue);
            }else if(debtWay.equals("write_off")){
                fieldWriteOf.put("value",baseValue);
            }
        } else if(localName.equals("value_currency")) {
            event = (XMLEvent) xmlReader.next();
            BaseValue baseValue = new BaseEntityDoubleValue(batch,index,new Double(event.asCharacters().getData()));
            if(debtWay.equals("current")){
                fieldCurrent.put("value_currency",baseValue);
            }else if(debtWay.equals("pastdue")){
                fieldPastDue.put("value_currency",baseValue);
            }else if(debtWay.equals("write_off")){
                fieldWriteOf.put("value_currency",baseValue);
            }
        } else if(localName.equals("balance_account")) {
            event = (XMLEvent) xmlReader.next();
            BaseEntity baseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"),batch.getRepDate());
            baseEntity.put("no_",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
            BaseValue baseValue = new BaseEntityComplexValue(batch,index,baseEntity);
            if(debtWay.equals("current")){
                fieldCurrent.put("balance_account",new BaseEntityComplexValue(batch,index,baseEntity));
            }else if(debtWay.equals("pastdue")){
                fieldPastDue.put("balance_account",baseValue);
            }else if(debtWay.equals("write_off")){
                fieldWriteOf.put("balance_account",baseValue);
            }
        } else if(localName.equals("open_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try{
                fieldPastDue.put("open_date",new BaseEntityDateValue(batch,index,dateFormat.parse(dateRaw)));
            }catch(ParseException e){
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if(localName.equals("close_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                fieldPastDue.put("close_date",new BaseEntityDateValue(batch,index,dateFormat.parse(dateRaw)));
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if(localName.equals("date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                fieldWriteOf.put("date",new BaseEntityDateValue(batch,index,dateFormat.parse(dateRaw)));
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
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
                currentBaseEntity.put("current",new BaseEntityComplexValue(batch,index,fieldCurrent));
                //debt.setCurrent(ctRemainsTypeCurrent);
            } else if(localName.equals("pastdue")) {
                currentBaseEntity.put("pastdue",new BaseEntityComplexValue(batch,index,fieldPastDue));
                //debt.setPastdue(ctRemainsTypePastdue);
            } else if(localName.equals("write_off")) {
                currentBaseEntity.put("write_off",new BaseEntityComplexValue(batch,index,fieldWriteOf));
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

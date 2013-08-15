package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
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
public class ChangeRemainsDebtParser extends BatchParser {
    private String debtWay;
    
    public ChangeRemainsDebtParser() {
        super();
    }
    
    @Override
    public void startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
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
        } else if(localName.equals("value_currency")) {
        } else if(localName.equals("balance_account")) {
        } else if(localName.equals("open_date")) {
        } else if(localName.equals("close_date")) {
        } else if(localName.equals("date")) {
        } else {
            throw new UnknownTagException(localName);
        }
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
                } else if(debtWay.equals("pastdue")) {
                    //ctRemainsTypePastdue.setValueCurrency(new BigDecimal(contents.toString()));
                } else if(debtWay.equals("write_off")) {
                    //ctRemainsTypeDebtWriteOff.setValueCurrency(new BigDecimal(contents.toString()));
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

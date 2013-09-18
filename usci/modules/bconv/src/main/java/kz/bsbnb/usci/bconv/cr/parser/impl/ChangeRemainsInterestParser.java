package kz.bsbnb.usci.bconv.cr.parser.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownValException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
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
    
    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("interest")) {
        } else if(localName.equals("current")) {
            //ctRemainsTypeCurrent = new CtRemainsTypeCurrent();
            interestWay = localName;
        } else if(localName.equals("pastdue")) {
            //ctRemainsTypePastdue = new CtRemainsTypePastdue();
            interestWay = localName;
        } else if(localName.equals("write_off")) {
            //ctRemainsTypeInterestWriteOff = new CtRemainsTypeInterestWriteOff();
            interestWay = localName;
        } else if(localName.equals("value")) {
        } else if(localName.equals("value_currency")) {
        } else if(localName.equals("balance_account")) {
        } else if(localName.equals("open_date")) {
        } else if(localName.equals("close_date")) {
        } else if(localName.equals("date")) {
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
            } else if(localName.equals("pastdue")) {
                //interest.setPastdue(ctRemainsTypePastdue);
            } else if(localName.equals("write_off")) {
                //interest.setWriteOff(ctRemainsTypeInterestWriteOff);
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

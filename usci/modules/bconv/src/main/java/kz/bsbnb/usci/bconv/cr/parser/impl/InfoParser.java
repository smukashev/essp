package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.TypeErrorException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author k.tulbassiyev
 */
public class InfoParser extends BatchParser {
    private static final Logger logger
            = Logger.getLogger(InfoParser.class.getName());

    public InfoParser() {
        super();
    }

    @Override
    public void startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("info")) {
        } else if(localName.equals("creditor")) {
        } else if(localName.equals("code")) {
        } else if(localName.equals("docs")) {
            //docs = new Docs();
        } else if(localName.equals("doc")) {
            //currentDoc = new CtDoc();
            //currentDoc.setDocType(attributes.getValue("doc_type"));
        } else if(localName.equals("name")) {
        } else if(localName.equals("no")) {
        } else if(localName.equals("account_date")) {
        } else if(localName.equals("report_date")) {
        } else if(localName.equals("actual_credit_count")) {
        } else {
            throw new UnknownTagException(localName);
        }
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        //try {
            if(localName.equals("info")) {
                //batch.setInfo(info);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("creditor")) {
                //ctCreditor.setDocs(docs);
                //info.setCreditor(ctCreditor);
            } else if(localName.equals("code")) {
                //ctCreditor.setCode(contents.toString());
            } else if(localName.equals("docs")) {
            } else if(localName.equals("doc")) {
                //docs.getDoc().add(currentDoc);
            } else if(localName.equals("name")) {
                //currentDoc.setName(contents.toString());
            } else if(localName.equals("no")) {
                //currentDoc.setNo(contents.toString());
            } else if(localName.equals("account_date")) {
                //info.setAccountDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("report_date")) {
                //info.setReportDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("actual_credit_count")) {
                //info.setActualCreditCount(new BigInteger(contents.toString()));
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch(ParseException e) {
            throw new TypeErrorException(localName);
        } */
        return false;
    }
}

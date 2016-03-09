package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.receiver.reader.impl.beans.InfoData;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;

@Component
@Scope("prototype")
public class InfoReader extends MainReader {

    private InfoData infoData = new InfoData();

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "code":
                break;
            case "doc":
                infoData.setDocType(event.asStartElement().getAttributeByName(new QName("doc_type")).getValue());
                break;
            case "no":
                break;
            case "report_date":
                break;
            case "actual_credit_count":
                break;
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "info":
                return true;
            case "code":
                infoData.setCode(data.toString());
                data.setLength(0);
                break;
            case "doc":
                break;
            case "no":
                infoData.setDocValue(data.toString());
                data.setLength(0);
                break;
            case "report_date":
                try {
                    infoData.setReportDate(dateFormat.parse(data.toString()));
                } catch (ParseException e) {
                }
                data.setLength(0);
                break;
            case "actual_credit_count":
                try{
                    infoData.setActualCreditCount(Long.parseLong(data.toString()));
                }catch(NumberFormatException nfe){
                    nfe.printStackTrace();
                }
                data.setLength(0);
                break;
        }

        return false;
    }

    public InfoData getInfoData() {
        return infoData;
    }
}

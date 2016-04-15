package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.receiver.reader.impl.beans.InfoData;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;

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
            case "account_date":
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
            case "account_date":
                try {
                    infoData.setAccountDate(DataTypes.dateFormatSlash.parse(data.toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                data.setLength(0);
                break;
            case "report_date":
                try {
                    infoData.setReportDate(DataTypes.dateFormatSlash.parse(data.toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                data.setLength(0);
                break;
            case "actual_credit_count":
                try {
                    infoData.setActualCreditCount(Long.parseLong(data.toString()));
                } catch (NumberFormatException nfe) {
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

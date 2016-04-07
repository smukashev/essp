package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.receiver.reader.impl.beans.ManifestData;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ManifestReader extends MainReader {
    private ManifestData manifestData = new ManifestData();
    private String currentName;

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "manifest":
                break;
            case "type":
                break;
            case "userid":
                break;
            case "size":
                break;
            case "date":
                break;
            case "name":
                break;
            case "value":
                break;
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "manifest":
                return true;
            case "type":
                manifestData.setType(data.toString());
                data.setLength(0);
                break;
            case "userid":
                manifestData.setUserId(Long.parseLong(data.toString()));
                data.setLength(0);
                break;
            case "size":
                manifestData.setSize(Integer.parseInt(data.toString()));
                data.setLength(0);
                break;
            case "date":
                try {
                    manifestData.setReportDate(new SimpleDateFormat("dd.MM.yyyy").parse(data.toString()));
                } catch (ParseException e) {
                }
                data.setLength(0);
                break;
            case "name":
                currentName = data.toString();
                data.setLength(0);
                break;
            case "value":
                manifestData.getAdditionalParams().put(currentName, data.toString());
                data.setLength(0);
                break;
        }

        return false;
    }

    public ManifestData getManifestData() {
        return manifestData;
    }
}

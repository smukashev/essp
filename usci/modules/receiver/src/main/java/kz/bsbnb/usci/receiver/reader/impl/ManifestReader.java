package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.reader.impl.beans.InfoData;
import kz.bsbnb.usci.receiver.reader.impl.beans.ManifestData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class ManifestReader extends MainReader{

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
                manifestData.getAdditionalParams().put(currentName,data.toString());
                data.setLength(0);
                break;
        }

        return false;
    }

    public ManifestData getManifestData() {
        return manifestData;
    }
}

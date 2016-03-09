package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.bconv.cr.parser.Const;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class MainReader {

    protected XMLEventReader xmlReader;
    protected StringBuilder data = new StringBuilder();
    protected DateFormat dateFormat = new SimpleDateFormat(Const.DATE_FORMAT);

    public void parse(XMLEventReader xmlReader) throws SAXException {
        this.xmlReader = xmlReader;

        while (xmlReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlReader.next();

            if (event.isStartDocument()) {
                System.out.println("start document");
            } else if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if (startElement(event, startElement, localName)) break;
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (endElement(localName)) break;
            } else if (event.isCharacters()) {
                data.append(event.asCharacters().getData().replaceAll("\\s+", ""));
            } else if (event.isEndDocument()) {
                System.out.println("end document");
                data.append(event.asCharacters().getData().replaceAll("\\s+", ""));
            } else {
                System.out.println(event.toString());
            }
        }
    }

    public abstract boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException;

    public abstract boolean endElement(String localName) throws SAXException;
}

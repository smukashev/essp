package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;

@Component
@Scope("prototype")
public class MainParser extends BatchParser {
    @Autowired
    private InfoParser infoParser;

    @Autowired
    private PackageParser packageParser;

    @Autowired
    private PortfolioDataParser portfolioDataParser;

    public void parse(InputStream in, Batch batch) throws SAXException, IOException, XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        parse(inputFactory.createXMLEventReader(in), batch, 1L);
    }

    public void parseNextPackage() throws SAXException {
        long currentIndex = index++;

        packageParser.parse(xmlReader, batch, currentIndex);

        if (packageParser.hasMore()) {
            currentBaseEntity = packageParser.getCurrentBaseEntity();
            BaseEntity creditor = infoParser.getCurrentBaseEntity();

            currentBaseEntity.put("creditor", new BaseEntityComplexValue(0, -1, batch.getRepDate(), creditor,
                    false, true));

            for (String s : creditor.getValidationErrors()) {
                currentBaseEntity.addValidationError(s);
            }
        } else {
            parse(xmlReader, batch, index = 1L);
        }
    }

    public void skipNextPackage() throws SAXException {
        while (xmlReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlReader.next();
            currentBaseEntity = null;

            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (localName.equals("packages")) {
                    hasMore = false;
                    return;
                } else if (localName.equals("package")) {
                    hasMore = true;
                    return;
                }
            }
        }
    }

    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("batch")) {
        } else if (localName.equals("info")) {
            infoParser.parse(xmlReader, batch, index);
        } else if (localName.equals("packages")) {
        } else if (localName.equals("package")) {
            BaseEntity pkg = new BaseEntity(metaClassRepository.getMetaClass("credit"), batch.getRepDate());

            String strOperationType = event.asStartElement().getAttributeByName(
                    new QName("operation_type")).getValue();

            if (strOperationType.equals("insert")) {
                pkg.setOperation(OperationType.INSERT);
            } else if (strOperationType.equals("update")) {
                pkg.setOperation(OperationType.UPDATE);
            } else {
                throw new IllegalStateException("Операция не поддерживается(" + strOperationType + ");");
            }

            packageParser.setCurrentBaseEntity(pkg);
            hasMore = true;
            parseNextPackage();
            return true;
        } else if (localName.equals("portfolio_data")) {
            hasMore = true;
            portfolioDataParser.parse(xmlReader, batch, index);
            currentBaseEntity = portfolioDataParser.getCurrentBaseEntity();
            currentBaseEntity.put("creditor", new BaseEntityComplexValue(0, -1, batch.getRepDate(),
                    infoParser.getCurrentBaseEntity(), false, true));
            return true;
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("batch")) {
            hasMore = false;
            return true;
        } else if (localName.equals("info")) {
        } else if (localName.equals("packages")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    public int getPackageCount() {
        return packageParser.getTotalCount();
    }
}

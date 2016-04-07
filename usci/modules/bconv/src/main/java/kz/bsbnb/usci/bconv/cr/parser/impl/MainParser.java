package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.util.Errors;
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
        parse(inputFactory.createXMLEventReader(in), batch, 1L, creditorId);
    }

    public void parseNextPackage() throws SAXException {
        long currentIndex = index++;

        packageParser.parse(xmlReader, batch, currentIndex, creditorId);

        if (packageParser.hasMore()) {
            currentBaseEntity = packageParser.getCurrentBaseEntity();
            BaseEntity creditor = infoParser.getCurrentBaseEntity();

            currentBaseEntity.put("creditor", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), creditor,
                    false, true));

            for (String s : creditor.getValidationErrors()) {
                currentBaseEntity.addValidationError(s);
            }
        } else {
            parse(xmlReader, batch, index = 1L, creditorId);
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
        switch (localName) {
            case "batch":
                break;
            case "info":
                infoParser.parse(xmlReader, batch, index, creditorId);
                break;
            case "packages":
                break;
            case "package":
                BaseEntity pkg = new BaseEntity(metaClassRepository.getMetaClass("credit"),
                        batch.getRepDate(), creditorId);

                String strOperationType = event.asStartElement().getAttributeByName(
                        new QName("operation_type")).getValue();

                switch (strOperationType) {
                    case "insert":
                        pkg.setOperation(OperationType.INSERT);
                        break;
                    case "update":
                        pkg.setOperation(OperationType.UPDATE);
                        break;
                    default:
                        throw new IllegalStateException(Errors.compose(Errors.E118, strOperationType));
                }

                packageParser.setCurrentBaseEntity(pkg);
                hasMore = true;
                parseNextPackage();
                return true;
            case "portfolio_data":
                hasMore = true;
                portfolioDataParser.parse(xmlReader, batch, index, creditorId);
                currentBaseEntity = portfolioDataParser.getCurrentBaseEntity();
                currentBaseEntity.put("creditor", new BaseEntityComplexValue(0, creditorId, batch.getRepDate(),
                        infoParser.getCurrentBaseEntity(), false, true));
                return true;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "batch":
                hasMore = false;
                return true;
            case "info":
                break;
            case "packages":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    public int getPackageCount() {
        return packageParser.getTotalCount();
    }
}

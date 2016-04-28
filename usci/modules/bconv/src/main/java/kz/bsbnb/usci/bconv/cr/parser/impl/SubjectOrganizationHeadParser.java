package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetComplexValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@Component
@Scope("prototype")
public class SubjectOrganizationHeadParser extends BatchParser {
    @Autowired
    private SubjectOrganizationHeadNamesParser subjectOrganizationHeadNamesParser;

    @Autowired
    private SubjectOrganizationHeadDocsParser subjectOrganizationHeadDocsParser;

    public SubjectOrganizationHeadParser() {
        super();
    }

    private MetaClass personNameMeta, documentMeta;

    @Override
    public void init() {
        currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("head"), batch.getRepDate(), creditorId);

        personNameMeta = metaClassRepository.getMetaClass("person_name");
        documentMeta = metaClassRepository.getMetaClass("document");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName)
            throws SAXException {
        switch (localName) {
            case "head":
                break;
            case "names":
                BaseSet headNames = new BaseSet(personNameMeta, creditorId);
                while (true) {
                    subjectOrganizationHeadNamesParser.parse(xmlReader, batch, index, creditorId);
                    if (subjectOrganizationHeadNamesParser.hasMore()) {
                        headNames.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(),
                                subjectOrganizationHeadNamesParser.getCurrentBaseEntity(), false, true));
                    } else break;
                }

                currentBaseEntity.put("names",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), headNames, false, true));
                break;
            case "docs":
                BaseSet docs = new BaseSet(documentMeta, creditorId);

                while (true) {
                    subjectOrganizationHeadDocsParser.parse(xmlReader, batch, index, creditorId);

                    if (subjectOrganizationHeadDocsParser.hasMore()) {
                        docs.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(),
                                subjectOrganizationHeadDocsParser.getCurrentBaseEntity(), false, true));
                    } else break;
                }

                currentBaseEntity.put("docs",
                        new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), docs, false, true));
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "head":
                return true;
            case "names":
                break;
            case "docs":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }
}

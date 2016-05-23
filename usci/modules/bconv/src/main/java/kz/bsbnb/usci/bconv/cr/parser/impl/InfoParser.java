package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;


@Component
@Scope("prototype")
public class InfoParser extends BatchParser {
    public InfoParser() {
        super();
    }

    private BaseSet docs;
    private BaseEntity currentDoc;
    private BaseValue<Date> reportDate;

    private MetaClass refCreditorMeta, documentMeta, refDocTypeMeta;

    @Override
    public void init() {
        refCreditorMeta = metaClassRepository.getMetaClass("ref_creditor");
        documentMeta = metaClassRepository.getMetaClass("document");
        refDocTypeMeta = metaClassRepository.getMetaClass("ref_doc_type");
    }

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        switch (localName) {
            case "info":
                break;
            case "creditor":
                currentBaseEntity = new BaseEntity(refCreditorMeta, batch.getRepDate(), creditorId);
                break;
            case "code":
                event = (XMLEvent) xmlReader.next();
                String crCode = trim(event.asCharacters().getData());

                currentBaseEntity.put("code", new BaseEntityStringValue(0, creditorId, batch.getRepDate(), crCode, false, true));
                break;
            case "docs":
                docs = new BaseSet(documentMeta, creditorId);
                break;
            case "doc":
                currentDoc = new BaseEntity(documentMeta, batch.getRepDate(), creditorId);

                BaseEntity docType = new BaseEntity(refDocTypeMeta, batch.getRepDate(), creditorId);

                docType.put("code",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(),
                                event.asStartElement().getAttributeByName(new QName("doc_type")).getValue(), false, true));

                currentDoc.put("doc_type",
                        new BaseEntityComplexValue(0, creditorId, batch.getRepDate(), docType, false, true));
                break;
            case "name":
                event = (XMLEvent) xmlReader.next();

                currentDoc.put("name",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));
                break;
            case "no":
                event = (XMLEvent) xmlReader.next();

                currentDoc.put("no",
                        new BaseEntityStringValue(0, creditorId, batch.getRepDate(), trim(event.asCharacters().getData()), false, true));
                break;
            case "account_date":
                break;
            case "report_date":
                event = (XMLEvent) xmlReader.next();
                String dateRaw = trim(event.asCharacters().getData());

                try {
                    reportDate = new BaseEntityDateValue(0, creditorId, batch.getRepDate(), dateFormat.parse(dateRaw), false, true);
                } catch (ParseException e) {
                    currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
                }
                break;
            case "actual_credit_count":
                break;
            case  "maintenance":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        switch (localName) {
            case "info":
                return true;
            case "creditor":
                break;
            case "code":
                break;
            case "docs":
                currentBaseEntity.put("docs", new BaseEntityComplexSet(0, creditorId, batch.getRepDate(), docs, false, true));
                break;
            case "doc":
                docs.put(new BaseSetComplexValue(0, creditorId, batch.getRepDate(), currentDoc, false, true));
                break;
            case "name":
                break;
            case "no":
                break;
            case "account_date":
                break;
            case "report_date":
                break;
            case "actual_credit_count":
                break;
            case "maintenance":
                break;
            default:
                throw new UnknownTagException(localName);
        }

        return false;
    }

    public BaseValue<Date> getReportDate() {
        return reportDate;
    }
}

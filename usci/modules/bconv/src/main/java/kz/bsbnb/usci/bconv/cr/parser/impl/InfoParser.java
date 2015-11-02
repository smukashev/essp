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

    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if (localName.equals("info")) {
        } else if (localName.equals("creditor")) {
            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_creditor"), batch.getRepDate());

        } else if (localName.equals("code")) {
            event = (XMLEvent) xmlReader.next();
            String crCode = event.asCharacters().getData();

            RefListResponse refListResponse = refProcessorDao.getRefListResponse(
                    metaClassRepository.getMetaClass("ref_creditor").getId(), batch.getRepDate(), false);

            boolean found = false;

            for (Map<String, Object> m : refListResponse.getData())
                if (m.get("CODE") != null && m.get("CODE").equals(crCode)) {
                    long creditorId = ((BigDecimal) m.get("ID")).longValue();
                    IBaseEntity loadedCreditor = baseEntityLoadDao.load(creditorId);
                    BaseSet creditorDocsLoaded = (BaseSet) loadedCreditor.getEl("docs");
                    BaseSet creditorDocs = new BaseSet(metaClassRepository.getMetaClass("document"));
                    for (IBaseValue bv : creditorDocsLoaded.get()) {
                        BaseEntity docLoaded = (BaseEntity) bv.getValue();
                        BaseEntity doc = new BaseEntity(metaClassRepository.getMetaClass("document"),
                                batch.getRepDate());
                        doc.put("no", new BaseValue(0, -1, batch.getRepDate(), docLoaded.getEl("no")));

                        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"),
                                batch.getRepDate());
                        docType.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                                (String) docLoaded.getEl("doc_type.code"), false, true));
                        doc.put("doc_type", new BaseEntityComplexValue(0, -1, batch.getRepDate(), docType,
                                false, true));
                        creditorDocs.put(new BaseValue(0, -1, batch.getRepDate(), doc));
                    }
                    currentBaseEntity.put("docs", new BaseEntityComplexSet(0, -1, batch.getRepDate(),
                            creditorDocs, false, true));
                    found = true;
                    break;
                }

            if (!found)
                currentBaseEntity.addValidationError(String.format("Кредитор с кодом %s не найден", crCode));
        } else if (localName.equals("docs")) {
            docs = new BaseSet(metaClassRepository.getMetaClass("document"));
        } else if (localName.equals("doc")) {
            currentDoc = new BaseEntity(metaClassRepository.getMetaClass("document"), batch.getRepDate());

            BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), batch.getRepDate());
            docType.put("code", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asStartElement().getAttributeByName(new QName("doc_type")).getValue(), false, true));
            currentDoc.put("doc_type", new BaseEntityComplexValue(0, -1, batch.getRepDate(), docType, false, true));
        } else if (localName.equals("name")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("name", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else if (localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("no", new BaseEntityStringValue(0, -1, batch.getRepDate(),
                    event.asCharacters().getData(), false, true));
        } else if (localName.equals("account_date")) {
        } else if (localName.equals("report_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                reportDate = new BaseEntityDateValue(0, -1, batch.getRepDate(), dateFormat.parse(dateRaw),
                        false, true);
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if (localName.equals("actual_credit_count")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        if (localName.equals("info")) {
            return true;
        } else if (localName.equals("creditor")) {
        } else if (localName.equals("code")) {
        } else if (localName.equals("docs")) {
            currentBaseEntity.put("docs", new BaseEntityComplexSet(0, -1, batch.getRepDate(), docs, false, true));
        } else if (localName.equals("doc")) {
            docs.put(new BaseSetComplexValue(0, -1, batch.getRepDate(), currentDoc, false, true));
        } else if (localName.equals("name")) {
        } else if (localName.equals("no")) {
        } else if (localName.equals("account_date")) {
        } else if (localName.equals("report_date")) {
        } else if (localName.equals("actual_credit_count")) {
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    public BaseValue<Date> getReportDate() {
        return reportDate;
    }
}

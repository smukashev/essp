package kz.bsbnb.usci.bconv.cr.parser.impl;

import kz.bsbnb.usci.bconv.cr.parser.exceptions.TypeErrorException;
import kz.bsbnb.usci.bconv.cr.parser.exceptions.UnknownTagException;
import kz.bsbnb.usci.bconv.cr.parser.BatchParser;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author k.tulbassiyev
 */
@Component
@Scope("prototype")
public class InfoParser extends BatchParser {
    private static final Logger logger
            = Logger.getLogger(InfoParser.class.getName());

    public InfoParser() {
        super();
    }

    private BaseSet docs;
    private BaseEntity currentDoc;
    private BaseValue accountDate;
    private BaseValue<Date> reportDate;
    private BaseValue actualCreditCount;



    @Override
    public boolean startElement(XMLEvent event, StartElement startElement, String localName) throws SAXException {
        if(localName.equals("info")) {
        } else if(localName.equals("creditor")) {
            currentBaseEntity = new BaseEntity(metaClassRepository.getMetaClass("ref_creditor"),batch.getRepDate());
            //currentBaseEntity.put("code",new BaseValue(batch,index,777));
        } else if(localName.equals("code")) {
            event = (XMLEvent) xmlReader.next();
            String crCode = event.asCharacters().getData();
            IBaseEntityProcessorDao processorDao = baseEntityRepository.getBaseEntityProcessorDao();
            RefListResponse refListResponse = processorDao.getRefListResponse(
                    metaClassRepository.getMetaClass("ref_creditor").getId(), batch.getRepDate(), false);

            boolean found = false;

            for(Map<String,Object> m : refListResponse.getData())
                if(m.get("CODE") != null && m.get("CODE").equals(crCode)){
                    long creditorId = ((BigDecimal)m.get("ID")).longValue();
                    IBaseEntity loadedCreditor = processorDao.load(creditorId);
                    BaseSet creditorDocsLoaded = (BaseSet)loadedCreditor.getEl("docs");
                    BaseSet creditorDocs = new BaseSet(metaClassRepository.getMetaClass("document"));
                    for(IBaseValue bv:  creditorDocsLoaded.get()) {
                        BaseEntity docLoaded = (BaseEntity) bv.getValue();
                        BaseEntity doc = new BaseEntity(metaClassRepository.getMetaClass("document"),batch.getRepDate());
                        doc.put("no", new BaseValue(batch,index, docLoaded.getEl("no")));

                        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"),batch.getRepDate());
                        docType.put("code",new BaseEntityStringValue(batch,index,
                                (String)docLoaded.getEl("doc_type.code")));
                        doc.put("doc_type", new BaseEntityComplexValue(batch, index, docType));
                        creditorDocs.put(new BaseValue(batch,index, doc));
                    }
                    currentBaseEntity.put("docs",new BaseEntityComplexSet(batch,index,creditorDocs));
                    found = true;
                    break;
                }

            if(!found)
                currentBaseEntity.addValidationError(String.format("Кредитор с кодом %s не найден", crCode));
        } else if(localName.equals("docs")) {
            docs = new BaseSet(metaClassRepository.getMetaClass("document"));
            //docs = new Docs();
        } else if(localName.equals("doc")) {
            //currentDoc = new CtDoc();
            //currentDoc.setDocType(attributes.getValue("doc_type"));
            currentDoc = new BaseEntity(metaClassRepository.getMetaClass("document"),batch.getRepDate());

            BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"),batch.getRepDate());
            docType.put("code",new BaseEntityStringValue(batch,index,
                    event.asStartElement().getAttributeByName(new QName("doc_type")).getValue()));
            currentDoc.put("doc_type",new BaseEntityComplexValue(batch,index,docType));
        } else if(localName.equals("name")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("name",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("no")) {
            event = (XMLEvent) xmlReader.next();
            currentDoc.put("no",new BaseEntityStringValue(batch,index,event.asCharacters().getData()));
        } else if(localName.equals("account_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                accountDate = new BaseEntityDateValue(batch,index,dateFormat.parse(dateRaw));
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if(localName.equals("report_date")) {
            event = (XMLEvent) xmlReader.next();
            String dateRaw = event.asCharacters().getData();
            try {
                reportDate = new BaseEntityDateValue(batch,index,dateFormat.parse(dateRaw));
            } catch (ParseException e) {
                currentBaseEntity.addValidationError("Неправильная дата: " + dateRaw);
            }
        } else if(localName.equals("actual_credit_count")) {
            event = (XMLEvent) xmlReader.next();
            actualCreditCount = new BaseEntityIntegerValue(batch,index,new Integer(event.asCharacters().getData()));
        } else {
            throw new UnknownTagException(localName);
        }

        return false;
    }

    @Override
    public boolean endElement(String localName) throws SAXException {
        //try {
            if(localName.equals("info")) {
                //batch.setInfo(info);
                //xmlReader.setContentHandler(contentHandler);
                return true;
            } else if(localName.equals("creditor")) {
                //ctCreditor.setDocs(docs);
                //info.setCreditor(ctCreditor);
            } else if(localName.equals("code")) {
                //ctCreditor.setCode(contents.toString());
            } else if(localName.equals("docs")) {
                currentBaseEntity.put("docs",new BaseEntityComplexSet(batch,index,docs));
            } else if(localName.equals("doc")) {
                //docs.getDoc().add(currentDoc);
                docs.put(new BaseSetComplexValue(batch,index,currentDoc));
            } else if(localName.equals("name")) {
                //currentDoc.setName(contents.toString());
            } else if(localName.equals("no")) {
                //currentDoc.setNo(contents.toString());
            } else if(localName.equals("account_date")) {
                //info.setAccountDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("report_date")) {
                //info.setReportDate(convertDateToCalendar(dateFormat.parse(contents.toString())));
            } else if(localName.equals("actual_credit_count")) {
                //info.setActualCreditCount(new BigInteger(contents.toString()));
            } else {
                throw new UnknownTagException(localName);
            }
        /*} catch(ParseException e) {
            throw new TypeErrorException(localName);
        } */
        return false;
    }

    public BaseValue getAccountDate() {
        return accountDate;
    }

    public BaseValue<Date> getReportDate() {
        return reportDate;
    }

    public BaseValue getActualCreditCount() {
        return actualCreditCount;
    }

}

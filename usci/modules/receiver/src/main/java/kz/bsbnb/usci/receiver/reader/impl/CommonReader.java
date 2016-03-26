package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import kz.bsbnb.usci.receiver.helper.impl.ParserHelper;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.reader.IReader;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

public abstract class CommonReader<T> implements IReader<T> {
    @Autowired
    protected IServiceRepository serviceRepository;

    @Autowired
    protected ParserHelper parserHelper;

    @Autowired
    protected FileHelper fileHelper;

    @Value("#{jobParameters['batchId']}")
    protected Long batchId;

    @Value("#{jobParameters['userId']}")
    protected Long userId;

    @Value("#{jobParameters['reportId']}")
    protected Long reportId;

    @Value("#{jobParameters['actualCount']}")
    protected Long actualCount;

    @Value("#{jobParameters['creditorId']}")
    protected Long creditorId;

    XMLEventReader xmlEventReader;

    private static final long STEP_WAIT_TIMEOUT = 500;

    protected IBatchService batchService;

    IMetaFactoryService metaFactoryService;

    ReportBeanRemoteBusiness reportService;

    protected Batch batch;

    @Override
    public abstract T read() throws UnexpectedInputException, ParseException, NonTransientResourceException;

    private class ErrorHandlerImpl implements ErrorHandler {
        private boolean isValid = true;
        private String errMessage;

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            System.err.println("Предпреждение: " + exception.getException());
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            isValid = false;
            errMessage = exception.getMessage();
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            isValid = false;
            errMessage = exception.getMessage();
        }
    }

    boolean validateSchema(boolean isOriginal, ByteArrayInputStream xmlInputStream) throws IOException, SAXException {
        URL schemaURL;

        if (isOriginal) {
            schemaURL = getClass().getClassLoader().getResource("usci.xsd");
        } else {
            schemaURL = getClass().getClassLoader().getResource("credit-registry.xsd");
        }

        Source xml = new StreamSource(xmlInputStream);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        if (schemaURL == null)
            throw new IllegalStateException();

        Schema schema = schemaFactory.newSchema(schemaURL);

        Validator validator = schema.newValidator();

        ErrorHandlerImpl errorHandlerImpl = new ErrorHandlerImpl();
        validator.setErrorHandler(errorHandlerImpl);

        validator.validate(xml);

        if (!errorHandlerImpl.isValid) {
            batchService.addBatchStatus(new BatchStatus()
                    .setBatchId(batchId)
                    .setStatus(BatchStatuses.ERROR)
                    .setDescription("XML не прошёл проверку XSD: " + errorHandlerImpl.errMessage)
                    .setReceiptDate(new Date()));
        }

        return errorHandlerImpl.isValid;
    }

    void waitSync(IServiceRepository serviceFactory) {
        while (serviceFactory.getEntityService().getQueueSize() > ZipFilesMonitor.MAX_SYNC_QUEUE_SIZE) {
            try {
                Thread.sleep(STEP_WAIT_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

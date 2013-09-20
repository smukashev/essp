package kz.bsbnb.usci.receiver.reader.impl;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusArrayJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Stack;

/**
 * @author k.tulbassiyev
 */
@Component
@Scope("step")
public class CREntityReader<T> extends CommonReader<T> {
    private Logger logger = Logger.getLogger(CREntityReader.class);
    private Stack<IBaseContainer> stack = new Stack<IBaseContainer>();
    private IBaseContainer currentContainer;
    private Batch batch;

    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;

    private CouchbaseClient couchbaseClient;
    private Gson gson = new Gson();

    private BatchFullJModel batchFullJModel;

    @Autowired
    private MainParser crParser;

    @PostConstruct
    public void init() {
        logger.info("Reader init.");
        batchService = serviceRepository.getBatchService();
        metaFactoryService = serviceRepository.getMetaFactoryService();
        couchbaseClient = couchbaseClientFactory.getCouchbaseClient();

        int counter = 100;
        Object obj = null;

        while(counter-- > 0 && (obj = couchbaseClient.get("batch:" + batchId)) == null) {
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                break;
            }
        }

        if (obj == null) {
            statusSingleton.addBatchStatus(batchId,
                    new BatchStatusJModel(Global.BATCH_STATUS_ERROR, "Can't load batch from couchbase!", new Date()));

            throw new IllegalStateException("Can't load batch from couchbase!");
        }

        batchFullJModel = gson.fromJson(obj.toString(), BatchFullJModel.class);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(batchFullJModel.getContent());
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        batch = batchService.load(batchId);

        try
        {
            crParser.parse(xmlEventReader, batch, 0);
        } catch (SAXException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        logger.info("Read called");

        T entity = (T)crParser.getCurrentBaseEntity();

        long index = crParser.getIndex() - 1;

        if (crParser.hasMore()) {
            statusSingleton.addContractStatus(batchId, new ContractStatusJModel(index,
                    Global.CONTRACT_STATUS_PROCESSING, null, new Date()));
            try
            {
                crParser.parseNextPackage();
            } catch (SAXException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return null;
            }

            statusSingleton.addContractStatus(batchId, new ContractStatusJModel(index,
                    Global.CONTRACT_STATUS_COMPLETED, null, new Date()));

            return entity;
        }

        statusSingleton.addBatchStatus(batchId, new BatchStatusJModel(
                Global.BATCH_STATUS_COMPLETED, null, new Date()));

        //ContractStatusArrayJModel statusJModel = statusSingleton.endBatch(batchId);
        statusSingleton.endBatch(batchId);
        //batchFullJModel.setStatus(statusJModel);

        couchbaseClient.set("batch:" + batchId, 0, gson.toJson(batchFullJModel));

        return null;
    }

}

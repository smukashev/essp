package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import kz.bsbnb.usci.receiver.helper.impl.ParserHelper;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.reader.IReader;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.stream.XMLEventReader;

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

    protected XMLEventReader xmlEventReader;

    /* steps to wait sync, after throw exception */
    protected static final long TOTAL_WAIT_TIMEOUT = 3600;

    /* time for one step */
    protected static final long STEP_WAIT_TIMEOUT = 500;

    @Override
    public abstract T read() throws UnexpectedInputException, ParseException, NonTransientResourceException;

    public void waitSync(IServiceRepository serviceFactory) {
        int sleepCounter = 0;
        while (serviceFactory.getEntityService().getQueueSize() > ZipFilesMonitor.MAX_SYNC_QUEUE_SIZE) {
            try {
                Thread.sleep(STEP_WAIT_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sleepCounter++;
            if (sleepCounter > TOTAL_WAIT_TIMEOUT)
                throw new IllegalStateException(Errors.getMessage(Errors.E192));
        }
    }
}

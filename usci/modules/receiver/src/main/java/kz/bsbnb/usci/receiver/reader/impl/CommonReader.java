package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import kz.bsbnb.usci.receiver.helper.impl.ParserHelper;
import kz.bsbnb.usci.receiver.reader.IReader;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.stream.XMLEventReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author k.tulbassiyev
 */
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

    protected DateFormat dateFormat = new SimpleDateFormat(Global.DATE_FORMAT);

    @Override
    public abstract T read() throws UnexpectedInputException, ParseException, NonTransientResourceException;
}

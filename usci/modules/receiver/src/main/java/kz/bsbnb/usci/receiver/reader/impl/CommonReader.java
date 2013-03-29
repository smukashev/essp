package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import kz.bsbnb.usci.receiver.helper.impl.ParserHelper;
import kz.bsbnb.usci.receiver.reader.AbstractReader;
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
public abstract class CommonReader<T> implements AbstractReader<T> {
    @Autowired
    protected ICouchbaseClientFactory couchbaseClientFactory;

    @Autowired
    protected IServiceRepository serviceRepository;

    @Autowired
    protected ParserHelper parserHelper;

    @Autowired
    protected FileHelper fileHelper;

    /*@Value("#{jobParameters['fileName']}")
    protected String fileName;*/

    @Value("#{jobParameters['batchId']}")
    protected Long batchId;

    protected XMLEventReader xmlEventReader;

    protected DateFormat dateFormat = new SimpleDateFormat(Global.DATE_FORMAT);

    @Override
    public abstract T read() throws UnexpectedInputException,
            ParseException, NonTransientResourceException;
}

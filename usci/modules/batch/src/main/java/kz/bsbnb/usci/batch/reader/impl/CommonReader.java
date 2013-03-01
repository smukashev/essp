package kz.bsbnb.usci.batch.reader.impl;

import kz.bsbnb.usci.batch.common.Global;
import kz.bsbnb.usci.batch.helper.impl.FileHelper;
import kz.bsbnb.usci.batch.helper.impl.ParserHelper;
import kz.bsbnb.usci.batch.reader.AbstractReader;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
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
public abstract class CommonReader<T> implements AbstractReader<T>
{
    @Autowired
    protected IStorage storage;

    @Autowired
    protected IMetaClassDao metaClassDao;

    @Autowired
    protected IBatchDao batchDao;

    @Autowired
    protected IBaseEntityDao baseEntityDao;

    @Autowired
    protected IMetaFactory metaFactory;

    @Autowired
    protected ParserHelper parserHelper;

    @Autowired
    protected FileHelper fileHelper;

    @Value("#{jobParameters['fileName']}")
    protected String fileName;

    @Value("#{jobParameters['batchId']}")
    protected Long batchId;

    protected XMLEventReader xmlEventReader;

    protected DateFormat dateFormat = new SimpleDateFormat(Global.DATE_FORMAT);

    @Override
    public abstract T read() throws UnexpectedInputException,
            ParseException, NonTransientResourceException;
}

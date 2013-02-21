package kz.bsbnb.usci.batch.parser.impl;

import kz.bsbnb.usci.batch.helper.impl.FileHelper;
import kz.bsbnb.usci.batch.helper.impl.ParserHelper;
import kz.bsbnb.usci.batch.parser.IParser;
import kz.bsbnb.usci.batch.parser.IParserFactory;
import kz.bsbnb.usci.batch.parser.listener.IListener;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;

/**
 * @author k.tulbassiyev
 */
@Repository
public class MainParserFactory implements IParserFactory
{
    @Autowired
    IStorage storage;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBatchDao batchDao;

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IMetaFactory metaFactory;

    @Autowired
    FileHelper fileHelper;

    @Autowired
    ParserHelper parserHelper;

    public IParser getIParser(String fileName, Batch batch, IListener listener)
    {
        IParser parser = new MainParser(fileHelper.getFileBytes(new File(fileName)), batch);

        parser.setBaseEntityDao(baseEntityDao);
        parser.setBatchDao(batchDao);
        parser.setMetaClassDao(metaClassDao);
        parser.setMetaFactory(metaFactory);
        parser.setStorage(storage);
        parser.setParserHelper(parserHelper);
        parser.setListener(listener);

        return parser;
    }
}

package kz.bsbnb.usci.batch.parser;

import kz.bsbnb.usci.batch.common.Global;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author k.tulbassiyev
 */
public abstract class CommonParser extends DefaultHandler implements IParser
{
    protected XMLReader xmlReader;
    protected CharArrayWriter contents = new CharArrayWriter();
    protected DateFormat dateFormat = new SimpleDateFormat(Global.DATE_FORMAT);

    protected IStorage storage;
    protected IMetaClassDao metaClassDao;
    protected IBatchDao batchDao;
    protected IBaseEntityDao baseEntityDao;
    protected IMetaFactory metaFactory;

    protected Batch batch;

    public CommonParser()
    {
        super();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String str = new String(ch, start, length).trim();
        contents.write(str, 0, str.length());
    }

    @Override
    public abstract void parse() throws SAXException, IOException;

    public void setStorage(IStorage storage)
    {
        this.storage = storage;
    }

    @Override
    public void setMetaClassDao(IMetaClassDao metaClassDao)
    {
        this.metaClassDao = metaClassDao;
    }

    @Override
    public void setBatchDao(IBatchDao batchDao)
    {
        this.batchDao = batchDao;
    }

    @Override
    public void setBaseEntityDao(IBaseEntityDao baseEntityDao)
    {
        this.baseEntityDao = baseEntityDao;
    }

    @Override
    public void setMetaFactory(IMetaFactory metaFactory)
    {
        this.metaFactory = metaFactory;
    }
}

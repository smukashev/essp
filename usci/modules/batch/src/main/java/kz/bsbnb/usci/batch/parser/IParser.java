package kz.bsbnb.usci.batch.parser;

import kz.bsbnb.usci.batch.common.Global;
import kz.bsbnb.usci.batch.helper.impl.ParserHelper;
import kz.bsbnb.usci.batch.parser.listener.IListener;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.xml.sax.XMLReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author k.tulbassiyev
 */
public interface IParser
{
    public void parse() throws SAXException, IOException;

    public void setStorage(IStorage storage);
    public void setBatchDao(IBatchDao batchDao);
    public void setMetaClassDao(IMetaClassDao metaClassDao);
    public void setBaseEntityDao(IBaseEntityDao baseEntityDao);
    public void setMetaFactory(IMetaFactory metaFactory);
    public void setParserHelper(ParserHelper parserHelper);
    public void setListener(IListener listener);
}

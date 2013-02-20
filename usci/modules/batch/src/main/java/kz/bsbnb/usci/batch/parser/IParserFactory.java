package kz.bsbnb.usci.batch.parser;

import kz.bsbnb.usci.batch.parser.listener.IListener;
import kz.bsbnb.usci.eav.model.Batch;

/**
 * @author k.tulbassiyev
 */
public interface IParserFactory
{
    public IParser getIParser(String fileName, Batch batch, IListener listener);
}

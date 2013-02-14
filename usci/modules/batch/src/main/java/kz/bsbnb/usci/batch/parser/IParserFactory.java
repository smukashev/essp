package kz.bsbnb.usci.batch.parser;

/**
 * @author k.tulbassiyev
 */
public interface IParserFactory
{
    public IParser getIParser(String fileName);
}

package kz.bsbnb.usci.batch.parser.impl;

import kz.bsbnb.usci.batch.parser.CommonParser;
import kz.bsbnb.usci.batch.parser.listener.IListener;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.IBaseContainer;
import kz.bsbnb.usci.eav.model.batchdata.impl.BaseValue;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaSet;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 * @author k.tulbassiyev
 */
public class MainParser extends CommonParser
{
    private InputSource inputSource;

    private Logger logger = Logger.getLogger(MainParser.class);

    private Stack<IBaseContainer> stack = new Stack<IBaseContainer>();

    private IBaseContainer currentContainer;

    private int index = 1, level = 0;

    public MainParser(byte[] xmlBytes, Batch batch)
    {
        this.batch = batch;

        ByteArrayInputStream bStream = new ByteArrayInputStream(xmlBytes);
        inputSource = new InputSource(bStream);
    }

    public void parse() throws SAXException, IOException
    {
        xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(this);
        xmlReader.parse(inputSource);
    }

    @Override
    public void startDocument() throws SAXException
    {
        logger.info("started");
    }

    @Override
    public void endDocument() throws SAXException
    {
        logger.info("finished");
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
    {
        contents.reset();

        if(localName.equalsIgnoreCase("batch"))
        {
            logger.info("batch");
        }
        else if(localName.equalsIgnoreCase("entities"))
        {
            logger.info("entities");
        }
        else if(localName.equalsIgnoreCase("entity"))
        {
            currentContainer = metaFactory.getBaseEntity(attributes.getValue("class"));
        }
        else
        {
            IMetaType metaType = currentContainer.getMemberType(localName);

            if(metaType.isArray())
            {
                stack.push(currentContainer);
                currentContainer = metaFactory.getBaseSet(((MetaSet)metaType).getMemberType());
            }
            else if(!metaType.isArray() && metaType.isComplex())
            {
                stack.push(currentContainer);
                currentContainer = metaFactory.getBaseEntity((MetaClass)metaType);
            }

            level++;
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        if(localName.equalsIgnoreCase("batch"))
        {
            logger.info("batch");
        }
        else if(localName.equalsIgnoreCase("entities"))
        {
            logger.info("entities");
        }
        else if(localName.equalsIgnoreCase("entity"))
        {
            listener.put((BaseEntity)currentContainer);
            currentContainer = null;
            index++;
        }
        else
        {
            IMetaType metaType;

            if(level == stack.size())
                metaType = stack.peek().getMemberType(localName);
            else
                metaType = currentContainer.getMemberType(localName);

            Object o = null;

            if(!metaType.isComplex() && !metaType.isArray())
            {
                MetaValue metaValue = (MetaValue) metaType;

                try
                {
                    o = parserHelper.getCastObject(metaValue.getTypeCode(), contents.toString());
                }
                catch (NumberFormatException n)
                {
                    n.printStackTrace();
                }
            }
            else
            {
                o = currentContainer;
                currentContainer = stack.pop();
            }

            currentContainer.put(localName, new BaseValue(batch, index, o));

            level--;
        }
    }
}

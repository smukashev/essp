package kz.bsbnb.usci.batch.parser.impl;

import kz.bsbnb.usci.batch.common.Global;
import kz.bsbnb.usci.batch.parser.CommonParser;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author k.tulbassiyev
 */
public class MainParser extends CommonParser
{
    private InputSource inputSource;

    private Logger logger = Logger.getLogger(MainParser.class);

    private Stack<BaseEntity> stack = new Stack<BaseEntity>();

    private BaseEntity currentEntity;

    private int level = 0, index = 1;

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
            if(currentEntity != null)
                stack.push(currentEntity);

            currentEntity = metaFactory.getBaseEntity(attributes.getValue("class"), batch);
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
            BaseEntity entity;

            if(stack.size() == level)
            {
                try
                {
                    entity = stack.pop();
                }
                catch(EmptyStackException ex)
                {
                    entity = null;
                }

                if(entity == null)
                    throw new NullPointerException("Entity is NULL!");

                BaseEntity parentEntity;

                try
                {
                    parentEntity = stack.peek();
                }
                catch (EmptyStackException ex)
                {
                    parentEntity = null;
                }

                if(parentEntity != null)
                {
                    parentEntity.set(entity.getMeta().getClassName(), batch, index, entity);
                }
                else
                {
                    baseEntityDao.save(entity);
                    index++;
                }
            }
            else
            {
                try
                {
                    entity = stack.peek();
                }
                catch(EmptyStackException ex)
                {
                    entity = null;
                }

                if(entity == null)
                    throw new NullPointerException("Entity is NULL!");

                entity.set(currentEntity.getMeta().getClassName(), batch, index, currentEntity);
            }

            currentEntity = null;
            level--;
        }
        else
        {
            IMetaType metaType = currentEntity.getMeta().getMemberType(localName);

            Object o = null;

            if(!metaType.isArray())
            {
                MetaValue metaValue = (MetaValue) metaType;

                try
                {
                    o = getCastObject(metaValue.getTypeCode(), contents.toString());
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                catch (NumberFormatException n)
                {
                    n.printStackTrace();
                }
            }
            else
            {
                MetaValueArray metaValueArray = (MetaValueArray) metaType;

                try
                {
                    o = getCastObject(metaValueArray.getTypeCode(), contents.toString());
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                catch (NumberFormatException n)
                {
                    n.printStackTrace();
                }
            }

            currentEntity.set(localName, batch, index, o);
        }
    }

    public Object getCastObject(DataTypes typeCode, String value) throws ParseException
    {
        switch(typeCode)
        {
            case INTEGER:
                return Integer.parseInt(value);
            case DATE:
                return new SimpleDateFormat(Global.DATE_FORMAT).parse(value);
            case STRING:
                return value;
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case DOUBLE:
                return Double.parseDouble(value);
            default:
                throw new IllegalArgumentException("Unknown type. Can not be returned an appropriate class.");
        }
    }
}

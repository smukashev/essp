package kz.bsbnb.usci.batch.parser.impl;

import kz.bsbnb.usci.batch.parser.CommonParser;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
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

    /* statistic variablees */
    private Long parseTime1 = 0L, parseTime2 = 0L;
    private List<Long> parseTimeList = new ArrayList<Long>();
    private List<Long> saveTimeList = new ArrayList<Long>();
    private boolean parseInstalled = false;

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
        long totalParseTime = 0L, totalSaveTime = 0L;

        for (Long t : parseTimeList)
            totalParseTime += t;

        for (Long t : saveTimeList)
            totalSaveTime += t;

        logger.info("---------------------------------------------------");
        logger.info("[total save time]          :       " + totalSaveTime);
        logger.info("[total parse time]         :       " + totalParseTime);
        logger.info("[avg save time]            :       " + (totalSaveTime/saveTimeList.size()));
        logger.info("[avg parse time]           :       " + (totalParseTime/parseTimeList.size()));

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

            if(!parseInstalled)
            {
                parseTime1 = System.currentTimeMillis();
                parseInstalled = true;
            }
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
                    IMetaType metaType = parentEntity.getMeta().getMemberType(entity.getMeta().getClassName());

                    if(metaType.isArray())
                    {
                        parentEntity.addToArray(entity.getMeta().getClassName(), batch, index, entity);
                    }
                    else
                    {
                        parentEntity.set(entity.getMeta().getClassName(), batch, index, entity);
                    }
                }
                else
                {
                    // parse time
                    parseTime2 = System.currentTimeMillis();
                    logger.info("[parse entity]["+index+"]  :  " + (parseTime2 - parseTime1));
                    parseTimeList.add((parseTime2 - parseTime1));
                    parseInstalled = false;

                    // save time
                    long saveTime1 = System.currentTimeMillis();
                    baseEntityDao.save(entity);
                    long saveTime2 = System.currentTimeMillis();

                    logger.info("[save entity]["+index+"]   :  " + (saveTime2 - saveTime1));
                    saveTimeList.add((saveTime2 - saveTime1));

                    // do not append save time
                    parseTime1 = System.currentTimeMillis();
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

                IMetaType metaType = entity.getMeta().getMemberType(currentEntity.getMeta().getClassName());

                if(metaType.isArray())
                {
                    entity.addToArray(currentEntity.getMeta().getClassName(), batch, index, currentEntity);
                }
                else
                {
                    entity.set(currentEntity.getMeta().getClassName(), batch, index, currentEntity);
                }

            }

            currentEntity = null;
            level--;
        }
        else
        {
            boolean currentEntityInstalled = false;

            if(currentEntity == null)
            {
                currentEntity = stack.peek();
                currentEntityInstalled = true;
            }

            IMetaType metaType = currentEntity.getMeta().getMemberType(localName);

            Object o = null;

            if(!metaType.isArray())
            {
                MetaValue metaValue = (MetaValue) metaType;

                try
                {
                    o = parserHelper.getCastObject(metaValue.getTypeCode(), contents.toString());
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                catch (NumberFormatException n)
                {
                    n.printStackTrace();
                }

                currentEntity.set(localName, batch, index, o);

                if(currentEntityInstalled)
                    currentEntity = null;
            }
            else
            {
                MetaValueArray metaValueArray = (MetaValueArray) metaType;

                try
                {
                    o = parserHelper.getCastObject(metaValueArray.getTypeCode(), contents.toString());
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                catch (NumberFormatException n)
                {
                    n.printStackTrace();
                }

                currentEntity.addToArray(localName, batch, index, o);
            }
        }
    }
}

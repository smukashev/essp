package kz.bsbnb.usci.batch.parser.impl;

import kz.bsbnb.usci.batch.helper.impl.FileHelper;
import kz.bsbnb.usci.batch.parser.IParser;
import kz.bsbnb.usci.batch.parser.CommonParser;
import kz.bsbnb.usci.eav.model.BaseEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 * @author k.tulbassiyev
 */
public class MainParser extends CommonParser implements IParser
{
    private InputSource inputSource;

    private Logger logger = Logger.getLogger(MainParser.class);

    private Stack<BaseEntity> stack = new Stack<BaseEntity>();

    private List<BaseEntity> entities = new ArrayList<BaseEntity>();

    private BaseEntity currentEntity;

    private int level = 0;

    public MainParser(byte[] xmlBytes)
    {
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

            currentEntity = new BaseEntity(attributes.getValue("class"));
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
                    parentEntity.set(entity.getMeta().getClassName(), null, 0L, entity);
                }
                else
                {
                    entities.add(entity);
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

                entity.set(currentEntity.getMeta().getClassName(), null, 0L, currentEntity);
            }

            currentEntity = null;
            level--;
        }
        else
        {
            currentEntity.set(localName, null, 0L, contents.toString());
        }
    }
}

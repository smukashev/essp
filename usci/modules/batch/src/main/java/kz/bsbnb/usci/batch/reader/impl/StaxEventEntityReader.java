package kz.bsbnb.usci.batch.reader.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Stack;

/**
 * @author k.tulbassiyev
 */
@Component
@Scope("step")
public class StaxEventEntityReader<T> extends CommonReader<T>
{
    private Logger logger = Logger.getLogger(StaxEventEntityReader.class);
    private Stack<IBaseContainer> stack = new Stack<IBaseContainer>();
    private IBaseContainer currentContainer;
    private Batch batch;
    private int index = 1, level = 0;

    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;

    @PostConstruct
    public void init()
    {
        batchService = serviceRepository.getBatchService();
        metaFactoryService = serviceRepository.getMetaFactoryService();

        File file = new File(fileName);

        byte[] byteArray = fileHelper.getFileBytes(file);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try
        {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        batch = batchService.load(batchId);
    }

    public void startElement(XMLEvent event, StartElement startElement, String localName)
    {
        if(localName.equals("batch"))
        {
            logger.info("batch");
        }
        else if(localName.equals("entities"))
        {
            logger.info("entities");
        }
        else if(localName.equals("entity"))
        {
            currentContainer = metaFactoryService.getBaseEntity(
                    startElement.getAttributeByName(new QName("class")).getValue());
        }
        else
        {
            IMetaType metaType = currentContainer.getMemberType(localName);

            if(metaType.isSet())
            {
                stack.push(currentContainer);
                currentContainer = metaFactoryService.getBaseSet(((MetaSet)metaType).getMemberType());
                level++;
            }
            else if(metaType.isComplex() && !metaType.isSet())
            {
                stack.push(currentContainer);
                currentContainer = metaFactoryService.getBaseEntity((MetaClass)metaType);
                level++;
            }
            else if(!metaType.isComplex() && !metaType.isSet())
            {
                Object o = null;
                MetaValue metaValue = (MetaValue) metaType;

                try
                {
                    event = (XMLEvent) xmlEventReader.next();
                    o = parserHelper.getCastObject(metaValue.getTypeCode(), event.asCharacters().getData());
                    xmlEventReader.next();
                }
                catch (NumberFormatException n)
                {
                    n.printStackTrace();
                }

                currentContainer.put(localName, new BaseValue(batch, index, o));
            }
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException
    {

        while(xmlEventReader.hasNext())
        {
            XMLEvent event = (XMLEvent) xmlEventReader.next();

            if(event.isStartDocument())
            {
                logger.info("start document");
            }
            else if(event.isStartElement())
            {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                startElement(event, startElement, localName);
            }
            else if(event.isEndElement())
            {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if(localName.equals("batch"))
                {
                    logger.info("batch");
                }
                else if(localName.equals("entities"))
                {
                    logger.info("entities");
                }
                else if(localName.equals("entity"))
                {
                    T entity = (T) currentContainer;
                    currentContainer = null;
                    index++;

                    return entity;
                }
                else
                {
                    IMetaType metaType;

                    if(level == stack.size())
                        metaType = stack.peek().getMemberType(localName);
                    else
                        metaType = currentContainer.getMemberType(localName);

                    if(metaType.isComplex() || metaType.isSet())
                    {
                        Object o = currentContainer;
                        currentContainer = stack.pop();

                        currentContainer.put(localName, new BaseValue(batch, index, o));
                        level--;
                    }
                }
            }
            else if(event.isEndDocument())
            {
                logger.info("end document");
            }
            else
            {
                logger.info(event);
            }
        }

        return null;
    }
}

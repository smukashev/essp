package kz.bsbnb.usci.tool.xml.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.tool.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.tool.data.impl.MetaClassGenerator;
import kz.bsbnb.usci.tool.xml.AbstractXmlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public class XmlBaseEntityGenerator extends AbstractXmlGenerator
{
    private final static Logger logger = LoggerFactory.getLogger(XmlBaseEntityGenerator.class);

    private final static int dataSize = 15;

    private final static String FILE_PATH = "/opt/xmls/test.xml";

    public static void main(String args[]) throws ParserConfigurationException, TransformerException
    {
        MetaClassGenerator metaClassGenerator = new MetaClassGenerator(25, 2);
        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("stressApplicationContext.xml");

        File xmlFile = new File(FILE_PATH);

        if(!xmlFile.exists())
        {
            try
            {
                xmlFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);

        ArrayList<MetaClass> data = new ArrayList<MetaClass>();

        Document document;

        if(!storage.testConnection())
        {
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        storage.clear();
        storage.initialize();

        System.out.println("Generation: ..........");
        System.out.print(  "Progress  : ");

        for(int i = 0; i < dataSize; i++)
        {
            MetaClass metaClass = metaClassGenerator.generateMetaClass(0);

            long metaClassId = metaClassDao.save(metaClass);

            metaClass = metaClassDao.load(metaClassId);

            data.add(i, metaClass);

            if(i % (dataSize * 0.1) == 0)
                System.out.print(".");
        }

        System.out.println();

        // --------

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        document = documentBuilder.newDocument();

        Element batchElement = document.createElement("batch");
        Element entitiesElement = document.createElement("entities");

        document.appendChild(batchElement);

        Batch batch = new Batch(new Timestamp(new Date().getTime()));

        long index = 0L;

        for (MetaClass metaClass : data)
        {
            BaseEntity baseEntity = baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index);

            processBaseEntity(document, baseEntity, "entity", true, entitiesElement);
        }

        batchElement.appendChild(entitiesElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result =  new StreamResult(new File(FILE_PATH));
        transformer.transform(source, result);
    }

    public static void processBaseEntity(Document document, BaseEntity entity, String nameInParent, boolean firstTime, Element parentElement)
    {
        MetaClass meta = entity.getMeta();

        Element element = document.createElement(nameInParent);

        if(firstTime)
            element.setAttribute("class", entity.getMeta().getClassName());

        for (String name : meta.getMemberNames())
        {
            IMetaType metaType = meta.getMemberType(name);

            if(metaType.isComplex() && metaType.isArray())
            {
                Element arrayContainer = document.createElement(name);

                for (IBaseValue batchValue : (((BaseSet)entity.getBatchValue(name).getValue()).get()))
                {
                    BaseEntity memberEntity = (BaseEntity) batchValue.getValue();

                    processBaseEntity(document, memberEntity, "item", false, arrayContainer);
                }

                element.appendChild(arrayContainer);
            }
            else if(metaType.isComplex() && !metaType.isArray())
            {
                BaseEntity memberEntity = (BaseEntity) entity.getBatchValue(name).getValue();

                processBaseEntity(document, memberEntity, name, false, element);
            }
            else if(!metaType.isComplex() && metaType.isArray())
            {
                MetaSet metaSet = (MetaSet) metaType;

                Element arrayContainer = document.createElement(name);

                for (IBaseValue batchValue : (((BaseSet)entity.getBatchValue(name).getValue()).get()))
                {
                    Element childElement = document.createElement("item");

                    Object value = batchValue.getValue();

                    childElement.appendChild(document.createTextNode(
                            metaSet.getTypeCode() == DataTypes.DATE ?
                                    new SimpleDateFormat("yyyy-MM-dd").format(value)
                                    : value.toString()));

                    arrayContainer.appendChild(childElement);
                }

                element.appendChild(arrayContainer);
            }
            else if(!metaType.isComplex() && !metaType.isArray())
            {
                MetaValue metaValue = (MetaValue) metaType;

                Element childElement = document.createElement(name);

                Object value = entity.getBatchValue(name).getValue();

                childElement.appendChild(document.createTextNode(
                        metaValue.getTypeCode() == DataTypes.DATE ?
                                new SimpleDateFormat("yyyy-MM-dd").format(value)
                                : value.toString()));

                element.appendChild(childElement);
            }
        }

        parentElement.appendChild(element);
    }
}

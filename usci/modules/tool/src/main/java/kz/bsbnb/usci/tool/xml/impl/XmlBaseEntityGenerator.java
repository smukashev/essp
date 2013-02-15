package kz.bsbnb.usci.tool.xml.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.tool.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.tool.data.impl.MetaClassGenerator;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class XmlBaseEntityGenerator // extends AbstractXmlGenerator
{
    private final static Logger logger = LoggerFactory.getLogger(XmlBaseEntityGenerator.class);

    private final static int dataSize = 25;

    public static void main(String args[]) throws ParserConfigurationException, TransformerException
    {
        System.out.println("Test started at: " + Calendar.getInstance().getTime());

        MetaClassGenerator metaClassGenerator = new MetaClassGenerator(25, 2);
        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("stressApplicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);

        ArrayList<MetaClass> data = new ArrayList<MetaClass>();

        Document document = null;

        try
        {
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

                if(i % (dataSize / 10) == 0)
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
            // batchElement.appendChild(entitiesElement);

            Batch batch = new Batch(new Timestamp(new Date().getTime()));

            long index = 0L;

            for (MetaClass metaClass : data)
            {
                BaseEntity baseEntity = baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index);

                processBaseEntity(document, baseEntity, entitiesElement);

                batchElement.appendChild(entitiesElement);
            }
        }
        finally
        {
            storage.clear();
        }



        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result =  new StreamResult(System.out);
        transformer.transform(source, result);
    }

    public static void processBaseEntity(Document document, BaseEntity entity, Element parentElement)
    {
        MetaClass meta = entity.getMeta();

        for (String name : meta.getMemberNames())
        {
            IMetaType metaType = meta.getMemberType(name);

            if(metaType.isComplex())
            {
                if(metaType.isArray())
                {

                }
                else
                {

                }
            }
            else
            {
                if(metaType.isArray())
                {

                }
                else
                {

                }
            }
        }
    }
}

package kz.bsbnb.usci.cli.app;

import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.bconv.xsd.Xsd2MetaClass;
import kz.bsbnb.usci.brms.rulesingleton.RulesSingleton;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.model.impl.SimpleTrack;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchVersionService;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.impl.searcher.ImprovedBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.impl.BaseEntityXmlGenerator;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import kz.bsbnb.usci.tool.status.ReceiverStatus;
import kz.bsbnb.usci.tool.status.SyncStatus;
import org.jooq.SelectConditionStep;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class CLI
{
    private String command;
    private ArrayList<String> args = new ArrayList<String>();

    private static SimpleDateFormat sdfout = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private IStorage storage;

    @Autowired
    private IMetaClassDao metaClassDao;

    @Autowired
    protected IMetaClassRepository metaClassRepository;

    @Autowired
    protected IBatchRepository batchRepository;

    @Autowired
    private Xsd2MetaClass xsdConverter;

    @Autowired
    private MainParser crParser;

    @Autowired
    private IBaseEntityDao baseEntityDao;

    @Autowired
    private ImprovedBaseEntitySearcher searcher;

    @Autowired
    private RulesSingleton rulesSingleton;

    private BasicBaseEntityComparator comparator = new BasicBaseEntityComparator();

    private InputStream inputStream = null;

    public void processCRBatch(String fname, int count, int offset, Date repDate) throws SAXException, IOException, XMLStreamException
    {
        File inFile = new File(fname);

        InputStream in = null;
        in = new FileInputStream(inFile);

        System.out.println("Processing batch with rep date: " + repDate);

        Batch b = new Batch(repDate);
        b.setUserId(0L);

        Batch batch = batchRepository.addBatch(b);

        crParser.parse(in, batch);

        BaseEntity entity;
        int i = 0;
        while(crParser.hasMore() && (((i++) - offset) < count)) {
            if (i > offset) {
                entity = crParser.getCurrentBaseEntity();
                System.out.println(entity);
                long id = baseEntityDao.process(entity).getId();
                System.out.println("Saved with id: " + id);
            }

            if (i >= offset) {
                crParser.parseNextPackage();
            } else {
                crParser.skipNextPackage();
            }
        }

    }

    public void processXSD(String fname, String metaClassName) throws FileNotFoundException
    {
        File inFile = new File(fname);

        InputStream in = null;
        in = new FileInputStream(inFile);

        System.out.println("Parsing...");
        MetaClass meta = xsdConverter.convertXSD(in, metaClassName);

        System.out.println("Saving...");
        long id = metaClassDao.save(meta);

        System.out.println("Saved with id: " + id);
    }

    public void listXSD(String fname) throws FileNotFoundException
    {
        File inFile = new File(fname);

        InputStream in = null;
        in = new FileInputStream(inFile);

        System.out.println("Classes: ");
        ArrayList<String> names = xsdConverter.listClasses(in);

        for (String name : names) {
            System.out.println(name);
        }
    }

    public void showMetaClass(String name) {
        MetaClass meta = metaClassRepository.getMetaClass(name);

        if (meta == null) {
            System.out.println("No such meta class: " + name);
        } else {
            System.out.println(meta.toString());
        }
    }

    public void showMetaClass(long id) {
        MetaClass meta = metaClassRepository.getMetaClass(id);

        if (meta == null) {
            System.out.println("No such meta class with id: " + id);
        } else {
            System.out.println(meta.toString());
        }
    }

    public void toggleMetaClassKey(long id, String attrName) {
        MetaClass meta = metaClassRepository.getMetaClass(id);

        if (meta == null) {
            System.out.println("No such meta class with id: " + id);
        } else {
            IMetaAttribute attr = meta.getMetaAttribute(attrName);

            if (attr != null) {
                attr.setKey(!attr.isKey());
                metaClassRepository.saveMetaClass(meta);
            } else {
                System.out.println("No such attribute: " + attrName);
            }
        }
    }

    public void showMetaClassPaths(long id, String subMetaName) {
        MetaClass meta = metaClassRepository.getMetaClass(id);
        MetaClass subMeta = metaClassRepository.getMetaClass(subMetaName);

        if (meta == null) {
            System.out.println("No such meta class with id: " + id);
        } else {
            if (subMeta == null) {
                System.out.println("No such meta class with name: " + subMetaName);
            }

            List<String> paths = meta.getAllPaths(subMeta);

            for (String path : paths) {
                System.out.println(path);
            }
        }
    }

    public void toggleMetaClassKey(String className, String attrName) {
        MetaClass meta = metaClassRepository.getMetaClass(className);

        if (meta == null) {
            System.out.println("No such meta class with name: " + className);
        } else {
            IMetaAttribute attr = meta.getMetaAttribute(attrName);

            if (attr != null) {
                attr.setKey(!attr.isKey());
                metaClassRepository.saveMetaClass(meta);
            } else {
                System.out.println("No such attribute: " + attrName);
            }
        }
    }

    public void showMetaClassPaths(String className, String subMetaName) {
        MetaClass meta = metaClassRepository.getMetaClass(className);
        MetaClass subMeta = metaClassRepository.getMetaClass(subMetaName);

        if (meta == null) {
            System.out.println("No such meta class with name: " + className);
        } else {
            if (subMeta == null) {
                System.out.println("No such meta class with name: " + subMetaName);
            }

            List<String> paths = meta.getAllPaths(subMeta);

            for (String path : paths) {
                System.out.println(path);
            }
        }
    }

    public void addMetaClassKeyFilter(String className, String attrName, String subName, String value) {
        MetaClass meta = metaClassRepository.getMetaClass(className);

        if (meta == null) {
            System.out.println("No such meta class with name: " + className);
        } else {
            IMetaType attr = meta.getMemberType(attrName);

            if (attr != null) {
                if (attr.isSet() && attr.isComplex()) {
                    MetaSet set = (MetaSet)attr;

                    set.addArrayKeyFilter(subName, value);

                    metaClassRepository.saveMetaClass(meta);
                } else {
                    System.out.println("Attribute: " + attrName + " is not a complex set");
                }
            } else {
                System.out.println("No such attribute: " + attrName);
            }
        }
    }

    public void addMetaClassKeyFilter(long id, String attrName, String subName, String value) {
        MetaClass meta = metaClassRepository.getMetaClass(id);

        if (meta == null) {
            System.out.println("No such meta class with id: " + id);
        } else {
            IMetaType attr = meta.getMemberType(attrName);

            if (attr != null) {
                if (attr.isSet() && attr.isComplex()) {
                    MetaSet set = (MetaSet)attr;

                    set.addArrayKeyFilter(subName, value);

                    metaClassRepository.saveMetaClass(meta);
                } else {
                    System.out.println("Attribute: " + attrName + " is not a complex set");
                }
            } else {
                System.out.println("No such attribute: " + attrName);
            }
        }
    }

    public void showEntity(long id) {
        IBaseEntity entity = baseEntityDao.load(id);

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            System.out.println(entity.toString());
        }
    }

    public void dumpEntityToXML(String ids, String fileName) {
        StringTokenizer st = new StringTokenizer(ids, ",");
        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        while (st.hasMoreTokens()) {
            long id = Long.parseLong(st.nextToken());
            IBaseEntity entity = baseEntityDao.load(id);
            if (entity != null) {
                entities.add((BaseEntity)entity);
            }
        }

        if (entities.size() == 0) {
            System.out.println("No entities found with ids: " + ids);
        } else {
            BaseEntityXmlGenerator baseEntityXmlGenerator = new BaseEntityXmlGenerator();

            Document document = baseEntityXmlGenerator.getGeneratedDocument(entities);

            baseEntityXmlGenerator.writeToXml(document, fileName);
        }
    }

    public void readEntityFromXML(String fileName, String repDate) {
        try {
            Date reportDate = sdfout.parse(repDate);
            CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchRepository, reportDate);
            BaseEntity entity;
            while((entity = reader.read()) != null) {
                long id = baseEntityDao.process(entity).getId();
                System.out.println("Saved with id: " + id);
            }
        } catch (FileNotFoundException e)
        {
            System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Can't parse date " + repDate + " must be in format "+ sdfout.toString());
        }

    }

    public void showEntityAttr(String path, long id) {
        IBaseEntity entity = baseEntityDao.load(id);

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            Object value = entity.getEl(path);

            if (value != null) {
                System.out.println(value.toString());
            } else {
                System.out.println("No such attribute with path: " + path);
            }
        }
    }

    public void showEntityInter(long id1, long id2) {
        IBaseEntity entity1 = baseEntityDao.load(id1);
        IBaseEntity entity2 = baseEntityDao.load(id2);

        if (entity1 == null) {
            System.out.println("No such entity with id: " + id1);
        } else if (entity2 == null) {
            System.out.println("No such entity with id: " + id2);
        } else {
            List<String> inter = comparator.intersect((BaseEntity)entity1, (BaseEntity)entity2);

            for (String str : inter) {
                System.out.println(str);
            }
        }
    }

    public void showEntitySQ(long id) {
        IBaseEntity entity = baseEntityDao.load(id);

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            SelectConditionStep where = searcher.generateSQL(entity, null);

            if (where != null) {
                System.out.println(where.getSQL(true));
            } else {
                System.out.println("Error generating SQL.");
            }
        }
    }

    public void execEntitySQ(long id) {
        IBaseEntity entity = baseEntityDao.load(id);

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            //SelectConditionStep where = searcher.generateSQL(entity, null);
            ArrayList<Long> array = searcher.findAll((BaseEntity)entity);

            for (Long ids : array) {
                System.out.println(ids.toString());
            }
        }
    }

    public void execEntityByMetaId(String name) {
        MetaClass meta = metaClassRepository.getMetaClass(name);

        if (meta == null) {
            System.out.println("No such metaClass: " + name);
            return;
        }

        List<BaseEntity> entities = baseEntityDao.getEntityByMetaclass(meta);

        if (entities.size() == 0) {
            System.out.println("No such entities with class: " + name);
        } else {
            for (BaseEntity ids : entities) {
                System.out.println(ids.toString());
            }
        }
    }

    public void commandXSD() throws FileNotFoundException
    {
        if (args.size() > 1) {
            if (args.get(0).equals("list")) {
                listXSD(args.get(1));
            } else if (args.get(0).equals("convert")) {
                if (args.size() > 2) {
                    processXSD(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <list, convert> <fileName> <className>");
                }
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <list, convert> <fileName> [className]");
        }
    }

    public void commandCRBatch() throws IOException, SAXException, XMLStreamException, ParseException
    {
        if (args.size() > 2) {
            if (args.size() > 3) {
                processCRBatch(args.get(0), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)),
                    new Date(sdfout.parse(args.get(3)).getTime()));
            } else {
                processCRBatch(args.get(0), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)),
                        new Date((new java.util.Date()).getTime()));
            }
        } else {
            System.out.println("Argument needed: <fileName> <count> <offset>");
        }
    }

    private void createMetaClass(String metaName, boolean isRef, boolean isImmutable) {
        MetaClass meta = new MetaClass(metaName);
        meta.setReference(isRef);

        metaClassRepository.saveMetaClass(meta);
    }

    public void addAttributeToMeta(String metaName, String attrName, String type, String className, boolean arrayFlag,
                                   ComplexKeyTypes keyType, boolean isImmutable) {
        MetaClass meta = metaClassRepository.getMetaClass(metaName);

        if (type.equals("MetaClass")) {
            MetaClass toAdd = metaClassRepository.getMetaClass(className);

            if (!arrayFlag) {
                MetaAttribute metaAttr = new MetaAttribute(false, false, toAdd);
                metaAttr.setImmutable(isImmutable);
                meta.setMetaAttribute(attrName, metaAttr);
            } else {
                MetaSet setToAdd = new MetaSet(toAdd);
                if (keyType != null) {
                    setToAdd.setArrayKeyType(keyType);
                }
                meta.setMetaAttribute(attrName, new MetaAttribute(false, false, setToAdd));
            }
        } else {
            MetaValue value = new MetaValue(DataTypes.valueOf(type));

            if (!arrayFlag) {
                MetaAttribute metaAttr = new MetaAttribute(false, false, value);
                metaAttr.setImmutable(isImmutable);

                meta.setMetaAttribute(attrName, metaAttr);
            } else {
                MetaSet setToAdd = new MetaSet(value);
                if (keyType != null) {
                    setToAdd.setArrayKeyType(keyType);
                }
                meta.setMetaAttribute(attrName, new MetaAttribute(false, false, setToAdd));
            }
        }

        metaClassRepository.saveMetaClass(meta);
    }

    public void removeAttributeFromMeta(String metaName, String attrName) {
        MetaClass meta = metaClassRepository.getMetaClass(metaName);

        meta.removeMemberType(attrName);

        metaClassRepository.saveMetaClass(meta);
    }

    public void commandMeta()
    {
        if (args.size() > 1) {
            if (args.get(0).equals("show")) {
                if (args.get(1).equals("id")) {
                    showMetaClass(Long.parseLong(args.get(2)));
                } else if (args.get(1).equals("name")) {
                    showMetaClass(args.get(2));
                } else {
                    System.out.println("No such metaClass identification method: " + args.get(1));
                }
            } else if (args.get(0).equals("add")) {
                if (args.size() > 4) {
                    if(args.get(3).equals("MetaClass")) {
                        addAttributeToMeta(args.get(1), args.get(2), args.get(3), args.get(4),
                                args.size() > 5 ? Boolean.parseBoolean(args.get(5)) : false,
                                args.size() > 6 ? ComplexKeyTypes.valueOf(args.get(6)) : null,
                                args.size() > 7 ? Boolean.parseBoolean(args.get(7)) : false);
                    } else {
                        addAttributeToMeta(args.get(1), args.get(2), args.get(3), null,
                                args.size() > 4 ? Boolean.parseBoolean(args.get(4)) : false,
                                args.size() > 5 ? ComplexKeyTypes.valueOf(args.get(5)) : null,
                                args.size() > 6 ? Boolean.parseBoolean(args.get(6)) : false);
                    }
                } else {
                    addAttributeToMeta(args.get(1), args.get(2), args.get(3), null,
                            args.size() > 4 ? Boolean.parseBoolean(args.get(4)) : false, null,
                            args.size() > 5 ? Boolean.parseBoolean(args.get(5)) : false);
                }
            } else if (args.get(0).equals("remove")) {
                if (args.size() > 2) {
                    removeAttributeFromMeta(args.get(1), args.get(2));
                }
            } else if (args.get(0).equals("create")) {
                createMetaClass(args.get(1),
                        args.size() > 2 ? Boolean.parseBoolean(args.get(2)) : false,
                        args.size() > 3 ? Boolean.parseBoolean(args.get(3)) : false);
            } else if (args.get(0).equals("delete")) {
                System.out.println("Unimplemented stub in cli");
            } else if (args.get(0).equals("key")) {
                if (args.size() > 3) {
                    if (args.get(1).equals("id")) {
                        toggleMetaClassKey(Long.parseLong(args.get(2)), args.get(3));
                    } else if (args.get(1).equals("name")) {
                        toggleMetaClassKey(args.get(2), args.get(3));
                    } else {
                        System.out.println("No such metaClass identification method: " + args.get(1));
                    }
                } else {
                    System.out.println("Argument needed: <key> <id, name> <id or name> <attributeName>");
                }
            } else if (args.get(0).equals("paths")) {
                if (args.size() > 3) {
                    if (args.get(1).equals("id")) {
                        showMetaClassPaths(Long.parseLong(args.get(2)), args.get(3));
                    } else if (args.get(1).equals("name")) {
                        showMetaClassPaths(args.get(2), args.get(3));
                    } else {
                        System.out.println("No such metaClass identification method: " + args.get(1));
                    }
                } else {
                    System.out.println("Argument needed: <paths> <id, name> <id or name> <attributeName>");
                }
            } else if (args.get(0).equals("fkey")) {
                if (args.size() > 5) {
                    if (args.get(1).equals("id")) {
                        addMetaClassKeyFilter(Long.parseLong(args.get(2)), args.get(3), args.get(4), args.get(5));
                    } else if (args.get(1).equals("name")) {
                        addMetaClassKeyFilter(args.get(2), args.get(3), args.get(4), args.get(5));
                    } else {
                        System.out.println("No such metaClass identification method: " + args.get(1));
                    }
                } else {
                    System.out.println("Argument needed: <fkey> <id, name> <id or name> <attributeName> " +
                            "<subAttributeName> <filterValue>");
                }
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <show, key, paths, create> <id, name, className> <id or name> " +
                    "[attributeName, subClassName]");
        }
    }

    public Connection connectToDB(String url, String name, String password) throws ClassNotFoundException, SQLException
    {
        Class.forName("oracle.jdbc.OracleDriver");
        return DriverManager.getConnection(url, name, password);
    }

    public void commandImport()
    {
        if (args.size() > 4) {
            Connection conn = null;

            RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

            IBatchProcessService batchProcessService = null;

            try {
                batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
                //batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/batchEntryService");
                batchProcessServiceFactoryBean.setServiceUrl(args.get(3));
                batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
                batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

                batchProcessServiceFactoryBean.afterPropertiesSet();
                batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
            }

            try
            {
                conn = connectToDB(args.get(0), args.get(1), args.get(2));
            } catch (ClassNotFoundException e)
            {
                System.out.println("Error can't load driver: oracle.jdbc.OracleDriver");
                return;
            } catch (SQLException e)
            {
                System.out.println("Can't connect to DB: " + e.getMessage());
                return;
            }

            PreparedStatement preparedStatement = null;
            PreparedStatement preparedStatementDone = null;
            try
            {
                preparedStatement = conn.prepareStatement("SELECT xf.id, xf.file_name, xf.file_content\n" +
                        "  FROM core.xml_file xf\n" +
                        " WHERE xf.status = 'COMPLETED'\n" +
                        "   AND xf.sent = 0 ORDER BY xf.id ASC");

                preparedStatementDone = conn.prepareStatement("UPDATE core.xml_file xf \n" +
                        "   SET xf.sent = ? \n" +
                        " WHERE xf.id = ?");
            } catch (SQLException e)
            {
                System.out.println("Can't create prepared statement: " + e.getMessage());
                try
                {
                    conn.close();
                } catch (SQLException e1)
                {
                    e1.printStackTrace();
                }
                return;
            }

            File tempDir = new File(args.get(4));

            if (!tempDir.exists()) {
                System.out.println("No such directory " + args.get(4));
                return;
            }

            if (!tempDir.isDirectory()) {
                System.out.println(args.get(4) + " must be a directory");
                return;
            }

            int fileLimit = -1;
            if (args.size() > 5) {
                fileLimit = Integer.parseInt(args.get(5));
            }
            int fileNumber = 0;

            while(true) {
                if (fileLimit > 0) {
                    if (fileNumber >= fileLimit) {
                        break;
                    }
                }
                fileNumber++;

                ResultSet result2 = null;
                try
                {
                    result2 = preparedStatement.executeQuery();
                } catch (SQLException e)
                {
                    System.out.println("Can't execute db query: " + e.getMessage());
                    break;
                }

                int id = 0;

                try
                {
                    if (result2.next()) {
                        id = result2.getInt("id");
                        String fileName = result2.getString("file_name");
                        Blob blob = result2.getBlob("file_content");

                        File newFile = new File(tempDir.getAbsolutePath() + "/" + fileName + ".zip");
                        newFile.createNewFile();

                        InputStream in = blob.getBinaryStream();

                        byte[] buffer = new byte[1024];

                        FileOutputStream fout = new FileOutputStream(newFile);

                        while(in.read(buffer) > 0) {
                            fout.write(buffer);
                        }

                        fout.close();

                        System.out.println(fileNumber + " - Sending file: " + newFile.getCanonicalFile());

                        batchProcessService.processBatchWithoutUser(newFile.getAbsolutePath());

                        preparedStatementDone.setInt(1, 1);
                        preparedStatementDone.setInt(2, id);

                        if (preparedStatementDone.execute()) {
                            System.out.println("Error can't mark sent file: " + id);
                        }

                        Thread.sleep(10000);
                    } else {
                        System.out.println("Nothing to do.");
                        Thread.sleep(10000);
                    }
                } catch (SQLException e)
                {
                    System.out.println("Can't get result from db: " + e.getMessage());
                    break;
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    System.out.println("Can't create temp file: " + e.getMessage());
                } catch (Exception e)
                {
                    e.printStackTrace();
                    try {
                        preparedStatementDone.setInt(1, 1);
                        preparedStatementDone.setInt(2, id);

                        if (preparedStatementDone.execute()) {
                            System.out.println("Error can't mark sent file: " + id);
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        } else {
            System.out.println("Argument needed: <credits_db_url> <user> <password> <receiver_url> <temp_files_folder>");
            System.out.println("Example: import jdbc:oracle:thin:@srv-scan.corp.nb.rk:1521/DBM01 core ***** rmi://127.0.0.1:1097/batchProcessService D:\\usci\\temp_xml_folder");
            System.out.println("Example: import jdbc:oracle:thin:@192.168.0.44:1521/CREDITS core core_feb_2013 rmi://127.0.0.1:1097/batchProcessService /home/a.tkachenko/temp_files");
        }
    }

    public void commandRStat()
    {
        if (args.size() > 0) {
            RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

            IBatchProcessService batchProcessService = null;

            try {
                batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
                //batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/batchEntryService");
                batchProcessServiceFactoryBean.setServiceUrl(args.get(0));
                batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
                batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

                batchProcessServiceFactoryBean.afterPropertiesSet();
                batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();

                ReceiverStatus rs = batchProcessService.getStatus();

                System.out.println(rs.toString());
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Argument needed: <receiver_url>");
            System.out.println("Example: rstat rmi://127.0.0.1:1097/batchProcessService");
        }
    }

    public void commandSStat()
    {
        if (args.size() > 0) {
            RmiProxyFactoryBean entityServiceFactoryBean = null;

            kz.bsbnb.usci.sync.service.IEntityService entityService = null;

            try {
                entityServiceFactoryBean = new RmiProxyFactoryBean();
                //batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/batchEntryService");
                entityServiceFactoryBean.setServiceUrl(args.get(0));
                entityServiceFactoryBean.setServiceInterface(kz.bsbnb.usci.sync.service.IEntityService.class);
                entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

                entityServiceFactoryBean.afterPropertiesSet();
                entityService = (kz.bsbnb.usci.sync.service.IEntityService) entityServiceFactoryBean.getObject();

                SyncStatus rs = entityService.getStatus();

                System.out.println(rs.toString());
            } catch (Exception e) {
                System.out.println("Can't connect to sync service: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Argument needed: <receiver_url>");
            System.out.println("Example: sstat rmi://127.0.0.1:1098/entityService");
        }
    }

    public void commandSQLStat()
    {
        if (args.size() > 0) {
            Connection conn = null;

            RmiProxyFactoryBean serviceFactory = null;

            IEntityService entityService = null;

            try {
                serviceFactory = new RmiProxyFactoryBean();
                //batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/batchEntryService");
                serviceFactory.setServiceUrl(args.get(0));
                serviceFactory.setServiceInterface(IEntityService.class);
                serviceFactory.setRefreshStubOnConnectFailure(true);

                serviceFactory.afterPropertiesSet();
                entityService = (IEntityService) serviceFactory.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
            }

            HashMap<String, QueryEntry> map = entityService.getSQLStats();

            System.out.println();
            System.out.println("+---------+------------------+------------------------+");
            System.out.println("|  count  |     avg (ms)     |       total (ms)       |");
            System.out.println("+---------+------------------+------------------------+");

            double totalInserts = 0;
            double totalSelects = 0;
            double totalProcess = 0;
            int totalProcessCount = 0;

            for (String query : map.keySet()) {
                QueryEntry qe = map.get(query);

                System.out.printf("| %7d | %16.6f | %22.6f | %s%n", qe.count,
                        qe.totalTime / qe.count, qe.totalTime, query);

                if (query.startsWith("insert")) {
                    totalInserts += qe.totalTime;
                }
                if (query.startsWith("select")) {
                    totalSelects += qe.totalTime;
                }
                if (query.startsWith("coreService")) {
                    totalProcess += qe.totalTime;
                    totalProcessCount += qe.count;
                }
            }

            System.out.println("+---------+------------------+------------------------+");

            if(totalProcessCount > 0) {
                System.out.println("AVG process: " + totalProcess / totalProcessCount);
                System.out.println("AVG inserts per process: " + totalInserts / totalProcessCount);
                System.out.println("AVG selects per process: " + totalSelects / totalProcessCount);
            }

            entityService.clearSQLStats();

        } else {
            System.out.println("Argument needed: <core_url>");
            System.out.println("Example: sqlstat rmi://127.0.0.1:1099/entityService");
        }
    }

    public void commandSTC()
    {
        if (args.size() > 2) {
            Connection conn = null;

            RmiProxyFactoryBean serviceFactory = null;

            kz.bsbnb.usci.sync.service.IEntityService entityService = null;

            try {
                serviceFactory = new RmiProxyFactoryBean();
                //batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/batchEntryService");
                serviceFactory.setServiceUrl(args.get(0));
                serviceFactory.setServiceInterface(kz.bsbnb.usci.sync.service.IEntityService.class);
                serviceFactory.setRefreshStubOnConnectFailure(true);

                serviceFactory.afterPropertiesSet();
                entityService = (kz.bsbnb.usci.sync.service.IEntityService) serviceFactory.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
            }

            int tCount = Integer.parseInt(args.get(1));
            boolean allowAutoIncrement = Boolean.parseBoolean(args.get(2));

            entityService.setThreadsCount(tCount, allowAutoIncrement);
        } else {
            System.out.println("Argument needed: <core_url> <threads_count> <allow_auto_increment>");
            System.out.println("Example: stc rmi://127.0.0.1:1098/entityService 32 false");
        }
    }

    public void commandEntity()
    {
        if (args.size() > 1) {
            if (args.get(0).equals("show")) {
                if (args.get(1).equals("id")) {
                    showEntity(Long.parseLong(args.get(2)));
                } else if (args.get(1).equals("attr")) {
                    if (args.size() > 3) {
                        showEntityAttr(args.get(3), Long.parseLong(args.get(2)));
                    } else {
                        System.out.println("Argument needed: <show> <attr> <id> <attributePath>");
                    }
                } else if (args.get(1).equals("inter")) {
                    if (args.size() > 3) {
                        showEntityInter(Long.parseLong(args.get(2)), Long.parseLong(args.get(3)));
                    } else {
                        System.out.println("Argument needed: <show> <inter> <id1> <id2>");
                    }
                } else if (args.get(1).equals("sq")) {
                    if (args.size() > 2) {
                        showEntitySQ(Long.parseLong(args.get(2)));
                    } else {
                        System.out.println("Argument needed: <show> <sq> <id> <attributePath>");
                    }
                } else if (args.get(1).equals("eq")) {
                    if (args.size() > 2) {
                        execEntitySQ(Long.parseLong(args.get(2)));
                    } else {
                        System.out.println("Argument needed: <show> <sq> <id> <attributePath>");
                    }
                } else if (args.get(1).equals("bymeta")) {
                    if (args.size() > 2) {
                        execEntityByMetaId(args.get(2));
                    } else {
                        System.out.println("Argument needed: <show> <bymeta> <metaName>");
                    }
                } else {
                    System.out.println("No such entity identification method: " + args.get(1));
                }
            } else if(args.get(0).equals("xml")) {
                if (args.size() > 2) {
                    dumpEntityToXML(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <xml> <id> <fileName>");
                }
            } else if(args.get(0).equals("read")) {
                if (args.size() > 2) {
                    readEntityFromXML(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <read> <fileName> <rep_date>");
                }
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <show, read> <id, attr, sq, inter> <id> [attributePath, id2]");
        }
    }

    public void commandTest()
    {
        if (storage.testConnection()) {
            System.out.println("Connected to DB.");
        }

        try {
            if (storage.isClean()) {
                System.out.println("DB is empty");
            } else {
                System.out.println("DB with data");
            }
        } catch(BadSqlGrammarException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("EAV structure might be corrupted. Try clear/init.");
        }
    }

    public void commandSql(){

        System.out.println("ok sql mode...");
        StringBuilder str = new StringBuilder();
        for(Object o : args)
            str.append(o+" ");
        System.out.println(str.toString());
        boolean res = storage.simpleSql(str.toString());
        System.out.println( res?"success":"fail");

    }

    public void commandRefs(){
        if(args.get(0).equals("import")){
            if(args.size() > 1)
                BaseCrawler.fileName = args.get(1);
            else {
                BaseCrawler.fileName = "C:\\entity_show\\mine";
                System.out.println("using default file " + BaseCrawler.fileName);
            }
            new BaseRepository().run();
        } else throw new IllegalArgumentException("allowed operations refs [import] [filename]");
    }

    private RmiProxyFactoryBean batchServiceFactoryBean;
    private RmiProxyFactoryBean batchVersionServiceFactoryBean;
    private RmiProxyFactoryBean ruleServiceFactoryBean;
    private RmiProxyFactoryBean listenerServiceFactoryBean;

    private RmiProxyFactoryBean entityServiceFactoryBean;

    private IBatchService batchService;
    private IRuleService ruleService;
    private IBatchVersionService batchVersionService;

    public void init(){


        try {
            rulesSingleton.reloadCache();

            entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
            entityServiceFactoryBean.setServiceInterface(IBaseEntityDao.class);

            entityServiceFactoryBean.afterPropertiesSet();


            batchServiceFactoryBean = new RmiProxyFactoryBean();
            batchServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchService");
            batchServiceFactoryBean.setServiceInterface(IBatchService.class);

            batchServiceFactoryBean.afterPropertiesSet();
            batchService = (IBatchService) batchServiceFactoryBean.getObject();

            batchVersionServiceFactoryBean = new RmiProxyFactoryBean();
            batchVersionServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchVersionService");
            batchVersionServiceFactoryBean.setServiceInterface(IBatchVersionService.class);

            batchVersionServiceFactoryBean.afterPropertiesSet();
            batchVersionService = (IBatchVersionService) batchVersionServiceFactoryBean.getObject();

            ruleServiceFactoryBean = new RmiProxyFactoryBean();
            ruleServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/ruleService");
            ruleServiceFactoryBean.setServiceInterface(IRuleService.class);

            ruleServiceFactoryBean.afterPropertiesSet();
            ruleService = (IRuleService) ruleServiceFactoryBean.getObject();
        } catch (Exception e) {
            System.out.println("Can\"t initialise services: " + e.getMessage());
        }

    }

    private Rule currentRule;
    private String currentPackageName = "afk";
    private Date currentDate = new Date();
    private boolean started = false;
    private BatchVersion currentBatchVersion;
    private String defaultDumpFile = "c:/rules/pledge2.cli";
    private String defaultEntityFile = "c:/rules/baseEntityId.txt";
    private BaseEntity currentBaseEntity;



    public void commandRule(Scanner in){
        if(!started)
        {
            init();
            started = true;
            try {
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        try{
        if(args.get(0).equals("debug")){

            DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
            Date date = dateFormatter.parse("01.03.2014");

            try {
                CLIXMLReader reader = new CLIXMLReader("c:/a.xml", metaClassRepository, batchRepository, date);
                BaseEntity baseEntity = reader.read();
                System.out.println(baseEntity);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if(args.get(0).equals("dump")){
            if(args.size() < 2)
                System.out.println("using default dump file path " + defaultDumpFile);
            try {
                PrintWriter out = new PrintWriter( args.size() < 2 ? defaultDumpFile : args.get(1));
                List<Rule> rules = ruleService.getAllRules();

                out.println("#rule set date 01.04.2013");
                out.println("rule create package afk");
                out.println("rule rc");
                out.println("rule set package afk");
                out.println("rule set version\n");
                for(Rule r: rules){
                    out.println("rule read $$$");
                    out.println("title: " + r.getTitle());
                    out.println(r.getRule());
                    out.println("$$$\n");
                    out.println("rule save\n");
                }
                out.println("quit");
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }else
        if(args.get(0).equals("read")){
            if(args.size() < 2){
                throw new IllegalArgumentException();
            }else{
                System.out.println("reading until "+args.get(1) +"...");
                StringBuilder sb = new StringBuilder();
                line = in.nextLine();
                if(!line.startsWith("title: "))
                    throw new IllegalArgumentException("title must be specified format title: <name>");
                String title = line.split("title: ")[1];
                line = in.nextLine();
                if(line.startsWith(args.get(1)))
                    throw new IllegalArgumentException("rule must not be empty");
                sb.append(line);
                do{
                    line = in.nextLine();
                    if(line.startsWith(args.get(1))) break;
                    sb.append("\n" + line);
                }while(true);
                currentRule = new Rule();
                currentRule.setRule(sb.toString());
                currentRule.setTitle(title);
            }
        } else if(args.get(0).equals("current")){
            if(args.size() == 1)
                System.out.println( currentRule ==null?null: currentRule.getRule());
            else if(args.get(1).equals("version"))
                System.out.println(currentBatchVersion);
            else if(args.get(1).equals("package"))
                System.out.println(currentPackageName);
            else if(args.get(1).equals("date"))
                System.out.println(currentDate);
            else if(args.get(1).equals("entity"))
                System.out.println(currentBaseEntity);
            else throw new IllegalArgumentException();
        } else if(args.get(0).equals("save")){
            long ruleId = ruleService.createNewRuleInBatch(currentRule, currentBatchVersion);
            System.out.println("ok saved: ruleId = " + ruleId);
        }else if(args.get(0).equals("run")){

            DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
            Date reportDate = dateFormatter.parse("01.03.2014");

            try {
                CLIXMLReader reader = new CLIXMLReader("c:/a.xml", metaClassRepository, batchRepository, reportDate);
                currentBaseEntity = reader.read();
                rulesSingleton.runRules(currentBaseEntity,currentPackageName,currentDate);

                for (String s: currentBaseEntity.getValidationErrors())
                    System.out.println("Validation error:" + s);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }else if(args.get(0).equals("set")){

             if(args.get(1).equals("version")){
               currentBatchVersion = batchVersionService.getBatchVersion(currentPackageName, currentDate);
             }else if(args.size() < 3) throw new IllegalArgumentException();
             else if(args.get(1).equals("package"))
               {
                   rulesSingleton.reloadCache();
                   rulesSingleton.getRulePackageName(args.get(2),currentDate);
                   currentPackageName = args.get(2);
               }
               else if(args.get(1).equals("date"))
               {
                   DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                   currentDate = formatter.parse(args.get(2));
               } else throw new IllegalArgumentException();
        } else if(args.get(0).equals("create")){
            try{
               if(!args.get(1).equals("package") || args.size()<3) throw new IllegalArgumentException();
            } catch (IllegalArgumentException e){
                throw e;
            }
            kz.bsbnb.usci.brms.rulesvr.model.impl.Batch batch = new kz.bsbnb.usci.brms.rulesvr.model.impl.Batch(args.get(2),currentDate);
            Long id = batchService.save(batch);
            batch.setId(id);
            batchVersionService.save(batch);
            System.out.println("ok batch created with id:"+id);
        } else if(args.get(0).equals("rc")){
            rulesSingleton.reloadCache();
        } else if( args.get(0).equals("eval")){
            System.out.println(currentBaseEntity.getEls(args.get(1)));
        } else if( args.get(0).equals("eval2")){
            System.out.println(currentBaseEntity.getEl(args.get(1)));
        }  else throw new IllegalArgumentException();
        }catch(IllegalArgumentException e){
            if(e.getMessage()==null)
              System.out.println("Argument needed: <read {label},current [<pckName,date>],save,run {id},set <package,date> {value} , create package {pckName}>");
            else
              System.out.println(e.getMessage());
            return;
        } catch (ParseException e) {
            System.out.println("Parse exception day format must be: dd.MM.yyyy");
            return;
        }  catch (IncorrectResultSizeDataAccessException e){
            System.out.println("Such rule already exists");
            return;
        }

        //rulesSingleton.runRules(entity, entity.getMeta().getClassName() + "_parser", entity.getReportDate());*/
    }

    String line;

    public void run() {
        if (storage.testConnection()) {
            System.out.println("Connected to DB.");
        }

        System.out.println("Waiting for commands.");
        System.out.print("> ");

        Scanner in;

        if (inputStream == null) {
            in = new Scanner(System.in);
        } else {
            in = new Scanner(inputStream);
        }


        Exception lastException = null;

        while(true) {
            /*
             //args.clear(); args.add("c:\\2_portfolio.xml"); args.add("2"); args.add("0");
            args.clear(); args.add("c:\\1.xml"); args.add("2"); args.add("49");
            //args.clear(); args.add("C:\\Projects\\usci\\usci\\modules\\cli\\src\\main\\resources\\test_batch.xml"); args.add("2"); args.add("0");
             try{
                commandCRBatch();
                 if(1==1) break;
             } catch(Exception e){
                 //System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             */
            while ( !(line = in.nextLine()).equals("quit")) {
                StringTokenizer st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    command = st.nextToken().trim();

                    args.clear();
                    while(st.hasMoreTokens()) {
                        args.add(st.nextToken().trim());
                    }
                } else {
                    continue;
                }


                if (command.startsWith("#")) {
                    continue;
                }

                try {

                    if (command.equals("test")) {
                        commandTest();
                    } else if (command.equals("clear")) {
                        storage.clear();
                    } else if (command.equals("rc")) {
                        metaClassRepository.resetCache();
                    } else if (command.equals("init")) {
                        storage.initialize();
                    } else if (command.equals("tc")) {
                        storage.tableCounts();
                    } else if (command.equals("le")) {
                        if (lastException != null) {
                            lastException.printStackTrace();
                        } else {
                            System.out.println("No errors.");
                        }
                    } else if (command.equals("xsd")) {
                        commandXSD();
                    } else if (command.equals("crbatch")) {
                        commandCRBatch();
                    } else if (command.equals("meta")) {
                        commandMeta();
                    } else if (command.equals("entity")) {
                        commandEntity();
                    } else if(command.equals("refs")){
                        commandRefs();
                    } else if(command.equals("sql")){
                        commandSql();
                    } else if(command.equals("rule")) {
                        commandRule(in);
                    } else if(command.equals("import")) {
                        commandImport();
                    } else if(command.equals("rstat")) {
                        commandRStat();
                    } else if(command.equals("sstat")) {
                        commandSStat();
                    } else if(command.equals("sqlstat")) {
                        commandSQLStat();
                    } else if(command.equals("stc")) {
                        commandSTC();
                    } else {
                        System.out.println("No such command: " + command);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    lastException = e;
                }
                if (inputStream == null) {
                    System.out.print("> ");
                }
            }
            if (inputStream == null) break;
            else {
                in = new Scanner(System.in);
                System.out.println();
                System.out.println("Done. Awaiting commands from cli.");
                inputStream = null;
            }
        }
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream(InputStream in)
    {
        this.inputStream = in;
    }
}

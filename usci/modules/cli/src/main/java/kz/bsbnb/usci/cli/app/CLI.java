package kz.bsbnb.usci.cli.app;

import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.bconv.xsd.Xsd2MetaClass;
import kz.bsbnb.usci.brms.rulesingleton.RulesSingleton;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchVersionService;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.impl.BaseEntityXmlGenerator;
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
import java.text.DateFormat;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
    private BasicBaseEntitySearcher searcher;

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

    public void readEntityFromXML(String fileName) {
        try {
            CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchRepository);
            BaseEntity entity;
            while((entity = reader.read()) != null) {
                long id = baseEntityDao.process(entity).getId();
                System.out.println("Saved with id: " + id);
            }
        } catch (FileNotFoundException e)
        {
            System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
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

    private void createMetaClass(String metaName) {
        MetaClass meta = new MetaClass(metaName);

        metaClassRepository.saveMetaClass(meta);
    }

    public void addAttributeToMeta(String metaName, String attrName, String type, String className, boolean arrayFlag) {
        MetaClass meta = metaClassRepository.getMetaClass(metaName);

        if (type.equals("MetaClass")) {
            MetaClass toAdd = metaClassRepository.getMetaClass(className);

            if (!arrayFlag) {
                meta.setMetaAttribute(attrName, new MetaAttribute(false, false, toAdd));
            } else {
                meta.setMetaAttribute(attrName, new MetaAttribute(false, false, new MetaSet(toAdd)));
            }
        } else {
            MetaValue value = new MetaValue(DataTypes.valueOf(type));

            if (!arrayFlag) {
                meta.setMetaAttribute(attrName, new MetaAttribute(false, false, value));
            } else {
                meta.setMetaAttribute(attrName, new MetaAttribute(false, false, new MetaSet(value)));
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
                    addAttributeToMeta(args.get(1), args.get(2), args.get(3), args.get(4),
                            args.size() > 5 ? Boolean.parseBoolean(args.get(5)) : false);
                } else {
                    addAttributeToMeta(args.get(1), args.get(2), args.get(3), null,
                            args.size() > 4 ? Boolean.parseBoolean(args.get(4)) : false);
                }
            } else if (args.get(0).equals("remove")) {
                if (args.size() > 2) {
                    removeAttributeFromMeta(args.get(1), args.get(2));
                }
            } else if (args.get(0).equals("create")) {
                createMetaClass(args.get(1));
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
                if (args.size() > 1) {
                    readEntityFromXML(args.get(1));
                } else {
                    System.out.println("Argument needed: <read> <fileName>");
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
    private String currentPackageName;
    private Date currentDate = new Date();
    private boolean started = false;



    public void commandRule(Scanner in){
        if(!started)
        {
            init();
            started = true;
        }

        try{
        if(args.get(0).equals("read")){
            if(args.size() < 2){
                throw new IllegalArgumentException();
            }else{
                System.out.println("reading until "+args.get(1) +"...");
                StringBuilder sb = new StringBuilder();
                do{
                    sb.append(line+"\n");
                    line = in.nextLine();
                    if(line.startsWith(args.get(1))) break;
                }while(true);
                line = in.nextLine();
                currentRule = new Rule();
                currentRule.setRule(sb.toString());
                currentRule.setTitle("sample");
            }
        } else if(args.get(0).equals("current")){
            if(args.size() == 1)
                System.out.println( currentRule ==null?null: currentRule.getRule());
            else if(args.get(1).equals("package"))
                System.out.println(currentPackageName);
            else if(args.get(1).equals("date"))
                System.out.println(currentDate);
            else throw new IllegalArgumentException();

        } else if(args.get(0).equals("save")){

            long packageId = -1;

            for(kz.bsbnb.usci.brms.rulesvr.model.impl.Batch b: batchService.getAllBatches()){
               if(b.getName().equals(currentPackageName)){
                   packageId = b.getId();
               }
            }

            if(packageId == -1)
                throw new IllegalArgumentException("no such package :" + currentPackageName);

            Long ruleId = ruleService.save(currentRule,new BatchVersion());
            batchVersionService.copyRule(ruleId,batchService.load(packageId),currentDate);

            System.out.println("ok saved: ruleId = " + ruleId);

        }else if(args.get(0).equals("run")){
            if(args.size() < 2) throw new  IllegalArgumentException();
            long id = Long.parseLong(args.get(1));

            BaseEntity baseEntity = (BaseEntity) baseEntityDao.load(id);

            rulesSingleton.runRules(baseEntity,currentPackageName,currentDate);

            for (String s: baseEntity.getValidationErrors())
                System.out.println("Validation error:" + s);
        }else if(args.get(0).equals("set")){
             if(args.size() < 3) throw new IllegalArgumentException();
               if(args.get(1).equals("package"))
               {
                   rulesSingleton.getRulePackageName(args.get(2),currentDate);
                   currentPackageName = args.get(2);
               }
               else if(args.get(1).equals("date"))
               {
                   DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                   currentDate = formatter.parse(args.get(2));
               }else throw new IllegalArgumentException();
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
        }else throw new IllegalArgumentException();
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
            line = in.nextLine();
            while (!line.equals("quit")) {
                StringTokenizer st = new StringTokenizer(line);
                try {
                    line = in.nextLine();
                } catch (NoSuchElementException e) {
                    break;
                }
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
                        commandEntity();}
                    else if(command.equals("sql")){
                        commandSql();
                    } else if(command.equals("rule")){
                        commandRule(in);
                    }else {
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

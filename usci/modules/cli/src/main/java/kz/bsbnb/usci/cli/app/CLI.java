package kz.bsbnb.usci.cli.app;

import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.bconv.xsd.Xsd2MetaClass;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

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

    private InputStream inputStream = null;

    public void processCRBatch(String fname, int count, int offset, Date repDate) throws SAXException, IOException, XMLStreamException
    {
        File inFile = new File(fname);

        InputStream in = null;
        in = new FileInputStream(inFile);

        System.out.println("Processing batch with rep date: " + repDate);

        Batch batch = batchRepository.addBatch(new Batch(repDate));

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

    public void commandMeta()
    {
        if (args.size() > 2) {
            if (args.get(0).equals("show")) {
                if (args.get(1).equals("id")) {
                    showMetaClass(Long.parseLong(args.get(2)));
                } else if (args.get(1).equals("name")) {
                    showMetaClass(args.get(2));
                } else {
                    System.out.println("No such metaClass identification method: " + args.get(1));
                }
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
                    System.out.println("Argument needed: <key> <id, name> <id or name> <attributeName> " +
                            "<subAttributeName> <filterValue>");
                }
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <show, key> <id, name> <id or name> [attributeName]");
        }
    }

    public void commandEntity()
    {
        if (args.size() > 2) {
            if (args.get(0).equals("show")) {
                if (args.get(1).equals("id")) {
                    showEntity(Long.parseLong(args.get(2)));
                } else if (args.get(1).equals("attr")) {
                    if (args.size() > 3) {
                        showEntityAttr(args.get(3), Long.parseLong(args.get(2)));
                    } else {
                        System.out.println("Argument needed: <show> <attr> <id> <attributePath>");
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
                } else {
                    System.out.println("No such entity identification method: " + args.get(1));
                }
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <show> <id, attr, sq> <id> [attributePath]");
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

        String line;

        Exception lastException = null;

        while(true) {
            while (!(line = in.nextLine()).equals("quit")) {
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
                    } else {
                        System.out.println("No such command: " + command);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    lastException = e;
                }

                System.out.print("> ");
            }
            if (inputStream == null) break;
            else {
                in = new Scanner(System.in);
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

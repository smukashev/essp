package kz.bsbnb.usci.cli.app;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.bconv.xsd.Xsd2MetaClass;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchVersionService;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.cli.app.command.impl.MetaAddCommand;
import kz.bsbnb.usci.cli.app.command.impl.MetaCreateCommand;
import kz.bsbnb.usci.cli.app.command.impl.MetaKeyCommand;
import kz.bsbnb.usci.cli.app.command.impl.MetaShowCommand;
import kz.bsbnb.usci.cli.app.mnt.Mnt;
import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityMergeManager;
import kz.bsbnb.usci.eav.manager.impl.MergeManagerKey;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.eav.model.json.EntityStatusArrayJModel;
import kz.bsbnb.usci.eav.model.json.EntityStatusJModel;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.output.BaseEntityOutput;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.impl.BaseEntityXmlGenerator;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.service.ShowcaseService;
import kz.bsbnb.usci.tool.status.CoreStatus;
import kz.bsbnb.usci.tool.status.ReceiverStatus;
import kz.bsbnb.usci.tool.status.SyncStatus;
import kz.bsbnb.usci.tool.status.SystemStatus;
import net.spy.memcached.OperationTimeoutException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class CLI {
    private static SimpleDateFormat sdfout = new SimpleDateFormat("dd.MM.yyyy");
    @Autowired
    protected IMetaClassRepository metaClassRepository;
    @Autowired
    protected IBatchRepository batchRepository;
    RmiProxyFactoryBean serviceFactory = null;
    IEntityService entityServiceCore = null;
    ShowCase showCase;
    String line;
    Exception lastException = null;
    private String command;
    private ArrayList<String> args = new ArrayList<String>();
    @Autowired
    private IStorage storage;

    @Autowired
    private IMetaClassDao metaClassDao;
    @Autowired
    private Xsd2MetaClass xsdConverter;
    @Autowired
    private MainParser crParser;
    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;
    @Autowired
    private ImprovedBaseEntitySearcher searcher;

    @Autowired
    ApplicationContext context;

    private BasicBaseEntityComparator comparator = new BasicBaseEntityComparator();
    private InputStream inputStream = null;
    private CouchbaseClient couchbaseClient;
    private JobDispatcher jobDispatcher = new JobDispatcher();
    private RmiProxyFactoryBean batchServiceFactoryBean;
    private RmiProxyFactoryBean batchVersionServiceFactoryBean;
    private RmiProxyFactoryBean ruleServiceFactoryBean;
    private RmiProxyFactoryBean showcaseServiceFactoryBean;
    private RmiProxyFactoryBean entityServiceFactoryBean;
    private IBatchService batchService;
    private IRuleService ruleService;
    private IBatchVersionService batchVersionService;
    private ShowcaseService showcaseService;
    private Rule currentRule;
    private String currentPackageName = "credit_parser";
    private Date currentDate = new Date();
    private boolean started = false;
    private BatchVersion currentBatchVersion;
    private String defaultDumpFile = "c:/rule_dumps/23_06_2015.cli";
    private BaseEntity currentBaseEntity;
    private Mnt mnt;

    public IEntityService getEntityService(String url) {
        if (entityServiceCore == null) {
            try {
                serviceFactory = new RmiProxyFactoryBean();
                serviceFactory.setServiceUrl(url);
                serviceFactory.setServiceInterface(IEntityService.class);
                serviceFactory.setRefreshStubOnConnectFailure(true);

                serviceFactory.afterPropertiesSet();
                entityServiceCore = (IEntityService) serviceFactory.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
            }
        }

        return entityServiceCore;
    }

    @PostConstruct
    public void initBean() {
        System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://localhost:8091/pools"));

        try {
            couchbaseClient = new CouchbaseClient(nodes, "test2", "");
        } catch (Exception e) {
            System.out.println("Error connecting to Couchbase: " + e.getMessage());
        }

        jobDispatcher.start();
    }

    private void shutdown() {
        couchbaseClient.shutdown();
    }

    public void processCRBatch(String fname, int count, int offset, Date repDate)
            throws SAXException, IOException, XMLStreamException {
        File inFile = new File(fname);

        InputStream in;
        in = new FileInputStream(inFile);

        System.out.println("Processing batch with rep date: " + repDate);

        Batch b = new Batch(repDate);
        b.setUserId(0L);

        Batch batch = batchRepository.addBatch(b);

        crParser.parse(in, batch);

        BaseEntity entity;
        int i = 0;
        while (crParser.hasMore() && (((i++) - offset) < count)) {
            if (i > offset) {
                entity = crParser.getCurrentBaseEntity();
                System.out.println(entity);
                long id = baseEntityProcessorDao.process(entity).getId();
                System.out.println("Saved with id: " + id);
            }

            if (i >= offset) {
                crParser.parseNextPackage();
            } else {
                crParser.skipNextPackage();
            }
        }

    }

    public void processXSD(String fname, String metaClassName) throws FileNotFoundException {
        File inFile = new File(fname);

        InputStream in = null;
        in = new FileInputStream(inFile);

        System.out.println("Parsing...");
        MetaClass meta = xsdConverter.convertXSD(in, metaClassName);

        System.out.println("Saving...");
        long id = metaClassDao.save(meta);

        System.out.println("Saved with id: " + id);
    }

    public void listXSD(String fname) throws FileNotFoundException {
        File inFile = new File(fname);

        InputStream in = null;
        in = new FileInputStream(inFile);

        System.out.println("Classes: ");
        ArrayList<String> names = xsdConverter.listClasses(in);

        for (String name : names) {
            System.out.println(name);
        }
    }

    public void setMetaClassKeyType(String name, ComplexKeyTypes type) {
        MetaClass meta = metaClassRepository.getMetaClass(name);

        if (meta == null) {
            System.out.println("No such meta class with name: " + name);
        } else {
            meta.setComplexKeyType(type);
            metaClassRepository.saveMetaClass(meta);
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
                    MetaSet set = (MetaSet) attr;

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
                    MetaSet set = (MetaSet) attr;

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

    public void entityToJava(long id, String reportDateStr) {
        IBaseEntity entity;

        if (reportDateStr != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            try {
                entity = baseEntityProcessorDao.loadByMaxReportDate(id, dateFormat.parse(reportDateStr));
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
        } else {
            entity = baseEntityProcessorDao.load(id);
        }

        System.out.println(BaseEntityOutput.toJava((BaseEntity) entity, "", 0));
    }

    public void showEntity(long id, String reportDateStr) {
        IBaseEntity entity;

        if (reportDateStr != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            try {
                entity = baseEntityProcessorDao.loadByMaxReportDate(id, dateFormat.parse(reportDateStr));
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
        } else {
            entity = baseEntityProcessorDao.load(id);
        }

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            System.out.println(entity.toString());
        }
    }

    public void batchStat() {
        if (args.size() < 5) {
            System.out.println("Usage: <report_date> <output_file>");
            System.out.println("Example: batchstat 01.05.2013 " +
                    "D:\\usci\\out.txt jdbc:oracle:thin:@172.17.110.92:1521:XE core CORE_2013");
            return;
        }

        String reportDateStr = args.get(0);
        String fileNameStr = args.get(1);

        Gson gson = new Gson();

        if (reportDateStr != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date reportDate = null;
            try {
                reportDate = dateFormat.parse(reportDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }

            File f = new File(fileNameStr);
            FileOutputStream fout = null;
            try {
                f.createNewFile();

                fout = new FileOutputStream(f);

                while (true) {
                    try {
                        Connection conn;

                        try {
                            conn = connectToDB(args.get(2), args.get(3), args.get(4));
                        } catch (ClassNotFoundException e) {
                            System.out.println("Error can't load driver: oracle.jdbc.OracleDriver");
                            return;
                        } catch (SQLException e) {
                            System.out.println("Can't connect to DB: " + e.getMessage());
                            return;
                        }

                        PreparedStatement preparedStatement = null;
                        try {
                            preparedStatement = conn.prepareStatement("SELECT b.id FROM eav_batches b  " +
                                    "WHERE b.rep_date = to_date('" + reportDateStr.trim() + "', 'dd.MM.yyyy')");
                        } catch (SQLException e) {
                            System.out.println("Can't create prepared statement: " + e.getMessage());
                            try {
                                conn.close();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                            return;
                        }

                        ResultSet result = preparedStatement.executeQuery();

                        while (result.next()) {
                            long batchId = result.getLong("id");

                            System.out.println("Processing id: " + batchId);

                            Object batchObject = null;
                            Object manifestObject = null;
                            Object batchStatusObject = null;

                            int counter = 0;

                            while (counter < 100) {
                                try {
                                    batchObject = couchbaseClient.get("batch:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting...");
                                    counter++;
                                }
                            }

                            counter = 0;

                            while (counter < 100) {
                                try {
                                    manifestObject = couchbaseClient.get("manifest:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting...");
                                    counter++;
                                }
                            }

                            counter = 0;

                            while (counter < 100) {
                                try {
                                    batchStatusObject = couchbaseClient.get("batch_status:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting...");
                                    counter++;
                                }
                            }

                            if (batchObject == null || manifestObject == null) {
                                System.out.println("Batch with id: " + batchId + " has no manifest or batch!");

                                if (batchObject != null) {
                                    couchbaseClient.delete("batch:" + batchId);
                                }
                                if (manifestObject != null) {
                                    couchbaseClient.delete("manifest:" + batchId);
                                }
                                if (batchStatusObject != null) {
                                    couchbaseClient.delete("batch_status:" + batchId);
                                }
                                continue;
                            }

                            String batchStr = batchObject.toString();

                            BatchFullJModel batchFull = gson.fromJson(batchStr, BatchFullJModel.class);

                            String batchInfoStr = manifestObject.toString();

                            BatchInfo batchInfo = gson.fromJson(batchInfoStr, BatchInfo.class);

                            if (DataUtils.compareBeginningOfTheDay(batchInfo.getRepDate(), reportDate) != 0) {
                                continue;
                            }

                            View view = couchbaseClient.getView("batch", "entity_status");
                            Query query = new Query();
                            query.setDescending(true);
                            query.setRangeEnd("[" + batchId + ", 0]");
                            query.setRangeStart("[" + batchId + ", 999999999999999]");

                            ViewResponse response = couchbaseClient.query(view, query);

                            Iterator<ViewRow> rows = response.iterator();

                            int row_count = 0;
                            int error_count = 0;
                            while (rows.hasNext()) {
                                ViewRow viewRowNoDocs = rows.next();

                                row_count++;

                                EntityStatusArrayJModel batchFullStatusJModel =
                                        gson.fromJson(viewRowNoDocs.getValue(), EntityStatusArrayJModel.class);

                                boolean errorFound = false;
                                boolean completedFound = false;
                                for (EntityStatusJModel csajm : batchFullStatusJModel.getEntityStatuses()) {
                                    if (csajm.getProtocol().equals("ERROR")) {
                                        errorFound = true;
                                    }
                                    if (csajm.getProtocol().equals("COMPLETED")) {
                                        completedFound = true;
                                    }
                                }
                                if (errorFound && !completedFound)
                                    error_count++;
                            }

//                        if (error_count > 0 || row_count != batchInfo.getSize()) {
//                            System.out.println(batchId + " - " + batchInfo.getSize() + "/" + row_count + " - " + error_count);
//                            sender.addJob(batchId, batchInfo);
//                            receiverStatusSingleton.batchReceived();
//                        }

                            fout.write((batchId + "," +
                                    batchFull.getFileName() + "," +
                                    batchInfo.getSize() + "," + row_count + "," + error_count + "\n").getBytes());
                        }
                        break;
                    } catch (Exception e) {
                        System.out.println("Error in pending batches view: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                }
                System.out.println("Done");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Report date needed.");
            return;
        }
    }

    public void batchRestart() {
        if (args.size() < 6) {
            System.out.println("Usage: <report_date> <output_file>");
            System.out.println("Example: batchrestart 01.05.2013 D:\\usci\\out.txt " +
                    "jdbc:oracle:thin:@172.17.110.92:1521:XE core CORE_2013 rmi://127.0.0.1:1097/batchProcessService");
            return;
        }

        RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

        IBatchProcessService batchProcessService = null;

        try {
            batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl(args.get(5));
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
            batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
        } catch (Exception e) {
            System.out.println("Can't connect to receiver service: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String reportDateStr = args.get(0);
        String fileNameStr = args.get(1);

        Gson gson = new Gson();

        if (reportDateStr != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date reportDate = null;
            try {
                reportDate = dateFormat.parse(reportDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }

            File f = new File(fileNameStr);
            FileOutputStream fout = null;
            try {
                f.createNewFile();

                fout = new FileOutputStream(f);

                while (true) {
                    try {
                        Connection conn = null;

                        try {
                            conn = connectToDB(args.get(2), args.get(3), args.get(4));
                        } catch (ClassNotFoundException e) {
                            System.out.println("Error can't load driver: oracle.jdbc.OracleDriver");
                            return;
                        } catch (SQLException e) {
                            System.out.println("Can't connect to DB: " + e.getMessage());
                            return;
                        }

                        PreparedStatement preparedStatement = null;
                        try {
                            preparedStatement = conn.prepareStatement("SELECT b.id FROM eav_batches b  " +
                                    "WHERE b.rep_date = to_date('" + reportDateStr.trim() + "', 'dd.MM.yyyy')");
                        } catch (SQLException e) {
                            System.out.println("Can't create prepared statement: " + e.getMessage());
                            try {
                                conn.close();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                            return;
                        }

                        ResultSet result = preparedStatement.executeQuery();

                        while (result.next()) {
                            long batchId = result.getLong("id");

                            System.out.println("Processing id: " + batchId);

                            Object batchObject = null;
                            Object manifestObject = null;
                            Object batchStatusObject = null;

                            int counter = 0;

                            while (counter < 100) {
                                try {
                                    batchObject = couchbaseClient.get("batch:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting.");
                                    counter++;
                                }
                            }

                            counter = 0;

                            while (counter < 100) {
                                try {
                                    manifestObject = couchbaseClient.get("manifest:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting.");
                                    counter++;
                                }
                            }

                            counter = 0;

                            while (counter < 100) {
                                try {
                                    batchStatusObject = couchbaseClient.get("batch_status:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting.");
                                    counter++;
                                }
                            }

                            if (batchObject == null || manifestObject == null) {
                                System.out.println("Batch with id: " + batchId + " has no manifest or batch!");

                                if (batchObject != null) {
                                    couchbaseClient.delete("batch:" + batchId);
                                }
                                if (manifestObject != null) {
                                    couchbaseClient.delete("manifest:" + batchId);
                                }
                                if (batchStatusObject != null) {
                                    couchbaseClient.delete("batch_status:" + batchId);
                                }
                                continue;
                            }

                            String batchStr = batchObject.toString();

                            BatchFullJModel batchFull = gson.fromJson(batchStr, BatchFullJModel.class);

                            String batchInfoStr = manifestObject.toString();

                            BatchInfo batchInfo = gson.fromJson(batchInfoStr, BatchInfo.class);

                            if (DataUtils.compareBeginningOfTheDay(batchInfo.getRepDate(), reportDate) != 0) {
                                continue;
                            }

                            View view = couchbaseClient.getView("batch", "entity_status");
                            Query query = new Query();
                            query.setDescending(true);
                            query.setRangeEnd("[" + batchId + ", 0]");
                            query.setRangeStart("[" + batchId + ", 999999999999999]");

                            ViewResponse response = couchbaseClient.query(view, query);

                            Iterator<ViewRow> rows = response.iterator();

                            int row_count = 0;
                            int error_count = 0;
                            while (rows.hasNext()) {
                                ViewRow viewRowNoDocs = rows.next();

                                row_count++;

                                EntityStatusArrayJModel batchFullStatusJModel =
                                        gson.fromJson(viewRowNoDocs.getValue(), EntityStatusArrayJModel.class);

                                boolean errorFound = false;
                                boolean completedFound = false;
                                for (EntityStatusJModel csajm : batchFullStatusJModel.getEntityStatuses()) {
                                    if (csajm.getProtocol().equals("ERROR")) {
                                        errorFound = true;
                                    }
                                    if (csajm.getProtocol().equals("COMPLETED")) {
                                        completedFound = true;
                                    }
                                }
                                if (errorFound && !completedFound)
                                    error_count++;
                            }

                            if (error_count > 0 || row_count != batchInfo.getSize()) {
                                fout.write((batchId + "," +
                                        batchFull.getFileName() + "," +
                                        batchInfo.getSize() + "," + row_count + "," + error_count +
                                        ",restarted\n").getBytes());

                                //sender.addJob(batchId, batchInfo);
                                //receiverStatusSingleton.batchReceived();
                                batchProcessService.restartBatch(batchId);
                            } else {
                                fout.write((batchId + "," +
                                        batchFull.getFileName() + "," +
                                        batchInfo.getSize() + "," + row_count + "," + error_count +
                                        ",skipped\n").getBytes());
                            }
                        }
                        break;
                    } catch (Exception e) {
                        System.out.println("Error in pending batches view: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                }
                System.out.println("Done");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Report date needed.");
            return;
        }
    }

    public void batchRestartAll() {
        if (args.size() < 6) {
            System.out.println("Usage: <report_date> <output_file>");
            System.out.println("Example: batchrestartall 01.05.2013 D:\\usci\\out.txt " +
                    "jdbc:oracle:thin:@172.17.110.92:1521:XE " +
                    "core CORE_2013 rmi://127.0.0.1:1097/batchProcessService");
            return;
        }

        RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

        IBatchProcessService batchProcessService = null;

        try {
            batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl(args.get(5));
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
            batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
        } catch (Exception e) {
            System.out.println("Can't connect to receiver service: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String reportDateStr = args.get(0);
        String fileNameStr = args.get(1);

        Gson gson = new Gson();

        if (reportDateStr != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date reportDate = null;
            try {
                reportDate = dateFormat.parse(reportDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }

            File f = new File(fileNameStr);
            FileOutputStream fout = null;
            try {
                f.createNewFile();

                fout = new FileOutputStream(f);

                while (true) {
                    try {
                        Connection conn = null;

                        try {
                            conn = connectToDB(args.get(2), args.get(3), args.get(4));
                        } catch (ClassNotFoundException e) {
                            System.out.println("Error can't load driver: oracle.jdbc.OracleDriver");
                            return;
                        } catch (SQLException e) {
                            System.out.println("Can't connect to DB: " + e.getMessage());
                            return;
                        }

                        PreparedStatement preparedStatement = null;
                        try {
                            preparedStatement = conn.prepareStatement("SELECT b.id FROM eav_batches b  " +
                                    "WHERE b.rep_date = to_date('" + reportDateStr.trim() + "', 'dd.MM.yyyy')");
                        } catch (SQLException e) {
                            System.out.println("Can't create prepared statement: " + e.getMessage());
                            try {
                                conn.close();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                            return;
                        }

                        ResultSet result = preparedStatement.executeQuery();

                        while (result.next()) {
                            long batchId = result.getLong("id");

                            System.out.println("Processing id: " + batchId);

                            Object batchObject = null;
                            Object manifestObject = null;
                            Object batchStatusObject = null;

                            int counter = 0;

                            while (counter < 100) {
                                try {
                                    batchObject = couchbaseClient.get("batch:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting.");
                                    counter++;
                                }
                            }

                            counter = 0;

                            while (counter < 100) {
                                try {
                                    manifestObject = couchbaseClient.get("manifest:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting.");
                                    counter++;
                                }
                            }

                            counter = 0;

                            while (counter < 100) {
                                try {
                                    batchStatusObject = couchbaseClient.get("batch_status:" + batchId);
                                    break;
                                } catch (OperationTimeoutException ex) {
                                    System.out.println("Timeout. Restarting.");
                                    counter++;
                                }
                            }

                            if (batchObject == null || manifestObject == null) {
                                System.out.println("Batch with id: " + batchId + " has no manifest or batch!");

                                if (batchObject != null) {
                                    couchbaseClient.delete("batch:" + batchId);
                                }
                                if (manifestObject != null) {
                                    couchbaseClient.delete("manifest:" + batchId);
                                }
                                if (batchStatusObject != null) {
                                    couchbaseClient.delete("batch_status:" + batchId);
                                }
                                continue;
                            }

                            String batchStr = batchObject.toString();

                            BatchFullJModel batchFull = gson.fromJson(batchStr, BatchFullJModel.class);

                            String batchInfoStr = manifestObject.toString();

                            BatchInfo batchInfo = gson.fromJson(batchInfoStr, BatchInfo.class);

                            if (DataUtils.compareBeginningOfTheDay(batchInfo.getRepDate(), reportDate) != 0) {
                                continue;
                            }

                            fout.write((batchId + "," +
                                    batchFull.getFileName() + "," +
                                    batchInfo.getSize() + ",restarted\n").getBytes());

                            batchProcessService.restartBatch(batchId);
                        }
                        break;
                    } catch (Exception e) {
                        System.out.println("Error in pending batches view: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                }
                System.out.println("Done");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Report date needed.");
            return;
        }
    }

    public void batchRestartSingle() {
        if (args.size() < 2) {
            System.out.println("Usage: <report_date> <output_file>");
            System.out.println("Example: sbatchrestart <batch_id> rmi://127.0.0.1:1097/batchProcessService");
            return;
        }

        RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

        IBatchProcessService batchProcessService = null;

        try {
            batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl(args.get(1));
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
            batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
        } catch (Exception e) {
            System.out.println("Can't connect to receiver service: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Gson gson = new Gson();

        long batchId = Long.parseLong(args.get(0));

        System.out.println("Processing id: " + batchId);

        Object batchObject = null;
        Object manifestObject = null;
        Object batchStatusObject = null;

        int counter = 0;

        while (counter < 100) {
            try {
                batchObject = couchbaseClient.get("batch:" + batchId);
                break;
            } catch (OperationTimeoutException ex) {
                System.out.println("Timeout. Restarting.");
                counter++;
            }
        }

        counter = 0;

        while (counter < 100) {
            try {
                manifestObject = couchbaseClient.get("manifest:" + batchId);
                break;
            } catch (OperationTimeoutException ex) {
                System.out.println("Timeout. Restarting.");
                counter++;
            }
        }

        counter = 0;

        while (counter < 100) {
            try {
                batchStatusObject = couchbaseClient.get("batch_status:" + batchId);
                break;
            } catch (OperationTimeoutException ex) {
                System.out.println("Timeout. Restarting.");
                counter++;
            }
        }

        if (batchObject == null || manifestObject == null) {
            System.out.println("Batch with id: " + batchId + " has no manifest or batch!");

            if (batchObject != null) {
                couchbaseClient.delete("batch:" + batchId);
            }
            if (manifestObject != null) {
                couchbaseClient.delete("manifest:" + batchId);
            }
            if (batchStatusObject != null) {
                couchbaseClient.delete("batch_status:" + batchId);
            }
            return;
        }

        String batchStr = batchObject.toString();

        BatchFullJModel batchFull = gson.fromJson(batchStr, BatchFullJModel.class);

        String batchInfoStr = manifestObject.toString();

        BatchInfo batchInfo = gson.fromJson(batchInfoStr, BatchInfo.class);

        View view = couchbaseClient.getView("batch", "entity_status");
        Query query = new Query();
        query.setDescending(true);
        query.setRangeEnd("[" + batchId + ", 0]");
        query.setRangeStart("[" + batchId + ", 999999999999999]");

        ViewResponse response = couchbaseClient.query(view, query);

        Iterator<ViewRow> rows = response.iterator();

        int row_count = 0;
        int error_count = 0;
        while (rows.hasNext()) {
            ViewRow viewRowNoDocs = rows.next();

            row_count++;

            EntityStatusArrayJModel batchFullStatusJModel =
                    gson.fromJson(viewRowNoDocs.getValue(), EntityStatusArrayJModel.class);

            boolean errorFound = false;
            boolean completedFound = false;
            for (EntityStatusJModel csajm : batchFullStatusJModel.getEntityStatuses()) {
                if (csajm.getProtocol().equals("ERROR")) {
                    errorFound = true;
                }
                if (csajm.getProtocol().equals("COMPLETED")) {
                    completedFound = true;
                }
            }
            if (errorFound)// && !completedFound)
                error_count++;
        }

        //if (error_count > 0 || row_count != batchInfo.getSize()) {
        System.out.println(batchId + "," +
                batchFull.getFileName() + "," +
                batchInfo.getSize() + "," + row_count + "," + error_count + ",restarted");

        //sender.addJob(batchId, batchInfo);
        //receiverStatusSingleton.batchReceived();
        batchProcessService.restartBatch(batchId);
        /*} else {
            System.out.println(batchId + "," +
                    batchFull.getFileName() + "," +
                    batchInfo.getSize() + "," + row_count + "," + error_count +
                    ",skipped because it has no errors");
        } */
    }

    public void removeEntityById(long id, String url) {
        jobDispatcher.addThread(new DeleteJob(getEntityService(url), id));
    }

    public void removeAllEntityByMetaId(long metaClassId) {
        RmiProxyFactoryBean serviceFactory = null;

        IEntityService entityServiceCore = null;

        try {
            serviceFactory = new RmiProxyFactoryBean();
            serviceFactory.setServiceUrl("rmi://127.0.0.1:1099/entityService");
            serviceFactory.setServiceInterface(IEntityService.class);
            serviceFactory.setRefreshStubOnConnectFailure(true);

            serviceFactory.afterPropertiesSet();
            entityServiceCore = (IEntityService) serviceFactory.getObject();
        } catch (Exception e) {
            System.out.println("Can't connect to receiver service: " + e.getMessage());
        }

        IMetaClass metaClass = metaClassDao.load(metaClassId);

        if(entityServiceCore == null)
            throw new NullPointerException("EntityService is null!");

        entityServiceCore.removeAllByMetaClass(metaClass);
    }

    public void dumpEntityToXML(String ids, String fileName) {
        StringTokenizer st = new StringTokenizer(ids, ",");
        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        while (st.hasMoreTokens()) {
            long id = Long.parseLong(st.nextToken());
            IBaseEntity entity = baseEntityProcessorDao.load(id);
            if (entity != null) {
                entities.add((BaseEntity) entity);
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

    public void dumpDeleteEntityToXML(String ids, String fileName) {
        StringTokenizer st = new StringTokenizer(ids, ",");
        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        while (st.hasMoreTokens()) {
            long id = Long.parseLong(st.nextToken());
            IBaseEntity entity = baseEntityProcessorDao.load(id);
            if (entity != null) {
                entities.add((BaseEntity) entity);
            }
        }

        if (entities.size() == 0) {
            System.out.println("No entities found with ids: " + ids);
        } else {
            BaseEntityXmlGenerator baseEntityXmlGenerator = new BaseEntityXmlGenerator();

            Document document = baseEntityXmlGenerator.getGeneratedDeleteDocument(entities);

            baseEntityXmlGenerator.writeToXml(document, fileName);
        }
    }

    public void readEntityFromXMLString(String xml, String repDate) {
        try {
            Date reportDate = sdfout.parse(repDate);
            CLIXMLReader reader = new CLIXMLReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
                    , metaClassRepository, batchRepository, reportDate);

            BaseEntity entity;
            while ((entity = reader.read()) != null) {
                try {
                    long id = baseEntityProcessorDao.process(entity).getId();
                    System.out.println("Instance of BaseEntity saved with id: " + id);
                } catch (Exception ex) {
                    lastException = ex;
                    System.out.println("While processing instance of BaseEntity unexpected error occurred: " +
                            ex.getMessage());
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void readEntityFromXML(String fileName, String repDate) {
        try {
            Date reportDate = sdfout.parse(repDate);
            CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchRepository, reportDate);
            BaseEntity entity;
            while ((entity = reader.read()) != null) {
                try {
                    long id = baseEntityProcessorDao.process(entity).getId();
                    System.out.println("Instance of BaseEntity saved with id: " + id);
                } catch (Exception ex) {
                    lastException = ex;
                    System.out.println("While processing instance of BaseEntity unexpected error occurred: " +
                            ex.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Can't parse date " + repDate + " must be in format " + sdfout.toString());
        }

    }

    public void findEntityFromXML(String fileName, String repDate) {
        try {
            Date reportDate = sdfout.parse(repDate);
            CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchRepository, reportDate);
            BaseEntity entity;
            while ((entity = reader.read()) != null) {
                //long id = baseEntityProcessorDao.process(entity).getId();
                ArrayList<Long> ids = searcher.findAll(entity);
                if (ids.size() < 1) {
                    System.out.println("Entity: \n" + entity.toString() + "\nNot found.");
                } else {
                    System.out.println("Entity: \n" + entity.toString() + "\nFound with ids: ");
                    for (Long id : ids) {
                        System.out.println(id + " ");
                    }
                }

            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Can't parse date " + repDate + " must be in format " + sdfout.toString());
        }

    }

    public void testEntityFromXML(String fileName, String repDate) {
        try {
            Date reportDate = sdfout.parse(repDate);
            CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchRepository, reportDate);
            BaseEntity entity;
            while ((entity = reader.read()) != null) {
                BaseEntity clonedEntity = entity.clone();

                List<String> intersectionList = comparator.intersect(entity, clonedEntity);

                System.out.println("Intersection count: " + intersectionList.size() + ", actual count: " +
                        entity.getSearchableChildrenCount());
                if (intersectionList.size() != entity.getSearchableChildrenCount()) {
                    System.out.println("Error");
                    for (String path : intersectionList) {
                        System.out.println(path);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Can't parse date " + repDate + " must be in format " + sdfout.toString());
        }

    }

    public void showEntityAttr(String path, long id) {
        IBaseEntity entity = baseEntityProcessorDao.load(id);

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
        IBaseEntity entity1 = baseEntityProcessorDao.load(id1);
        IBaseEntity entity2 = baseEntityProcessorDao.load(id2);

        if (entity1 == null) {
            System.out.println("No such entity with id: " + id1);
        } else if (entity2 == null) {
            System.out.println("No such entity with id: " + id2);
        } else {
            List<String> inter = comparator.intersect((BaseEntity) entity1, (BaseEntity) entity2);

            for (String str : inter) {
                System.out.println(str);
            }
        }
    }

    public void showEntitySQ(long id) {
        IBaseEntity entity = baseEntityProcessorDao.load(id);

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
        IBaseEntity entity = baseEntityProcessorDao.load(id);

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            //SelectConditionStep where = searcher.generateSQL(entity, null);
            ArrayList<Long> array = searcher.findAll((BaseEntity) entity);

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

        List<BaseEntity> entities = baseEntityProcessorDao.getEntityByMetaclass(meta);

        if (entities.size() == 0) {
            System.out.println("No such entities with class: " + name);
        } else {
            for (BaseEntity ids : entities) {
                System.out.println(ids.toString());
            }
        }
    }

    public void commandXSD() throws FileNotFoundException {
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

    public void commandCRBatch() throws IOException, SAXException, XMLStreamException, ParseException {
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

    public void removeAttributeFromMeta(String metaName, String attrName) {
        MetaClass meta = metaClassRepository.getMetaClass(metaName);

        meta.removeMemberType(attrName);

        metaClassRepository.saveMetaClass(meta);
    }

    public void setArgs(ArrayList<String> args) {
        this.args = args;
    }

    public void resetMetaCache() {
        metaClassRepository.resetCache();
    }

    public void commandMeta() {
        if (args.size() > 1) {
            if (args.get(0).equals("show")) {
                MetaShowCommand metaShowCommand = new MetaShowCommand();
                metaShowCommand.setMetaClassRepository(metaClassRepository);
                metaShowCommand.run(args.toArray(new String[args.size()]));
            } else if (args.get(0).equals("add")) {
                MetaAddCommand metaAddCommand = new MetaAddCommand();
                metaAddCommand.setMetaClassRepository(metaClassRepository);
                metaAddCommand.run(args.toArray(new String[args.size()]));
            } else if (args.get(0).equals("remove")) {
                if (args.size() > 2) {
                    removeAttributeFromMeta(args.get(1), args.get(2));
                }
            } else if (args.get(0).equals("create")) {
                MetaCreateCommand metaCreateCommand = new MetaCreateCommand();
                metaCreateCommand.setMetaClassRepository(metaClassRepository);
                metaCreateCommand.run(args.toArray(new String[args.size()]));
            } else if (args.get(0).equals("delete")) {
                System.out.println("Unimplemented stub in cli");
            } else if (args.get(0).equals("key")) {
                MetaKeyCommand metaKeyCommand = new MetaKeyCommand();
                metaKeyCommand.setMetaClassRepository(metaClassRepository);
                metaKeyCommand.run(args.toArray(new String[args.size()]));
            } else if (args.get(0).equals("keytype")) {
                if (args.size() > 2) {
                    setMetaClassKeyType(args.get(1), ComplexKeyTypes.valueOf(args.get(2)));
                } else {
                    System.out.println("Argument needed: <keytype> <name> <key_type>");
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

    public Connection connectToDB(String url, String name, String password)
            throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.OracleDriver");
        return DriverManager.getConnection(url, name, password);
    }

    public void commandImport() {
        if (args.size() > 4) {
            Connection conn = null;

            RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

            IBatchProcessService batchProcessService = null;

            try {
                batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
                batchProcessServiceFactoryBean.setServiceUrl(args.get(3));
                batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
                batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

                batchProcessServiceFactoryBean.afterPropertiesSet();
                batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
            }

            try {
                conn = connectToDB(args.get(0), args.get(1), args.get(2));
            } catch (ClassNotFoundException e) {
                System.out.println("Error can't load driver: oracle.jdbc.OracleDriver");
                return;
            } catch (SQLException e) {
                System.out.println("Can't connect to DB: " + e.getMessage());
                return;
            }

            PreparedStatement preparedStatement = null;
            PreparedStatement preparedStatementDone = null;
            try {
                preparedStatement = conn.prepareStatement("SELECT xf.id, xf.file_name, xf.file_content\n" +
                        "  FROM core.xml_file xf\n" +
                        " WHERE xf.status = 'COMPLETED'\n" +
                        "   AND xf.report_date = to_date('" + args.get(5) + "', 'dd.MM.yyyy')" +
                        //"   ORDER BY xf.id ASC");
                        "   AND xf.sent = 0 ORDER BY xf.id ASC");

                preparedStatementDone = conn.prepareStatement("UPDATE core.xml_file xf \n" +
                        "   SET xf.sent = ? \n" +
                        " WHERE xf.id = ?");
            } catch (SQLException e) {
                System.out.println("Can't create prepared statement: " + e.getMessage());
                try {
                    conn.close();
                } catch (SQLException e1) {
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

            int fileNumber = 0;

            while (true) {
                fileNumber++;

                ResultSet result2 = null;
                try {
                    result2 = preparedStatement.executeQuery();
                } catch (SQLException e) {
                    System.out.println("Can't execute db query: " + e.getMessage());
                    break;
                }

                int id = 0;

                try {
                    if (result2.next()) {
                        id = result2.getInt("id");
                        String fileName = result2.getString("file_name");
                        Blob blob = result2.getBlob("file_content");

                        File newFile = new File(tempDir.getAbsolutePath() + "/" + fileName + ".zip");
                        newFile.createNewFile();

                        InputStream in = blob.getBinaryStream();

                        byte[] buffer = new byte[1024];

                        FileOutputStream fout = new FileOutputStream(newFile);

                        while (in.read(buffer) > 0) {
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
                } catch (SQLException e) {
                    System.out.println("Can't get result from db: " + e.getMessage());
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Can't create temp file: " + e.getMessage());
                } catch (Exception e) {
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
            System.out.println("Argument needed: <credits_db_url> <user> <password> <receiver_url> " +
                    "<temp_files_folder>");

            System.out.println("Example: import jdbc:oracle:thin:@srv-scan.corp.nb.rk:1521/DBM01 " +
                    "core ***** rmi://127.0.0.1:1097/batchProcessService D:\\usci\\temp_xml_folder 01.05.2013");

            System.out.println("Example: import jdbc:oracle:thin:@192.168.0.44:1521/CREDITS " +
                    "core core_feb_2013 rmi://127.0.0.1:1097/batchProcessService " +
                    "/home/a.tkachenko/temp_files 01.05.2013");

            System.out.println("Example: import jdbc:oracle:thin:@192.168.0.44:1521/CREDITS " +
                    "core core_mar_2014 rmi://127.0.0.1:1097/batchProcessService D:\\USCI\\Temp 01.05.2013");
        }
    }

    public void commandCollectIds() {
        if (args.size() > 1) {
            String inFileName = args.get(0);
            String outFileName = args.get(1);

            ArrayList<Long> ids = new ArrayList<Long>();

            try {
                Scanner inputScanner = new Scanner(new FileInputStream(inFileName));
                FileOutputStream fout = new FileOutputStream(outFileName);

                while (inputScanner.hasNextLine()) {
                    String nextLine = inputScanner.nextLine();

                    int idIndex = nextLine.indexOf("Not yet implemented. Entity ID:");

                    if (idIndex > 0) {
                        String idString = nextLine.substring(idIndex +
                                "Not yet implemented. Entity ID:".length()).trim();

                        Long id = Long.parseLong(idString);
                        ids.add(id);
                    }

                    if (nextLine.indexOf("ERROR") > 0)
                        fout.write((nextLine + "\n").getBytes());
                }

                boolean first = true;
                String idsStr = "";
                for (Long id : ids) {
                    if (first) {
                        idsStr += id;
                        first = false;
                    } else {
                        idsStr += "," + id;
                    }
                }

                fout.write(idsStr.getBytes());

                fout.close();
                inputScanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Argument needed: <input_file_name> <output_file_name>");
            System.out.println("Example: collectids D:\\distr\\usci\\core.log D:\\distr\\usci\\out_core.log");
        }
    }

    public void commandRStat() {
        if (args.size() > 0) {
            RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

            IBatchProcessService batchProcessService = null;

            try {
                batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
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

    public void commandRemoteStat() {
        if (args.size() > 3) {
            RmiProxyFactoryBean batchProcessServiceFactoryBean = null;

            IBatchProcessService batchProcessService = null;

            try {
                batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
                batchProcessServiceFactoryBean.setServiceUrl(args.get(0));
                batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
                batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

                batchProcessServiceFactoryBean.afterPropertiesSet();
                batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
                e.printStackTrace();
            }

            RmiProxyFactoryBean entityServiceFactoryBean = null;

            kz.bsbnb.usci.sync.service.IEntityService entityServiceSync = null;

            try {
                entityServiceFactoryBean = new RmiProxyFactoryBean();
                entityServiceFactoryBean.setServiceUrl(args.get(1));
                entityServiceFactoryBean.setServiceInterface(kz.bsbnb.usci.sync.service.IEntityService.class);
                entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

                entityServiceFactoryBean.afterPropertiesSet();
                entityServiceSync = (kz.bsbnb.usci.sync.service.IEntityService) entityServiceFactoryBean.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to sync service: " + e.getMessage());
                e.printStackTrace();
            }

            RmiProxyFactoryBean serviceFactory = null;

            IEntityService entityServiceCore = null;

            try {
                serviceFactory = new RmiProxyFactoryBean();
                serviceFactory.setServiceUrl(args.get(2));
                serviceFactory.setServiceInterface(IEntityService.class);
                serviceFactory.setRefreshStubOnConnectFailure(true);

                serviceFactory.afterPropertiesSet();
                entityServiceCore = (IEntityService) serviceFactory.getObject();
            } catch (Exception e) {
                System.out.println("Can't connect to receiver service: " + e.getMessage());
            }

            Gson gson = new Gson();

            String url = args.get(3);

            while (true) {
                ReceiverStatus receiverStatus = batchProcessService.getStatus();
                SyncStatus syncStatus = entityServiceSync.getStatus();
                HashMap<String, QueryEntry> map = entityServiceCore.getSQLStats();

                double totalInserts = 0;
                double totalSelects = 0;
                double totalUpdates = 0;
                double totalDeletes = 0;
                double totalProcess = 0;
                int totalProcessCount = 0;

                for (String query : map.keySet()) {
                    QueryEntry qe = map.get(query);

                    if (query.trim().toLowerCase().startsWith("insert")) {
                        totalInserts += qe.totalTime;
                    }
                    if (query.trim().toLowerCase().startsWith("select")) {
                        totalSelects += qe.totalTime;
                    }
                    if (query.trim().toLowerCase().startsWith("update")) {
                        totalUpdates += qe.totalTime;
                    }
                    if (query.trim().toLowerCase().startsWith("delete")) {
                        totalDeletes += qe.totalTime;
                    }
                    if (query.startsWith("coreService")) {
                        totalProcess += qe.totalTime;
                        totalProcessCount += qe.count;
                    }
                }

                CoreStatus coreStatus = new CoreStatus();

                coreStatus.setTotalProcessed(totalProcessCount);

                if (totalProcessCount > 0) {
                    coreStatus.setAvgProcessed(totalProcess / totalProcessCount);
                    coreStatus.setAvgInserts(totalInserts / totalProcessCount);
                    coreStatus.setAvgSelects(totalSelects / totalProcessCount);
                    coreStatus.setAvgDeletes(totalDeletes / totalProcessCount);
                    coreStatus.setAvgUpdates(totalUpdates / totalProcessCount);
                }

                entityServiceCore.clearSQLStats();

                SystemStatus systemStatus = new SystemStatus(receiverStatus, syncStatus, coreStatus);

                String systemStatusString = gson.toJson(systemStatus);

                HttpClient httpClient = new DefaultHttpClient();

                try {
                    HttpPost request = new HttpPost(url);
                    StringEntity params = new StringEntity(systemStatusString);
                    request.addHeader("content-type", "application/x-www-form-urlencoded");
                    request.setEntity(params);
                    HttpResponse response = httpClient.execute(request);

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(response.getEntity().getContent(), writer);
                    String resultString = writer.toString();

                    System.out.println(resultString);

                    if (!resultString.equals("COMMAND: nocommand") && !resultString.startsWith("ERROR:")) {
                        String command = resultString.substring(9);

                        System.out.println("Command found: " + command);
                        processCommand(command.trim(), null);
                        System.out.println("Command done");
                    }
                } catch (Exception ex) {
                    // handle exception here
                    ex.printStackTrace();
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }

                try {
                    Thread.sleep(15000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else {
            System.out.println("Argument needed: <receiver_url> <sync_url> <core_url> <remote_url>");
            System.out.println("Example: remotestat rmi://127.0.0.1:1097/batchProcessService " +
                    "rmi://127.0.0.1:1098/entityService rmi://127.0.0.1:1099/entityService " +
                    "http://localhost/usci/importer.php");
        }
    }

    public void commandSStat() {
        if (args.size() > 0) {
            RmiProxyFactoryBean entityServiceFactoryBean = null;

            kz.bsbnb.usci.sync.service.IEntityService entityService = null;

            try {
                entityServiceFactoryBean = new RmiProxyFactoryBean();
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
            System.out.println("Argument needed: <sync_url>");
            System.out.println("Example: sstat rmi://127.0.0.1:1098/entityService");
        }
    }

    public void commandSQLStat() {
        if (args.size() > 0) {
            RmiProxyFactoryBean serviceFactory = null;

            IEntityService entityService = null;

            try {
                serviceFactory = new RmiProxyFactoryBean();
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

            if (totalProcessCount > 0) {
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

    public void commandSTC() {
        if (args.size() > 2) {
            Connection conn = null;

            RmiProxyFactoryBean serviceFactory = null;

            kz.bsbnb.usci.sync.service.IEntityService entityService = null;

            try {
                serviceFactory = new RmiProxyFactoryBean();
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

            if (entityService != null)
                entityService.setThreadsCount(tCount, allowAutoIncrement);
        } else {
            System.out.println("Argument needed: <core_url> <threads_count> <allow_auto_increment>");
            System.out.println("Example: stc rmi://127.0.0.1:1098/entityService 32 false");
        }
    }

    public void commandEntity() {
        if (args.size() > 1) {
            if (args.get(0).equals("show")) {
                if (args.get(1).equals("id")) {
                    if (args.get(2) != null && args.get(2).equals("tojava")) {
                        entityToJava(Long.parseLong(args.get(3)), args.size() > 4 ? args.get(4) : null);
                    } else {
                        showEntity(Long.parseLong(args.get(2)), args.size() > 3 ? args.get(3) : null);
                    }
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
            } else if (args.get(0).equals("xml")) {
                if (args.size() > 2) {
                    if (args.get(1).equals("delete"))
                        dumpDeleteEntityToXML(args.get(2), args.get(3));
                    else
                        dumpEntityToXML(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <xml> <id> <fileName>");
                    System.out.println("Argument needed: <xml> delete <id> <fileName>");
                }
            } else if (args.get(0).equals("rm")) {
                if (args.size() > 2) {
                    removeEntityById(Long.parseLong(args.get(1)), args.get(2));
                } else {
                    System.out.println("Argument needed: <rm> <id> <service_url>");
                    System.out.println("Example: rm 100 rmi://127.0.0.1:1099/batchEntryService");
                }
            } else if (args.get(0).equals("rmall")) {
                if (args.size() > 1) {
                    removeAllEntityByMetaId(Long.parseLong(args.get(1)));
                    System.out.println("All entities with CLASS_ID " + args.get(1) + " has been removed");
                } else {
                    System.out.println("Argument needed: <rmall> <meta_class_id>");
                    System.out.println("Example: rmall 59");
                }
            } else if (args.get(0).equals("read")) {
                if (args.size() > 2) {
                    readEntityFromXML(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <read> <fileName> <rep_date>");
                }
            } else if (args.get(0).equals("find")) {
                if (args.size() > 2) {
                    findEntityFromXML(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <find> <fileName> <rep_date>");
                }
            } else if (args.get(0).equals("test")) {
                if (args.size() > 2) {
                    testEntityFromXML(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <test> <fileName> <rep_date>");
                }
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <show, read> <id, attr, sq, inter> <id> [attributePath, id2]");
        }
    }

    public void commandTest() {
        if (storage.testConnection()) {
            System.out.println("Connected to DB.");
        }

        try {
            if (storage.isClean()) {
                System.out.println("DB is empty");
            } else {
                System.out.println("DB with data");
            }
        } catch (BadSqlGrammarException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("EAV structure might be corrupted. Try clear/init.");
        }
    }

    public void commandSql() {

        System.out.println("ok sql mode...");
        StringBuilder str = new StringBuilder();
        for (Object o : args)
            str.append(o + " ");
        System.out.println(str.toString());
        boolean res = storage.simpleSql(str.toString());
        System.out.println(res ? "success" : "fail");

    }

    public void commandRefs() {
        if (args.get(0).equals("import")) {
            if (args.size() > 1)
                BaseCrawler.fileName = args.get(1);
            else {
                BaseCrawler.fileName = "D:\\refs\\";
                System.out.println("using default file " + BaseCrawler.fileName);
            }
            new BaseRepository().run();
        } else throw new IllegalArgumentException("allowed operations refs [import] [filename]");
    }

    public void init() {
        try {
            entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
            entityServiceFactoryBean.setServiceInterface(IBaseEntityProcessorDao.class);

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

    public void commandRule(Scanner in) {
        if (!started) {
            init();
            started = true;
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            if (args.get(0).equals("debug")) {

                DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
                Date date = dateFormatter.parse("05.04.2015");

                try {
                    CLIXMLReader reader = new CLIXMLReader("c:/a.xml", metaClassRepository, batchRepository, date);
                    BaseEntity baseEntity = reader.read();
                    //System.out.println(ma);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (args.get(0).equals("dump")) {
                if (args.size() < 2)
                    System.out.println("using default dump file path " + defaultDumpFile);
                try {
                    PrintWriter out = new PrintWriter(args.size() < 2 ? defaultDumpFile : args.get(1));
                    List<Rule> rules = ruleService.getAllRules();

                    out.println("#rule set date 01.04.2013");
                    out.println("rule create package afk");
                    out.println("rule rc");
                    out.println("rule set package afk");
                    out.println("rule set version\n");
                    for (Rule r : rules) {
                        out.println("rule read $$$");
                        out.println("title: " + r.getTitle());
                        out.println(r.getRule());
                        out.println("$$$\n");
                        out.println("rule save\n");
                    }
                    out.println("quit");
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (args.get(0).equals("read")) {
                if (args.size() < 2) {
                    throw new IllegalArgumentException();
                } else {
                    System.out.println("reading until " + args.get(1) + "...");
                    StringBuilder sb = new StringBuilder();
                    line = in.nextLine();
                    if (!line.startsWith("title: "))
                        throw new IllegalArgumentException("title must be specified format title: <name>");
                    String title = line.split("title: ")[1];
                    line = in.nextLine();
                    if (line.startsWith(args.get(1)))
                        throw new IllegalArgumentException("rule must not be empty");
                    sb.append(line);
                    do {
                        line = in.nextLine();
                        if (line.startsWith(args.get(1))) break;
                        sb.append("\n" + line);
                    } while (true);
                    currentRule = new Rule();
                    currentRule.setRule(sb.toString());
                    currentRule.setTitle(title);
                }
            } else if (args.get(0).equals("current")) {
                if (args.size() == 1)
                    System.out.println(currentRule == null ? null : currentRule.getRule());
                else if (args.get(1).equals("version"))
                    System.out.println(currentBatchVersion);
                else if (args.get(1).equals("package"))
                    System.out.println(currentPackageName);
                else if (args.get(1).equals("date"))
                    System.out.println(currentDate);
                else if (args.get(1).equals("entity"))
                    System.out.println(currentBaseEntity);
                else throw new IllegalArgumentException();
            } else if (args.get(0).equals("save")) {
                long ruleId = ruleService.createNewRuleInBatch(currentRule, currentBatchVersion);
                System.out.println("ok saved: ruleId = " + ruleId);
            } else if (args.get(0).equals("run")) {

                DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
                Date reportDate = dateFormatter.parse("02.05.2015");

                try {
                    CLIXMLReader reader = new CLIXMLReader("c:/a.xml", metaClassRepository, batchRepository, reportDate);
                    currentBaseEntity = reader.read();
                    reader.close();
                    List<String> errors = ruleService.runRules(currentBaseEntity, currentPackageName,currentDate);

                    for (String s : errors)
                        System.out.println("Validation error:" + s);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            } else if (args.get(0).equals("set")) {

                if (args.get(1).equals("version")) {
                    currentBatchVersion = batchVersionService.getBatchVersion(currentPackageName, currentDate);
                } else if (args.size() < 3) throw new IllegalArgumentException();
                else if (args.get(1).equals("package")) {
                    ruleService.getRulePackageName(args.get(2), currentDate);
                    currentPackageName = args.get(2);
                } else if (args.get(1).equals("date")) {
                    DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    currentDate = formatter.parse(args.get(2));
                } else throw new IllegalArgumentException();
            } else if (args.get(0).equals("create")) {
                try {
                    if (!args.get(1).equals("package") || args.size() < 3) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    throw e;
                }
                kz.bsbnb.usci.brms.rulesvr.model.impl.Batch batch =
                        new kz.bsbnb.usci.brms.rulesvr.model.impl.Batch(args.get(2), currentDate);

                Long id = batchService.save(batch);
                batch.setId(id);
                batchVersionService.save(batch);
                System.out.println("ok batch created with id:" + id);
            } else if (args.get(0).equals("rc")) {
                ruleService.reloadCache();
            } else if (args.get(0).equals("eval")) {
                System.out.println(currentBaseEntity.getEls(args.get(1)));
            } else if (args.get(0).equals("eval2")) {
                System.out.println(currentBaseEntity.getEl(args.get(1)));
            } else if (args.get(0).equals("clear")) {
                ruleService.clearAllRules();
            } else if(args.get(0).equals("import")) {
                try {
                    Scanner importedPath = new Scanner(new File(args.get(1)));

                    while(importedPath.hasNext()) {
                        String line1 = importedPath.nextLine();
                        if(line1.equals("") || line1.startsWith("#"))
                            continue;
                        StringTokenizer st = new StringTokenizer(line1);
                        command = st.nextToken().trim();
                        if(command.equals("quit")) break;
                        if (st.hasMoreTokens()) {
                            args.clear();
                            while (st.hasMoreTokens()) {
                                args.add(st.nextToken().trim());
                            }
                        }
                        commandRule(importedPath);
                    }

                    importedPath.close();

                } catch(Exception e){
                    e.printStackTrace();
                }
            }else throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() == null)
                System.out.println("Argument needed: <read {label},current [<pckName,date>],save,run {id}," +
                        "set <package,date> {value} , create package {pckName}>");
            else
                System.out.println(e.getMessage());
            return;
        } catch (ParseException e) {
            System.out.println("Parse exception day format must be: dd.MM.yyyy");
            return;
        } catch (IncorrectResultSizeDataAccessException e) {
            System.out.println("no packages(maybe on that date)");
            return;
        }

        //rulesSingleton.runRules(entity, entity.getMeta().getClassName() + "_parser", entity.getReportDate());*/
    }

    public void showcaseStat() {
        RmiProxyFactoryBean serviceFactory = null;
        ShowcaseService showcaseService = null;

        try {
            serviceFactory = new RmiProxyFactoryBean();
            serviceFactory.setServiceUrl("rmi://127.0.0.1:1095/showcaseService");
            serviceFactory.setServiceInterface(ShowcaseService.class);
            serviceFactory.setRefreshStubOnConnectFailure(true);

            serviceFactory.afterPropertiesSet();
            showcaseService = (ShowcaseService) serviceFactory.getObject();
        } catch (Exception e) {
            System.out.println("Can't connect to receiver service: " + e.getMessage());
        }

        HashMap<String, QueryEntry> map = showcaseService.getSQLStats();

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

        if (totalProcessCount > 0) {
            System.out.println("AVG process: " + totalProcess / totalProcessCount);
            System.out.println("AVG inserts per process: " + totalInserts / totalProcessCount);
            System.out.println("AVG selects per process: " + totalSelects / totalProcessCount);
        }

        //showcaseService.clearSQLStats();
    }

    private void initSC() {
        try {
            showcaseServiceFactoryBean = new RmiProxyFactoryBean();
            showcaseServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1095/showcaseService");
            showcaseServiceFactoryBean.setServiceInterface(ShowcaseService.class);

            showcaseServiceFactoryBean.afterPropertiesSet();
            showcaseService = (ShowcaseService) showcaseServiceFactoryBean.getObject();
        } catch (Exception e) {
            showcaseServiceFactoryBean = null;
            showcaseService = null;

            System.err.println("Couldn't connect to ShowCaseService");
            System.err.println(e.getMessage());
        }

    }

    public void commandShowCase() {
        // Fast Init
        if (showCase == null) {
            showCase = new ShowCase();
            showCase.setMeta(metaClassRepository.getMetaClass("credit"));
        }

        if(showcaseServiceFactoryBean == null || showcaseService == null)
            initSC();

        if (args.get(0).equals("status")) {
            System.out.println(showCase.toString());

            for (ShowCaseField sf : showCase.getFieldsList())
                System.out.println(sf.getAttributePath() + ", " + sf.getColumnName());

            for (ShowCaseField sf : showCase.getCustomFieldsList())
                System.out.println("* " + sf.getAttributePath() + ", " + sf.getColumnName());
        } else if (args.get(0).equals("set")) {
            if (args.size() != 3)
                throw new IllegalArgumentException("showcase set [meta,name,tableName,downPath] {value}");
            if (args.get(1).equals("meta")) {
                showCase = new ShowCase();
                showCase.setMeta(metaClassRepository.getMetaClass(args.get(2)));
            } else if (args.get(1).equals("name")) {
                showCase.setName(args.get(2));
            } else if (args.get(1).equals("tableName")) {
                showCase.setTableName(args.get(2));
            } else if (args.get(1).equals("title")) {
                showCase.setTitle(args.get(2));
            } else if (args.get(1).equals("downPath")) {
                MetaClass metaClass = showCase.getMeta();
                if (metaClass.getEl(args.get(2)) == null)
                    throw new IllegalArgumentException("no such path for downPath:" + args.get(2));

                showCase.setDownPath(args.get(2));
            } else if(args.get(1).equals("final")) {
                showCase.setFinal(Boolean.parseBoolean(args.get(2)));
            } else
                throw new IllegalArgumentException("showcase set [meta,name,tableName,downPath] {value}");

        } else if (args.get(0).equals("list")) {
            if (args.get(1).equals("reset")) {
                showCase.getFieldsList().clear();
            } else if (args.get(1).equals("add")) {
                if(args.get(2) == null || args.get(3) == null)
                    throw new UnsupportedOperationException("AttributePath and columnName cannot be empty");

                showCase.addField(args.get(2), args.get(3));
            } else if (args.get(1).equals("addCustom")) {
                if(args.get(2) == null || args.get(3) == null || args.get(4) == null)
                    throw new UnsupportedOperationException("MetaClass, attributePath and columnName cannot be empty");

                MetaClass customMeta;

                if(args.get(2).equals("root")) {
                    customMeta = metaClassRepository.getMetaClass(args.get(2));
                } else {
                    customMeta = metaClassRepository.getMetaClass(args.get(2));
                }

                showCase.addCustomField(args.get(3), args.get(4), metaClassRepository.getMetaClass(args.get(2)));
            } else {
                System.err.println("Example: showcase list add [path] [columnName]");
                System.err.println("Example: showcase list addCustom metaClass [path] [columnName]");
                throw new IllegalArgumentException();
            }
        } else if (args.get(0).equals("save")) {
            long scId = showcaseService.add(showCase);
            if(scId > 0)
                System.out.println(showCase.getName() + ": Showcase successfully added!");
            else
                System.err.println("Couldn't save " + showCase.getName());
            showCase = null;
        } else if (args.get(0).equals("listSC")) {
            List<ShowcaseHolder> list = showcaseService.list();
            for (ShowcaseHolder holder : list) {
                System.out.println(holder.getShowCaseMeta().getName());

                for (ShowCaseField field : holder.getShowCaseMeta().getFieldsList())
                    System.out.println("\t" + field.getColumnName());

                for(ShowCaseField field : holder.getShowCaseMeta().getCustomFieldsList())
                    System.out.println("\t* " + field.getColumnName());
            }
        } else if (args.get(0).equals("loadSC")) {
            if (args.size() > 1) {
                ShowCase sc = showcaseService.load(args.get(1));
                System.out.println(sc.getName());

                for (ShowCaseField field : sc.getFieldsList())
                    System.out.println("\t" + field.getColumnName());

                for (ShowCaseField field : sc.getCustomFieldsList())
                    System.out.println("\t* " + field.getColumnName());

            } else {
                System.out.println("Usage: loadSC <showcase name>");
            }
        } else if (args.get(0).equals("startLoad")) {
            if (args.size() > 3) {
                try {
                    showcaseService.startLoad(args.get(1), sdfout.parse(args.get(2)),
                            Boolean.parseBoolean(args.get(3).toString()));
                } catch (ParseException e) {
                    System.out.println("Date format: \"dd.MM.yyyy\"");
                }
            } else {
                System.out.println("Usage: startLoad <showcase name> <report_date> populate");
            }
        } else if(args.get(0).equals("startLoadHistory")){
            boolean populate = false;

            if (args.size() > 1 && "populate".equals(args.get(1))) {
                populate = true;
            }

            Queue<Long> creditorIdsQueue = null;

            if (args.size() > 2) {
                creditorIdsQueue = new LinkedList<>();
                for (int i = 2; i < args.size(); i++) {
                    creditorIdsQueue.add(Long.valueOf(args.get(i)));
                }
            }

            showcaseService.startLoadHistory(populate, creditorIdsQueue);

        } else if (args.get(0).equals("stopLoad")) {
            if (args.size() > 1) {
                showcaseService.stopLoad(args.get(1));
            } else {
                System.out.println("Usage: stopLoad <showcase name>");
            }
        } else if(args.get(0).equals("stopLoadHistory")){
            showcaseService.stopLoadHistory();

        } else if (args.get(0).equals("pauseLoad")) {
            if (args.size() > 1) {
                showcaseService.pauseLoad(args.get(1));
            } else {
                System.out.println("Usage: pauseLoad <showcase name>");
            }
        } else if (args.get(0).equals("resumeLoad")) {
            if (args.size() > 1) {
                showcaseService.resumeLoad(args.get(1));
            } else {
                System.out.println("Usage: resumeLoad <showcase name>");
            }
        } else if (args.get(0).equals("listLoading")) {
            for (String sc : showcaseService.listLoading()) {
                System.out.println("\t" + sc);
            }
        } else if (args.get(0).equals("rc")) {
            showcaseService.reloadCash();
        } else if (args.get(0).equals("stats")) {
            showcaseStat();
        } else {
            throw new IllegalArgumentException("Arguments: showcase [status, set]");
        }
    }

    public Exception getLastException() {
        return lastException;
    }

    public void processCommand(String line, Scanner in) {
        StringTokenizer st = new StringTokenizer(line);
        if (st.hasMoreTokens()) {
            command = st.nextToken().trim();

            args.clear();
            while (st.hasMoreTokens()) {
                args.add(st.nextToken().trim());
            }
        } else {
            return;
        }


        if (command.startsWith("#")) {
            return;
        }

        try {

            if (command.equals("test")) {
                commandTest();
            } else if (command.equals("clear")) {
                System.out.println("Storage clearing...");
                storage.clear();
                System.out.println("Storage cleared");
            } else if (command.equals("rc")) {
                metaClassRepository.resetCache();
            } else if (command.equals("init")) {
                System.out.println("Storage initializing...");
                storage.initialize();
                System.out.println("Storage initialized");
            } else if (command.equals("empty")) {
                storage.empty();
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
            } else if (command.equals("include")) {
                commandInclude();
            } else if (command.equals("refs")) {
                commandRefs();
            } else if (command.equals("sql")) {
                commandSql();
            } else if (command.equals("rule")) {
                commandRule(in);
            } else if (command.equals("import")) {
                commandImport();
            } else if (command.equals("collectids")) {
                commandCollectIds();
            } else if (command.equals("rstat")) {
                commandRStat();
            } else if (command.equals("sstat")) {
                commandSStat();
            } else if (command.equals("sqlstat")) {
                commandSQLStat();
            } else if (command.equals("remotestat")) {
                commandRemoteStat();
            } else if (command.equals("batchstat")) {
                batchStat();
            } else if (command.equals("batchrestart")) {
                batchRestart();
            } else if (command.equals("batchrestartall")) {
                batchRestartAll();
            } else if (command.equals("sbatchrestart")) {
                batchRestartSingle();
            } else if (command.equals("stc")) {
                commandSTC();
            } else if (command.equals("showcase")) {
                commandShowCase();
            } else if (command.equals("merge")) {
                mergeEntity();
            } else if (command.equals("getbatch")) {
                commandGetBatch();
            } else if (command.equals("mnt")) {
                commandMaintenance(line);
            } else {
                System.out.println("No such command: " + command);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            lastException = e;
        }
    }

    private void commandMaintenance(String line) {
        if(mnt == null)
            mnt = context.getBean(Mnt.class);

        mnt.commandMaintenance(line);
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

        while (true) {
            while (!(line = in.nextLine()).equals("quit")) {
                processCommand(line, in);

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

        shutdown();
    }

    public void commandInclude() {
        if (args.size() > 0) {
            String fileName = args.get(0);

            System.out.println("Using file: " + fileName);

            try {
                Scanner in = new Scanner(new FileInputStream(new File(fileName)));

                while (!(line = in.nextLine()).equals("quit")) {
                    processCommand(line, in);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Argument needed: <sync_url>");
            System.out.println("Example: sstat rmi://127.0.0.1:1098/entityService");
        }
    }

    public void commandGetBatch() {
        if (args.size() > 1) {
            Long batchId = Long.valueOf(args.get(0));
            String path = args.get(1);

            Gson gson = new Gson();
            BatchFullJModel batchFullJModel;

            Object batchStr;

            try {
                batchStr = couchbaseClient.get("batch:" + batchId);
            } catch (OperationTimeoutException e) {
                batchStr = null;
            }

            if (batchStr == null) {
                System.out.println("Couldn't find batch with id: " + batchId + " in Couchbase");
                throw new NullPointerException();
            }

            batchFullJModel = gson.fromJson(batchStr.toString(), BatchFullJModel.class);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(batchFullJModel.getContent());

            ZipOutputStream zos = null;

            try {
                zos = new ZipOutputStream(new FileOutputStream(new File(path + batchId + ".zip")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry entry;

            try {
                while ((entry = zis.getNextEntry()) != null) {
                    ZipEntry destEntry = new ZipEntry(entry.getName());
                    zos.putNextEntry(destEntry);

                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = zis.read(buf)) > 0) zos.write(buf, 0, len);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                inputStream.close();
                zos.closeEntry();
                zos.close();
                zis.closeEntry();
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Batch with ID: " + batchId + " sucessfully moved to \"" + path + "\"");
        } else {
            System.out.println("Argument needed: <batchId> <path>");
            System.out.println("Example: getbatch 100500 /home/ktulbassiyev/");
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream in) {
        this.inputStream = in;
    }

    public void mergeEntity() {
        if (args.size() == 3) {
            String fileName = args.get(0);
            String repDate = args.get(1);
            String mergeManagerFileName = args.get(2);
            List<IBaseEntity> entityList = new ArrayList<IBaseEntity>();
            try {
                Date reportDate = sdfout.parse(repDate);
                CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchRepository, reportDate);
                BaseEntity entityToWrite;
                IBaseEntity savedEntity;
                while ((entityToWrite = reader.read()) != null) {
                    try {
                        savedEntity = baseEntityProcessorDao.process(entityToWrite);
                        entityList.add(savedEntity);
                        System.out.println("Instance of BaseEntity saved with id: " + savedEntity.getId());

                    } catch (Exception ex) {
                        lastException = ex;
                        System.out.println("While processing instance of BaseEntity unexpected error occurred: "
                                + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
            } catch (ParseException e) {
                System.out.println("Can't parse date " + repDate + " must be in format " + sdfout.toString());
            }
            IBaseEntityMergeManager mergeManager = constructMergeManagerFromJson(mergeManagerFileName);

            System.out.println("Merging two base entities " + entityList.get(0).getId() + "  "
                    + entityList.get(1).getId());
            System.out.println(">>>>>>>>>>>>>>LEFT ENTITY<<<<<<<<<<<<<<<<<<<<<");
            System.out.println(entityList.get(0));
            System.out.println("\n >>>>>>>>>>>>>>RIGHT ENTITY<<<<<<<<<<<<<<<<<<<<<");
            System.out.println(entityList.get(1));
            System.out.println("\n >>>>>>>>>>>>>>>RESULT!!!<<<<<<<<<<<<<<<<<<< ");
            IBaseEntity result = baseEntityProcessorDao.merge(entityList.get(0), entityList.get(1), mergeManager,
                    IBaseEntityProcessorDao.MergeResultChoice.LEFT, true);

            System.out.println(result);
        } else {
            System.out.println("Wrong number of arguments!");
        }
    }

    public IBaseEntityMergeManager constructMergeManagerFromJson(String jsonFilePath) {
        IBaseEntityMergeManager mergeManager = new BaseEntityMergeManager();
        try {
            FileReader fileReader = new FileReader(jsonFilePath);
            JsonReader jsonReader = new JsonReader(fileReader);
            jsonReader.beginObject();
            mergeManager = jsonToMergeManager(jsonReader);
            jsonReader.endObject();

        } catch (FileNotFoundException e) {
            System.out.println("File " + jsonFilePath + " not found, with error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mergeManager;
    }

    public IBaseEntityMergeManager jsonToMergeManager(JsonReader jsonReader) throws IOException {
        IBaseEntityMergeManager mergeManager = new BaseEntityMergeManager();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("action")) {
                String action = jsonReader.nextString();
                if (action.equals("keep_left"))
                    mergeManager.setAction(IBaseEntityMergeManager.Action.KEEP_LEFT);
                if (action.equals("keep_right"))
                    mergeManager.setAction(IBaseEntityMergeManager.Action.KEEP_RIGHT);
                if (action.equals("merge"))
                    mergeManager.setAction(IBaseEntityMergeManager.Action.TO_MERGE);
                if (action.equals("keep_both"))
                    mergeManager.setAction(IBaseEntityMergeManager.Action.KEEP_BOTH);
            } else if (name.equals("childMap")) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    MergeManagerKey key = null;
                    IBaseEntityMergeManager childManager = null;
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String innerName = jsonReader.nextName();
                        if (innerName.equals("id")) {
                            jsonReader.beginObject();
                            key = getKeyFromJson(jsonReader);
                            jsonReader.endObject();
                        } else if (innerName.equals("map")) {
                            jsonReader.beginObject();
                            childManager = jsonToMergeManager(jsonReader);
                            jsonReader.endObject();
                        }

                    }
                    jsonReader.endObject();
                    mergeManager.setChildManager(key, childManager);
                }
                jsonReader.endArray();
            }
        }
        return mergeManager;
    }

    private MergeManagerKey getKeyFromJson(JsonReader jsonReader) {
        String type = null;
        String left = null;
        String right = null;
        String attr = null;
        try {
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equals("type")) {
                    type = jsonReader.nextString();
                } else if (name.equals("left")) {
                    left = jsonReader.nextString();
                } else if (name.equals("right")) {
                    right = jsonReader.nextString();
                } else if (name.equals("attr")) {
                    attr = jsonReader.nextString();
                }
            }
            if (type.equals("attribute")) {
                MergeManagerKey<String> key = new MergeManagerKey<String>(attr);
                return key;
            }
            if (type.equals("long")) {
                MergeManagerKey<Long> key = new MergeManagerKey<Long>(Long.parseLong(left), Long.parseLong(right));
                return key;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    interface DispatcherJob {
        public boolean intersects(DispatcherJob job);

        public boolean isAlive();

        public void start();

        public void prepare();
    }

    class JobDispatcher extends Thread {
        private final int MAX_ACTIVE_THREADS = 32;
        private final int MAX_PREPARING_THREADS = 8;
        private ConcurrentLinkedQueue<DispatcherJob> threadsQueue = new ConcurrentLinkedQueue<DispatcherJob>();
        private ConcurrentLinkedQueue<DispatcherJob> preparedThreadsQueue = new ConcurrentLinkedQueue<DispatcherJob>();
        private ArrayList<ThreadPreparator> activePreparingThreads = new ArrayList<ThreadPreparator>();
        private ArrayList<DispatcherJob> activeThreads = new ArrayList<DispatcherJob>();
        private long jobsEnded = 0;

        public synchronized void addThread(DispatcherJob thread) {
            threadsQueue.add(thread);
        }

        public synchronized void addPreparedThread(DispatcherJob thread) {
            preparedThreadsQueue.add(thread);
        }

        public synchronized DispatcherJob getNextThread() {
            return threadsQueue.poll();
        }

        public synchronized DispatcherJob getNextPeparedThread() {
            return preparedThreadsQueue.poll();
        }

        public void clearDeadThreads() {
            Iterator<DispatcherJob> threadsIterator = activeThreads.iterator();

            while (threadsIterator.hasNext()) {
                DispatcherJob thread = threadsIterator.next();

                if (!thread.isAlive()) {
                    threadsIterator.remove();
                    jobsEnded++;
                }
            }
        }

        public void clearDeadPreparingThreads() {
            Iterator<ThreadPreparator> threadsIterator = activePreparingThreads.iterator();

            while (threadsIterator.hasNext()) {
                ThreadPreparator preparator = threadsIterator.next();

                if (!preparator.isAlive()) {
                    threadsIterator.remove();
                    addPreparedThread(preparator.getThread());
                }
            }
        }

        @Override
        public void run() {
            long t1 = System.currentTimeMillis();
            while (true) {
                try {
                    clearDeadPreparingThreads();
                    clearDeadThreads();

                    if (System.currentTimeMillis() - t1 > 5000) {
                        t1 = System.currentTimeMillis();
                        if (activeThreads.size() > 0 || activePreparingThreads.size() > 0 ||
                                threadsQueue.size() > 0) {
                            System.out.println("Active: " + activeThreads.size() + ", queue: " + threadsQueue.size() +
                                    ", ended: " + jobsEnded + ", preparing: " + activePreparingThreads.size() +
                                    ", prepared: " + preparedThreadsQueue.size());
                        }
                    }

                    if (activePreparingThreads.size() < MAX_PREPARING_THREADS) {
                        DispatcherJob newThread = getNextThread();
                        if (newThread != null) {
                            ThreadPreparator preparator = new ThreadPreparator(newThread);
                            preparator.start();
                            activePreparingThreads.add(preparator);
                        }
                    }

                    if (activeThreads.size() < MAX_ACTIVE_THREADS) {
                        DispatcherJob newThread = getNextPeparedThread();
                        if (newThread != null) {
                            boolean intersectionFound = false;
                            for (DispatcherJob job : activeThreads) {
                                if (job.intersects(newThread)) {
                                    intersectionFound = true;
                                    break;
                                }
                            }

                            if (intersectionFound) {
                                addThread(newThread);
                            } else {
                                newThread.start();
                                activeThreads.add(newThread);
                            }
                        } else {
                            try {
                                sleep(1000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ThreadPreparator extends Thread {
        DispatcherJob thread;

        ThreadPreparator(DispatcherJob thread) {
            this.thread = thread;
        }

        public DispatcherJob getThread() {
            return thread;
        }

        @Override
        public void run() {
            try {
                thread.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DeleteJob extends Thread implements DispatcherJob {

        private IEntityService entityServiceCore = null;
        private long id;
        private Set<Long> ids = null;

        DeleteJob(IEntityService entityServiceCore, long id) {
            this.entityServiceCore = entityServiceCore;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                System.out.println("Deleting entity with id: " + id);
                entityServiceCore.remove(id);
                System.out.println("Deleted entity with id: " + id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public boolean intersects(DispatcherJob job) {
            if (job instanceof DeleteJob) {
                if (ids == null || ((DeleteJob) job).ids == null)
                    throw new RuntimeException("Unprepared thread");

                Set<Long> inter = SetUtils.intersection(ids, ((DeleteJob) job).ids);

                return inter.size() > 0;
            }

            return false;
        }

        @Override
        public void prepare() {
            ids = entityServiceCore.getChildBaseEntityIds(id);
        }
    }

}

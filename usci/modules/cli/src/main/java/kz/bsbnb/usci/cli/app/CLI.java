package kz.bsbnb.usci.cli.app;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.bconv.xsd.XSDGenerator;
import kz.bsbnb.usci.bconv.xsd.Xsd2MetaClass;
import kz.bsbnb.usci.cli.app.command.impl.*;
import kz.bsbnb.usci.cli.app.common.impl.SqlRunner;
import kz.bsbnb.usci.cli.app.exporter.EntityExporter;
import kz.bsbnb.usci.cli.app.mnt.Mnt;
import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.eav.model.exceptions.KnownIterativeException;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.rule.impl.RulesSingleton;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.core.service.IPackageService;
import kz.bsbnb.usci.core.service.IRuleService;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityMergeManager;
import kz.bsbnb.usci.eav.manager.impl.MergeManagerKey;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.rule.PackageVersion;
import kz.bsbnb.usci.eav.rule.Rule;
import kz.bsbnb.usci.eav.rule.RulePackage;
import kz.bsbnb.usci.eav.showcase.EntityProcessorListenerImpl;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;
import kz.bsbnb.usci.eav.showcase.ShowCaseIndex;
import kz.bsbnb.usci.eav.model.stats.QueryEntry;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.impl.BaseEntityXmlGenerator;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import kz.bsbnb.usci.showcase.service.ShowcaseService;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.CoreStatus;
import kz.bsbnb.usci.tool.status.ReceiverStatus;
import kz.bsbnb.usci.tool.status.SyncStatus;
import kz.bsbnb.usci.tool.status.SystemStatus;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class CLI {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired private IMetaClassRepository metaClassRepository;

    @Autowired private IStorage storage;

    @Autowired private IMetaClassDao metaClassDao;

    @Autowired private IEavGlobalDao eavGlobalDao;

    @Autowired private Xsd2MetaClass xsdConverter;

    @Autowired private XSDGenerator xsdGenerator;

    @Autowired private MainParser crParser;

    @Qualifier("baseEntityProcessor")
    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired private IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired private IBaseEntityMergeDao baseEntityMergeDao;

    @Autowired private ImprovedBaseEntitySearcher searcher;

    @Autowired private ApplicationContext context;

    @Autowired private EntityExporter entityExporter;

    @Autowired private IBaseEntityDao baseEntityDao;

    @Autowired private IDaoListener applyListener;

    @Autowired private ISQLGenerator sqlGenerator;

    @Autowired
    EntityProcessorListenerImpl entityProcessorListener;


    private IEntityService entityServiceCore = null;

    private ShowCase showCase;

    private ShowCase childShowCase;

    private String line;

    private Exception lastException = null;

    private String command;

    private ArrayList<String> args = new ArrayList<>();

    private InputStream inputStream = null;

    private JobDispatcher jobDispatcher = new JobDispatcher();

    private RmiProxyFactoryBean showcaseServiceFactoryBean;

    private IRuleService ruleService;

    private IBatchService batchService;

    private IPackageService packageService;

    private ShowcaseService showcaseService;

    private Rule currentRule;

    private String currentPackageName = "credit_parser";

    private Date currentDate = new Date();

    private boolean started = false;

    private PackageVersion currentPackageVersion;

    private RulePackage currentRulePackage;

    private BaseEntity currentBaseEntity;

    private Mnt mnt;

    private JdbcTemplate jdbcTemplateSC;


    public void InitDataSourceSC(String Driver, String Username, String password, String url) {
        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName(Driver);
        source.setUrl(url);
        source.setUsername(Username);
        source.setPassword(password);
        source.setInitialSize(16);
        source.setMaxActive(16);
        source.setTestOnBorrow(true);
        source.setTestOnReturn(true);
        this.jdbcTemplateSC = new JdbcTemplate(source);
    }

    public IEntityService getEntityService(String url) {
        if (entityServiceCore == null) {
            try {
                RmiProxyFactoryBean serviceFactory = new RmiProxyFactoryBean();
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

    public void initBatchService() {
        if (batchService == null) {
            RmiProxyFactoryBean batchServiceFactoryBean = new RmiProxyFactoryBean();
            batchServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/batchService");
            batchServiceFactoryBean.setServiceInterface(IBatchService.class);
            batchServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchServiceFactoryBean.afterPropertiesSet();
            batchService = (IBatchService) batchServiceFactoryBean.getObject();
        }
    }

    @PostConstruct
    public void initBean() {
        initBatchService();
        jobDispatcher.start();
    }

    public void processCRBatch(String fileName, int count, int offset, Date repDate)
            throws SAXException, IOException, XMLStreamException {
        File inFile = new File(fileName);

        InputStream in;
        in = new FileInputStream(inFile);

        System.out.println("Processing batch with rep date: " + repDate);

        Batch batch = new Batch(repDate);
        batch.setUserId(0L);

        long batchId = batchService.save(batch);
        batch.setId(batchId);

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

        InputStream in;
        in = new FileInputStream(inFile);

        System.out.println("Parsing...");
        MetaClass meta = xsdConverter.convertXSD(in, metaClassName);

        System.out.println("Saving...");
        long id = metaClassDao.save(meta);

        System.out.println("Saved with id: " + id);
    }

    public void listXSD(String fname) throws FileNotFoundException {
        File inFile = new File(fname);

        InputStream in;
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

    public void showEntity(long id, String reportDateStr) {
        IBaseEntity entity;

        if (reportDateStr != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            try {
                entity = baseEntityLoadDao.loadByMaxReportDate(id, dateFormat.parse(reportDateStr));
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
        } else {
            entity = baseEntityLoadDao.load(id);
        }

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            String output = entity.toString();
            String[] lines = output.split(System.getProperty("line.separator"));

            for (String str : lines) {
                System.out.println(str);
            }
        }
    }

    public void batchStat() {
        if (args.size() < 5) {
            System.out.println("Usage: <report_date> <output_file>");
            System.out.println("Example: batchstat 01.05.2013 " +
                    "D:\\usci\\out.txt jdbc:oracle:thin:@172.17.110.92:1521:XE core core");
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

                            Batch batch = batchService.getBatch(batchId);

                            if (DataUtils.compareBeginningOfTheDay(batch.getRepDate(), reportDate) != 0) {
                                continue;
                            }

                            List<EntityStatus> entityStatusList = batchService.getEntityStatusList(batchId);

                            int row_count = 0;
                            int error_count = 0;

                            for (EntityStatus entityStatus : entityStatusList) {
                                row_count++;

                                boolean errorFound = false;
                                boolean completedFound = false;

                                if (entityStatus.getStatus() == EntityStatuses.ERROR) {
                                    errorFound = true;
                                }
                                if (entityStatus.getStatus() == EntityStatuses.COMPLETED) {
                                    completedFound = true;
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
                                    batch.getFileName() + "," +
                                    batch.getActualCount() + "," +
                                    row_count + "," + error_count + "\n").getBytes());
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
                    "jdbc:oracle:thin:@172.17.110.92:1521:XE core core rmi://127.0.0.1:1097/batchProcessService");
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

                            Batch batch = batchService.getBatch(batchId);

                            if (DataUtils.compareBeginningOfTheDay(batch.getRepDate(), reportDate) != 0) {
                                continue;
                            }

                            List<EntityStatus> entityStatusList = batchService.getEntityStatusList(batchId);

                            int row_count = 0;
                            int error_count = 0;

                            for (EntityStatus entityStatus : entityStatusList) {
                                row_count++;

                                boolean errorFound = false;
                                boolean completedFound = false;

                                if (entityStatus.getStatus() == EntityStatuses.ERROR) {
                                    errorFound = true;
                                }
                                if (entityStatus.getStatus() == EntityStatuses.COMPLETED) {
                                    completedFound = true;
                                }

                                if (errorFound && !completedFound)
                                    error_count++;
                            }

                            if (error_count > 0 || row_count != batch.getTotalCount()) {
                                fout.write((batchId + "," +
                                        batch.getFileName() + "," +
                                        batch.getTotalCount() + "," + row_count + "," + error_count +
                                        ",restarted\n").getBytes());

                                //sender.addJob(batchId, batchInfo);
                                //receiverStatusSingleton.batchReceived();
                                batchProcessService.restartBatch(batchId);
                            } else {
                                fout.write((batchId + "," +
                                        batch.getFileName() + "," +
                                        batch.getTotalCount() + "," + row_count + "," + error_count +
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
                    "core core rmi://127.0.0.1:1097/batchProcessService");
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

                            Batch batch = batchService.getBatch(batchId);

                            if (DataUtils.compareBeginningOfTheDay(batch.getRepDate(), reportDate) != 0) {
                                continue;
                            }

                            fout.write((batchId + "," +
                                    batch.getFileName() + "," +
                                    batch.getActualCount() + ",restarted\n").getBytes());

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

        Batch batch = batchService.getBatch(batchId);

        List<EntityStatus> entityStatusList = batchService.getEntityStatusList(batchId);

        int row_count = 0;
        int error_count = 0;

        for (EntityStatus entityStatus : entityStatusList) {
            row_count++;

            boolean errorFound = false;
            boolean completedFound = false;

            if (entityStatus.getStatus() == EntityStatuses.ERROR) {
                errorFound = true;
            }
            if (entityStatus.getStatus() == EntityStatuses.COMPLETED) {
                completedFound = true;
            }

            if (errorFound)// && !completedFound)
                error_count++;
        }

        //if (error_count > 0 || row_count != batchInfo.getSize()) {
        System.out.println(batchId + "," +
                batch.getFileName() + "," +
                batch.getActualCount() + "," + row_count + "," + error_count + ",restarted");

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

    public void removeEntityById(long id) {
        baseEntityDao.deleteRecursive(id);
    }

    public void dumpEntityToXML(String ids, String fileName) {
        StringTokenizer st = new StringTokenizer(ids, ",");
        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        while (st.hasMoreTokens()) {
            long id = Long.parseLong(st.nextToken());
            IBaseEntity entity = baseEntityLoadDao.load(id);
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
            IBaseEntity entity = baseEntityLoadDao.load(id);
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
            Date reportDate = simpleDateFormat.parse(repDate);
            CLIXMLReader reader = new CLIXMLReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
                    , metaClassRepository, batchService, reportDate, 0);

            BaseEntity entity;
            while ((entity = reader.read()) != null) {
                try {
                    long id = baseEntityProcessorDao.process(entity).getId();
                    System.out.println("Запись сохранилась с ИД: " + id);
                } catch (Exception ex) {
                    lastException = ex;
                    System.out.println("Ошибка: " + ex.getMessage());
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void readEntityFromXML(String fileName, String repDate, long creditorId) {
        try {
            Date reportDate = simpleDateFormat.parse(repDate);
            CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchService, reportDate, creditorId);
            BaseEntity entity;

            long totalCount = 0;
            long actualCount = 0;

            while ((entity = reader.read()) != null) {
                totalCount++;
                try {
                    long id = baseEntityProcessorDao.process(entity).getId();
                    System.out.println(entity.getMeta().getClassName() + " сохранился с ИД: " + id);
                    actualCount++;
                } catch (Exception ex) {
                    if(ex instanceof KnownIterativeException) {
                        for (String s : ((KnownIterativeException) ex).getMessages()) {
                            System.out.println(s);
                        }
                    } else {
                        lastException = ex;
                        System.out.println("Ошибка: " + Errors.decompose(ex.getMessage()));
                    }
                }
            }
            Batch batch = reader.getBatch();
            batch.setActualCount(actualCount);
            batch.setTotalCount(totalCount);
            batchService.save(batch);
        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Can't parse date " + repDate + " must be in format " + simpleDateFormat.toString());
        }
    }

    public void findEntityFromXML(String fileName, String repDate) {
        try {
            Date reportDate = simpleDateFormat.parse(repDate);
            // checkme!
            CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchService, reportDate, 0);
            BaseEntity entity;
            while ((entity = reader.read()) != null) {
                long creditorId = 0L;

                if (entity.getMeta().getClassName().equals("credit"))
                    creditorId = ((BaseEntity) entity.getEl("creditor")).getId();


                ArrayList<Long> ids = searcher.findAll(entity, creditorId);
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
            System.out.println("Can't parse date " + repDate + " must be in format " + simpleDateFormat.toString());
        }

    }

    public void showEntityAttr(String path, long id) {
        IBaseEntity entity = baseEntityLoadDao.load(id);

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
        IBaseEntity entity = baseEntityLoadDao.load(id);

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            long creditorId = 0L;

            if (entity.getMeta().getClassName().equals("credit"))
                creditorId = ((BaseEntity) entity.getEl("creditor")).getId();

            SelectConditionStep where = searcher.generateSQL(entity, creditorId, null);

            if (where != null) {
                System.out.println(where.getSQL(true));
            } else {
                System.out.println("Error generating SQL.");
            }
        }
    }

    public void execEntitySQ(long id) {
        IBaseEntity entity = baseEntityLoadDao.load(id);

        if (entity == null) {
            System.out.println("No such entity with id: " + id);
        } else {
            long creditorId = 0L;

            if (entity.getMeta().getClassName().equals("credit"))
                creditorId = ((BaseEntity) entity.getEl("creditor")).getId();

            ArrayList<Long> array = searcher.findAll((BaseEntity) entity, creditorId);

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

        List<BaseEntity> entities = baseEntityProcessorDao.getEntityByMetaClass(meta);

        if (entities.size() == 0) {
            System.out.println("No such entities with class: " + name);
        } else {
            for (BaseEntity ids : entities) {
                System.out.println(ids.toString());
            }
        }
    }

    public void commandXSD() throws FileNotFoundException {
        if (args.size() > 0) {
            if (args.get(0).equals("list")) {
                listXSD(args.get(1));
            } else if (args.get(0).equals("convert")) {
                if (args.size() > 2) {
                    processXSD(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <list, convert> <fileName> <className>");
                }
            } else if (args.get(0).equals("generate")) {
                generateXSD();
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <list, convert, generate> <fileName> [className]");
        }
    }

    private void generateXSD() throws FileNotFoundException {
        List<MetaClass> metaClasses = metaClassRepository.getMetaClasses();
        xsdGenerator.generate(System.out, metaClasses);
    }

    public void commandCRBatch() throws IOException, SAXException, XMLStreamException, ParseException {
        if (args.size() > 2) {
            if (args.size() > 3) {
                processCRBatch(args.get(0), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)),
                        new Date(simpleDateFormat.parse(args.get(3)).getTime()));
            } else {
                processCRBatch(args.get(0), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)),
                        new Date((new java.util.Date()).getTime()));
            }
        } else {
            System.out.println("Argument needed: <fileName> <count> <offset>");
        }
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

    public void commandGlobal() {
        if (args.size() > 1) {
            if (args.get(0).equals("add")) {
                GlobalAddCommand globalAddCommand = new GlobalAddCommand();
                globalAddCommand.setEavGlobalDao(eavGlobalDao);
                globalAddCommand.run(args.toArray(new String[args.size()]));
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <add> <type, code, value, desc>");
        }
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
            } else if (args.get(0).equals("tojava")) {
                MetaClass metaClass = metaClassRepository.getMetaClass(args.get(1));
                System.out.println(metaClass.toJava(""));
            } else if (args.get(0).equals("select")) {
                MetaClass metaClass = metaClassRepository.getMetaClass(args.get(1));
                Select select;
                if (args.size() > 2) {
                    select = sqlGenerator.getSimpleSelect(metaClass.getId(), true);
                } else {
                    select = sqlGenerator.getSimpleSelect(metaClass.getId(), false);
                }

                System.out.println(select);
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

    public void processUploadsCommand() {
        String pathToTmp = "E:/tmp/";
        File tmpFolder = new File(pathToTmp);
        File destination = new File("E:/Zips/");

        if (!tmpFolder.isDirectory())
            throw new IllegalArgumentException(pathToTmp + " is not a directory!");

        int counter = 1;

        Map<File, File> map = new HashMap<>();

        for (File creditorFolder : tmpFolder.listFiles()) {
            Long creditorId;

            try {
                creditorId = Long.parseLong(creditorFolder.getName());
            } catch (Exception e) {
                continue;
            }

            File timeFolders[] = creditorFolder.listFiles();
            File sortedTimeFolders[] = new File[timeFolders.length];

            for (int i = 0; i < timeFolders.length; i++) {
                sortedTimeFolders[timeFolders.length - i - 1] = timeFolders[i];
            }

            List<String> names = new LinkedList<>();

            for (File timeFolder : sortedTimeFolders) {
                for (File zipFile : timeFolder.listFiles()) {
                    if (!names.contains(zipFile.getName()) && !zipFile.getName().contains("XML_DATA_BY_CID")) {
                        map.put(timeFolder, zipFile);
                        names.add(zipFile.getName());
                    }
                }
            }
        }

        List<String> existingFiles = new LinkedList<>();
        for (Map.Entry<File, File> entry : map.entrySet()) {
            if (existingFiles.contains(entry.getValue().getName()))
                throw new IllegalStateException(entry.getValue().getName());

            existingFiles.add(entry.getValue().getName());
            System.out.println("update eav_batches set receipt_date = to_date('" + entry.getKey().getName() + "', 'YYYY.MM.DD-HH24:MI:SS') where file_name like '%" + entry.getValue().getName() + "';");
        }

        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        for (Map.Entry<File, File> entry : map.entrySet()) {
            File newFile = new File(destination.getAbsolutePath() + "/" + entry.getValue().getName());
            try {
                FileCopyUtils.copy(entry.getValue(), newFile);
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(newFile.getAbsolutePath() + " copied;");
        }
    }
    public void commandImportJob() throws SQLException {
        Connection conn = null;
        try {
            while (true) {
                try {
                    try {
                        if (conn == null || conn.isClosed())
                            conn = connectToDB("jdbc:oracle:thin:@10.8.1.101:1521:ESSPNEW", "core", "core");
                    } catch (Exception e) {
                        System.out.println("Can't connect to DB: " + e.getMessage());
                        return;
                    }

                    PreparedStatement preparedStatement;
                    PreparedStatement preparedStatementDone;
                    try {
                        preparedStatement = conn.prepareStatement("SELECT conn, login, pass, dir, rep_date from r_import where sent=0");
                        preparedStatementDone = conn.prepareStatement("UPDATE r_import ri \n" +
                                "   SET ri.sent = ? \n" +
                                " WHERE ri.rep_date = ?");
                    } catch (SQLException e) {
                        System.out.println("Can't create prepared statement: " + e.getMessage());
                        try {
                            conn.close();
                        } catch (SQLException e1) {
                        }
                        return;
                    }

                    ResultSet result = preparedStatement.executeQuery();

                    while (result.next()) {
                        String connString = result.getString("conn");
                        String login = result.getString("login");
                        String pass = result.getString("pass");
                        String dir = result.getString("dir");
                        String reportDate = result.getString("rep_date");
                        Connection conn2 = null;
                        PreparedStatement preparedSmt = null;
                        PreparedStatement preparedStmtDone = null;
                        ResultSet result2;
                        try {

                            try {
                                conn2 = connectToDB(connString, login, pass);
                            } catch (ClassNotFoundException e) {
                                System.out.println("Error can't load driver: oracle.jdbc.OracleDriver");
                                return;
                            } catch (SQLException e) {
                                System.out.println("Can't connect to DB: " + e.getMessage());
                                return;
                            }

                            try {
                                preparedSmt = conn2.prepareStatement("SELECT xf.id, xf.file_name, xf.file_content\n" +
                                        "  FROM core.xml_file xf\n" +
                                        " WHERE xf.status = 'COMPLETED'\n" +
                                        "   AND xf.report_date = to_date('" + reportDate + "', 'dd.MM.yyyy')" +
                                        //"   ORDER BY xf.id ASC");
                                        "   AND xf.sent = 0 ORDER BY xf.id ASC");

                                preparedStmtDone = conn2.prepareStatement("UPDATE core.xml_file xf \n" +
                                        "   SET xf.sent = ? \n" +
                                        " WHERE xf.id = ?");
                            } catch (SQLException e) {
                                System.out.println("Can't create prepared statement: " + e.getMessage());
                                try {
                                    conn2.close();
                                } catch (SQLException e1) {
                                    e1.printStackTrace();
                                }
                                return;
                            }

                            File tempDir = new File(dir);

                            if (!tempDir.exists()) {
                                System.out.println("No such directory " + dir);
                                return;
                            }

                            if (!tempDir.isDirectory()) {
                                System.out.println(dir + " must be a directory");
                                return;
                            }
                            int fileNumber = 0;

                            try {
                                result2 = preparedSmt.executeQuery();
                            } catch (SQLException e) {
                                System.out.println("Can't execute db query: " + e.getMessage());
                                break;
                            }
                            int id = 0;
                            while (result2.next()) {
                                fileNumber++;
                                id = result2.getInt("id");
                                String fileName = result2.getString("file_name");
                                Blob blob = result2.getBlob("file_content");

                                File lockFile = new File(tempDir.getAbsolutePath() + "/" + fileName + ".zip.lock");
                                lockFile.createNewFile();

                                File newFile = new File(tempDir.getAbsolutePath() + "/" + fileName + ".zip");
                                newFile.createNewFile();

                                InputStream in = blob.getBinaryStream();

                                byte[] buffer = new byte[1024];

                                FileOutputStream fout = new FileOutputStream(newFile);

                                while (in.read(buffer) > 0) {
                                    fout.write(buffer);
                                }
                                fout.close();

                                lockFile.delete();

                                System.out.println(fileNumber + " - Sending file: " + newFile.getCanonicalFile());

                                preparedStmtDone.setInt(Integer.valueOf(1), 1);
                                preparedStmtDone.setInt(Integer.valueOf(2), id);


                                if (preparedStmtDone.execute()) {
                                    System.out.println("Error can't mark sent file: " + id);
                                }

                                Thread.sleep(5000);
                            }
                            result2.close();
                            preparedStatementDone.setInt(Integer.valueOf(1), 1);
                            preparedStatementDone.setString(Integer.valueOf(2), reportDate);

                            if (preparedStatementDone.execute()) {
                                System.out.println("Error can't mark sent for report_Date : " + reportDate);
                            }
                        } finally {
                            if (conn2 != null)
                                conn2.close();
                        }
                    }

                    result.close();

                    Thread.sleep(5000);
                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        if (conn != null)
                            conn.close();
                    } catch (Exception ex) {
                    }
                }
            }
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    public void commandImport() {
        if (args.size() > 4) {
            Connection conn;

            RmiProxyFactoryBean batchProcessServiceFactoryBean;

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

                ResultSet result2;
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

                        File lockFile = new File(tempDir.getAbsolutePath() + "/" + fileName + ".zip.lock");
                        lockFile.createNewFile();

                        File newFile = new File(tempDir.getAbsolutePath() + "/" + fileName + ".zip");
                        newFile.createNewFile();

                        InputStream in = blob.getBinaryStream();

                        byte[] buffer = new byte[1024];

                        FileOutputStream fout = new FileOutputStream(newFile);

                        while (in.read(buffer) > 0) {
                            fout.write(buffer);
                        }

                        fout.close();

                        lockFile.delete();

                        System.out.println(fileNumber + " - Sending file: " + newFile.getCanonicalFile());

                        // fixme!
                        /*batchProcessService.processBatchWithoutUser(newFile.getAbsolutePath());*/

                        preparedStatementDone.setInt(Integer.valueOf(1), 1);
                        preparedStatementDone.setInt(Integer.valueOf(2), id);

                        if (preparedStatementDone.execute()) {
                            System.out.println("Error can't mark sent file: " + id);
                        }

                        Thread.sleep(30000);
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
                        preparedStatementDone.setInt(Integer.valueOf(1), 1);
                        preparedStatementDone.setInt(Integer.valueOf(2), id);

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

                    int idIndex = nextLine.indexOf("Не реализовано; Entity ID:");

                    if (idIndex > 0) {
                        String idString = nextLine.substring(idIndex +
                                "Не реализовано; Entity ID:".length()).trim();

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
                Map<String, QueryEntry> map = entityServiceCore.getSQLStats();

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

    public void commandCStat() {
        if(args.size() > 0) {
            RmiProxyFactoryBean entityServiceFactoryBean = null;

            kz.bsbnb.usci.core.service.IEntityService entityService = null;

            try {
                entityServiceFactoryBean = new RmiProxyFactoryBean();
                entityServiceFactoryBean.setServiceUrl(args.get(0));
                entityServiceFactoryBean.setServiceInterface(kz.bsbnb.usci.core.service.IEntityService.class);
                entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

                entityServiceFactoryBean.afterPropertiesSet();
                entityService = (kz.bsbnb.usci.core.service.IEntityService) entityServiceFactoryBean.getObject();

                System.out.println(entityService.getStatus());
            } catch (Exception e) {
                System.out.println("Can't connect to core service: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Argument needed: <core_url>");
            System.out.println("Example: cstat rmi://127.0.0.1:1099/entityService");
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

            Map<String, QueryEntry> map = entityService.getSQLStats();

            System.out.println();
            System.out.println("|  count  |     avg (ms)     |       total (ms)       |");

            double totalInserts = 0;
            double totalSelects = 0;
            double totalProcess = 0;
            int totalProcessCount = 0;

            for (String s : map.keySet()) {
                QueryEntry queryEntry = map.get(s);
                queryEntry.query = s;
            }

            List<QueryEntry> values = new LinkedList(map.values());
            Collections.sort(values, new Comparator<QueryEntry>() {
                @Override
                public int compare(QueryEntry o1, QueryEntry o2) {
                    return o1.totalTime > o2.totalTime ? -1 : 1;
                }
            });

            for (QueryEntry qe : values) {
                String query = qe.query;
                System.out.printf("| %7d | %16d | %22d | %s%n", qe.count, (qe.totalTime / qe.count), qe.totalTime, query);

                if (query.startsWith("insert"))
                    totalInserts += qe.totalTime;

                if (query.startsWith("select"))
                    totalSelects += qe.totalTime;

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
        } else {
            System.out.println("Argument needed: <core_url>");
            System.out.println("Example: sqlstat rmi://127.0.0.1:1099/entityService");
            System.out.println("Example: sqlstat rmi://127.0.0.1:1097/batchProcessService");
        }
    }

    public void commandEntity() {
        if (args.size() > 1) {
            if (args.get(0).equals("show")) {
                if (args.get(1).equals("id")) {
                        showEntity(Long.parseLong(args.get(2)), args.size() > 3 ? args.get(3) : null);
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
                if (args.size() > 1) {
                    removeEntityById(Long.parseLong(args.get(1)));
                } else {
                    System.out.println("Argument needed: <rm> <id> <service_url>");
                    System.out.println("Example: rm 100 rmi://127.0.0.1:1099/batchEntryService");
                }
            } else if (args.get(0).equals("read")) {
                if (args.size() > 2) {
                    long creditorId = 0L;
                    if (args.size() == 4)
                        creditorId = Long.parseLong(args.get(3));
                    readEntityFromXML(args.get(1), args.get(2), creditorId);
                } else {
                    System.out.println("Argument needed: <read> <fileName> <rep_date> {creditorId}");
                }
            } else if (args.get(0).equals("find")) {
                if (args.size() > 2) {
                    findEntityFromXML(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <find> <fileName> <rep_date>");
                }
            } else if (args.get(0).equals("sql")) {
                if (args.size() == 2) {
                    dumpEntityToSQL(args.get(1), null); }
                else if(args.size() == 3){
                    dumpEntityToSQL(args.get(1), args.get(2));
                } else {
                    System.out.println("Argument needed: <sql> <id1,id2...> [filename]");
                }
            } else {
                System.out.println("No such operation: " + args.get(0));
            }
        } else {
            System.out.println("Argument needed: <show, read> <id, attr, sq, inter> <id> [attributePath, id2]");
        }
    }

    private void dumpEntityToSQL(String idsString, String fileName) {
        entityExporter.setFile(fileName);
        String[] ids = idsString.split(",");
        List<Long> longList = new ArrayList<>();
        for(int i=0;i<ids.length;i++)
            longList.add(Long.parseLong(ids[i]));

        entityExporter.export(longList);
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

    public void commandSql() throws FileNotFoundException, SQLException {
        StringBuilder str = new StringBuilder();
        if(args.get(0).equals("run")){
            System.out.println("Запускаю скрипт " + args.get(1));
            long t1 = System.currentTimeMillis();
            SqlRunner runner = new SqlRunner(storage.getConnection(),  true);
            runner.runScript(args.get(1), StaticRouter.getCoreSchemaName());
            System.out.println("Скрипт отработан за " + Math.round(((System.currentTimeMillis() - t1) / 1000)) + " сек.");
        } else {
            for (Object o : args) str.append(o).append(" ");
            boolean res = storage.simpleSql(str.toString());
            if (!res) System.out.println("Скрипт не отработан: " + str);
        }
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
        } else throw new IllegalArgumentException(Errors.compose(Errors.E212));
    }

    public void init() {
        try {
            RmiProxyFactoryBean entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
            entityServiceFactoryBean.setServiceInterface(IBaseEntityProcessorDao.class);

            entityServiceFactoryBean.afterPropertiesSet();

            RmiProxyFactoryBean ruleBatchServiceFactoryBean = new RmiProxyFactoryBean();
            ruleBatchServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/packageService");
            ruleBatchServiceFactoryBean.setServiceInterface(IPackageService.class);

            ruleBatchServiceFactoryBean.afterPropertiesSet();
            packageService = (IPackageService) ruleBatchServiceFactoryBean.getObject();

            initBatchService();

            /*RmiProxyFactoryBean batchVersionServiceFactoryBean = new RmiProxyFactoryBean();
            batchVersionServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchVersionService");
            batchVersionServiceFactoryBean.setServiceInterface(IBatchVersionService.class);

            batchVersionServiceFactoryBean.afterPropertiesSet();
            batchVersionService = (IBatchVersionService) batchVersionServiceFactoryBean.getObject();*/

            RmiProxyFactoryBean ruleServiceFactoryBean = new RmiProxyFactoryBean();
            ruleServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/ruleService");
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
                currentDate = RulesSingleton.ruleDateFormat.parse("01_04_2001");
                currentRulePackage = ruleService.getPackage(currentPackageName);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage() + ", если скрипт выполняется первый раз - игнор");
            }
        }

        try {
            if (args.get(0).equals("debug")) {

                DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
                Date date = dateFormatter.parse("05.04.2015");

                try {
                    // checkme!
                    long creditorId = 0;
                    CLIXMLReader reader = new CLIXMLReader("c:/a.xml", metaClassRepository, batchService, date, creditorId);
                    BaseEntity baseEntity = reader.read();
                    //System.out.println(ma);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (args.get(0).equals("dump")) {
                String defaultDumpFile = "c:/rule_dumps/23_06_2015.cli";
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
                        throw new IllegalArgumentException(Errors.compose(Errors.E213));
                    String title = line.split("title: ")[1];
                    line = in.nextLine();
                    if (line.startsWith(args.get(1)))
                        throw new IllegalArgumentException(Errors.compose(Errors.E214));
                    sb.append(line);
                    do {
                        line = in.nextLine();
                        if (line.startsWith(args.get(1))) break;
                        sb.append("\n" + line);
                    } while (true);
                    currentRule = new Rule();
                    currentRule.setOpenDate(currentDate);
                    currentRule.setRule(sb.toString());
                    currentRule.setTitle(title);
                }
            } else if (args.get(0).equals("current")) {
                if (args.size() == 1)
                    System.out.println(currentRule == null ? null : currentRule.getRule());
                else if (args.get(1).equals("version"))
                    System.out.println(currentPackageVersion);
                else if (args.get(1).equals("package"))
                    System.out.println(currentRulePackage);
                else if (args.get(1).equals("date"))
                    System.out.println(currentDate);
                else if (args.get(1).equals("entity"))
                    System.out.println(currentBaseEntity);
                else throw new IllegalArgumentException();
            } else if (args.get(0).equals("save")) {
                long ruleId = ruleService.createNewRuleInPackage(currentRule, currentPackageVersion);
                System.out.println("ok saved: ruleId = " + ruleId);
            } else if (args.get(0).equals("run")) {

                String fileName = "/home/bauka/a.xml";
                DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
                Date reportDate = dateFormatter.parse("01.08.2016");

                Batch b = new Batch();
                b.setRepDate(reportDate);
                Long creditorId = -1L;
                boolean packageTagFound = false;

                try {
                    byte[] bytes;

                    int size;
                    byte[] buffer = new byte[1024];
                    InputStream is = new FileInputStream(fileName);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    while ((size = is.read(buffer, 0, buffer.length)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, size);
                        if(new String(buffer).contains("<package"))
                            packageTagFound = true;
                    }
                    bytes = byteArrayOutputStream.toByteArray();
                    RmiProxyFactoryBean batchProcessServiceFactoryBean;
                    IBatchProcessService batchProcessService;

                    try {
                        batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
                        batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchProcessService");
                        batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
                        batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);
                        batchProcessServiceFactoryBean.afterPropertiesSet();
                        batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
                        creditorId = batchProcessService.parseCreditorId(bytes);
                    } catch(Exception e) {
                        //batchProcessService is down
                    }
                    //System.out.println(creditorId);

                    //credit-registry
                    if(creditorId != -1) {
                        //check for schema
                        Source xml = new StreamSource(new ByteArrayInputStream(bytes));
                        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        URL schemaURL = getClass().getClassLoader().getResource("credit-registry.xsd");
                        Schema schema = schemaFactory.newSchema(schemaURL);
                        Validator validator = schema.newValidator();
                        validator.validate(xml);

                        crParser.setCreditorId(creditorId);
                        crParser.parse(new ByteArrayInputStream(bytes), b);
                        currentBaseEntity = crParser.getCurrentBaseEntity();
                        is.close();
                    } else {
                        if(packageTagFound)
                            throw new RuntimeException("cr file found, did you run receiver ?");

                        CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchService,
                                reportDate, creditorId);
                        currentBaseEntity = reader.read();
                        reader.close();
                    }

                    currentBaseEntity = (BaseEntity) baseEntityProcessorDao.prepare(currentBaseEntity, creditorId);

                    if(currentBaseEntity.getEl("creditor") != null && currentBaseEntity.getBaseEntityReportDate().getCreditorId() < 1) {
                        //careful: creditorId is auto injected
                        creditorId = ((BaseEntity) currentBaseEntity.getEl("creditor")).getId();
                        currentBaseEntity.getBaseEntityReportDate().setCreditorId(creditorId);
                    }

                    List<String> errors = ruleService.runRules(currentBaseEntity, currentPackageName, currentDate);
                    for (String s : errors)
                    System.out.println("Validation error:" + s);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (args.get(0).equals("set")) {

                /*if (args.get(1).equals("version")) {
                    currentBatchVersion = batchVersionService.getBatchVersion(currentPackageName, currentDate);
                } else */if (args.size() < 3) throw new IllegalArgumentException();
                else if (args.get(1).equals("package")) {
                    currentRulePackage = ruleService.getPackage(args.get(2));
                    currentPackageVersion = new PackageVersion(currentRulePackage, currentDate);
                    //ruleService.getRulePackageName(args.get(2), currentDate);
                    currentPackageName = args.get(2);
                } else if (args.get(1).equals("date")) {
                    DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    currentDate = formatter.parse(args.get(2));
                    currentPackageVersion = new PackageVersion(currentRulePackage, currentDate);
                } else throw new IllegalArgumentException();
            } else if (args.get(0).equals("create")) {
                try {
                    if (!args.get(1).equals("package") || args.size() < 3) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    throw e;
                }
                RulePackage batch = new RulePackage(args.get(2));

                boolean exists = false;
                Long id;
                for (RulePackage ruleBatch : packageService.getAllPackages()) {
                    if (ruleBatch.getName().equals(batch.getName())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    id = packageService.save(batch);
                    batch.setId(id);
                    //batchVersionService.save(batch);
                    System.out.println("ok package created with id:" + id);
                }

            } else if (args.get(0).equals("rc")) {
                ruleService.reloadCache();
            } else if (args.get(0).equals("eval")) {
                System.out.println(currentBaseEntity.getEls(args.get(1)));
            } else if (args.get(0).equals("eval2")) {
                System.out.println(currentBaseEntity.getEl(args.get(1)));
            } else if (args.get(0).equals("clear")) {
                ruleService.clearAllRules();
            } else if (args.get(0).equals("import")) {
                try {
                    Scanner importedPath = new Scanner(new File(args.get(1)));

                    while (importedPath.hasNext()) {
                        String line1 = importedPath.nextLine();
                        if (line1.equals("") || line1.startsWith("#"))
                            continue;
                        StringTokenizer st = new StringTokenizer(line1);
                        command = st.nextToken().trim();
                        if (command.equals("quit")) break;
                        if (st.hasMoreTokens()) {
                            args.clear();
                            while (st.hasMoreTokens()) {
                                args.add(st.nextToken().trim());
                            }
                        }
                        commandRule(importedPath);
                    }

                    importedPath.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else throw new IllegalArgumentException();
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

        Map<String, QueryEntry> map = showcaseService.getSQLStats();

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

            System.out.printf("| %7d | %16d | %22d | %s%n", qe.count, (qe.totalTime / qe.count), qe.totalTime, query);

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

    public void commandShowCase() throws SQLException {
        if (showcaseServiceFactoryBean == null || showcaseService == null)
            initSC();

        if (args.get(0).equals("status")) {
            System.out.println(showCase.toString());

            for (ShowCaseField sf : showCase.getFieldsList())
                System.out.println(sf.getAttributePath() + ", " + sf.getColumnName());

            for (ShowCaseField sf : showCase.getCustomFieldsList())
                System.out.println("* " + sf.getAttributePath() + ", " + sf.getColumnName());
        } else if (args.get(0).equals("set")) {
            if (args.size() != 3)
                throw new IllegalArgumentException(Errors.compose(Errors.E215));
            if (args.get(1).equals("meta")) {
                showCase = new ShowCase();
                showCase.setMeta(metaClassRepository.getMetaClass(args.get(2)));
            } else if (args.get(1).equals("name")) {
                showCase.setName(args.get(2));
            } else if (args.get(1).equals("tableName")) {
                showCase.setTableName(args.get(2));
            } else if (args.get(1).equals("downPath")) {
                MetaClass metaClass = showCase.getMeta();
                if (metaClass.getEl(args.get(2)) == null)
                    throw new IllegalArgumentException(Errors.compose(Errors.E215, args.get(2)));

                showCase.setDownPath(args.get(2));
            } else if (args.get(1).equals("final")) {
                showCase.setFinal(Boolean.parseBoolean(args.get(2)));
            } else
                throw new IllegalArgumentException(Errors.compose(Errors.E215));

        } else if (args.get(0).equals("list")) {
            if (args.get(1).equals("reset")) {
                showCase.getFieldsList().clear();
            } else if (args.get(1).equals("add")) {
                if (args.get(2) == null || args.get(3) == null)
                    throw new UnsupportedOperationException(Errors.compose(Errors.E217));

                showCase.addField(args.get(2), args.get(3));
            } else if (args.get(1).equals("addCustom")) {
                if (args.get(2) == null || args.get(3) == null || args.get(4) == null)
                    throw new UnsupportedOperationException(Errors.compose(Errors.E218));

                showCase.addCustomField(args.get(3), args.get(4), metaClassRepository.getMetaClass(args.get(2)));
            } else if (args.get(1).equals("addRootKey")) {
                if (args.size() == 4)
                    showCase.addRootKeyField(args.get(2), args.get(3));
                else
                    showCase.addRootKeyField(args.get(2),args.get(2));
            } else if (args.get(1).equals("addHistoryKey")) {
                showCase.addHistoryKeyField(args.get(2), args.get(3));
            } else {
                System.err.println("Example: showcase list add [path] [columnName]");
                System.err.println("Example: showcase list addCustom metaClass [path] [columnName]");
                throw new IllegalArgumentException();
            }
        } else if (args.get(0).equals("child")) {
            if (args.get(1).equals("init")) {
                childShowCase = new ShowCase();
                childShowCase.setChild(true);
            } else if (args.get(1).equals("set")) {
                if (args.get(2).equals("name")) {
                    childShowCase.setName(args.get(3));
                } else if (args.get(2).equals("meta")) {
                    childShowCase.setMeta(metaClassRepository.getMetaClass(args.get(3)));
                } else if (args.get(2).equals("downPath")) {
                    childShowCase.setDownPath(args.get(3));
                } else if (args.get(2).equals("tableName")) {
                    childShowCase.setTableName(args.get(3));
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (args.get(1).equals("list")) {
                if (args.get(2).equals("add")) {
                    childShowCase.addField(args.get(3), args.get(4));
                } else if(args.get(2).equals("addRootKey")) {
                    childShowCase.addRootKeyField(args.get(3), args.get(4));
                } else if(args.get(2).equals("addHistoryKey")) {
                    childShowCase.addHistoryKeyField(args.get(3), args.get(4));
                } else {
                    throw new IllegalArgumentException();
                }
            } else if(args.get(1).equals("save")) {
                showCase.addChildShowCase(childShowCase);
                childShowCase = new ShowCase();
            } else {
                throw new IllegalArgumentException();
            }
        } else if (args.get(0).equals("addIndex")) {
            if (args.get(1).equals("unique") || args.get(1).equals("nonunique")) {
                String IndexType = args.get(1);
                args.remove(0);
                args.remove(0);
                ShowCaseIndex index = new ShowCaseIndex(IndexType, showCase.getTableName(), args);
                showCase.addIndex(index.getIndex());
            } else {
                System.err.println("Example: addIndex [unique/nonunique] [columName1] .. [columNameN]");
            }


        } else if (args.get(0).equals("save")) {
            long scId = showcaseService.add(showCase);
            if (scId > 0)
                System.out.println(showCase.getName() + ": Showcase successfully added!");
            else
                System.err.println("Couldn't save " + showCase.getName());
            showCase = null;
        } else if (args.get(0).equals("listSC")) {
            List<ShowCase> list = showcaseService.list();
            for (ShowCase showCase : list) {
                System.out.println(showCase.getName());

                for (ShowCaseField field : showCase.getFieldsList())
                    System.out.println("\t" + field.getColumnName());

                for (ShowCaseField field : showCase.getCustomFieldsList())
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
        } else if (args.get(0).equals("rc")) {
            showcaseService.reloadCash();
        } else if (args.get(0).equals("stats")) {
            showcaseStat();
        } else if(args.get(0).equals("sql")) {
            if (args.get(1).equals("run")) {
                System.out.println("Запускаю скрипт " + args.get(2));
                long t1 = System.currentTimeMillis();
                InitDataSourceSC(showcaseService.getDriverSc(), showcaseService.getSchemaSc(), showcaseService.getPasswordSc(), showcaseService.getUrlSc());
                SqlRunner runner = new SqlRunner(jdbcTemplateSC.getDataSource().getConnection(), true);
                runner.runScript(args.get(2), StaticRouter.getShowcaseSchemaName());
                System.out.println("Скрипт отработан за " + ((System.currentTimeMillis() - t1) / 1000) + " сек.");
            }
        } else if (args.get(0).equals("oper_sc")) {
            Connection conn = null;
            try {
                while (true) {
                    try {
                        try {
                            if (conn == null || conn.isClosed())
                                conn = connectToDB("jdbc:oracle:thin:@10.8.1.200:1521:ESSP", "CORE", "core");
                        } catch (Exception e) {
                            System.out.println("Can't connect to DB: " + e.getMessage());
                            return;
                        }

                        PreparedStatement preparedStatement;
                        try {
                            preparedStatement = conn.prepareStatement("SELECT entity_id, report_date from (SELECT entity_id, report_date FROM oper_sc order by report_date, entity_id) where rownum < 256");
                        } catch (SQLException e) {
                            System.out.println("Can't create prepared statement: " + e.getMessage());
                            try {
                                conn.close();
                            } catch (SQLException e1) {
                            }
                            return;
                        }

                        ResultSet result = preparedStatement.executeQuery();

                        while (result.next()) {
                            Long entityId = result.getLong("entity_id");
                            Date reportDate = result.getDate("report_date");

                            IBaseEntity loadedEntity = baseEntityLoadDao.loadByMaxReportDate(entityId, reportDate);

                            applyListener.applyToDBEnded(loadedEntity);

                            preparedStatement = conn.prepareStatement("DELETE FROM oper_sc where entity_id = ? and report_date = ?");
                            preparedStatement.setLong(1, entityId);
                            preparedStatement.setDate(2, DataUtils.convertToSQLDate(DataUtils.convertToTimestamp(reportDate)));

                            preparedStatement.executeUpdate();

                            System.out.println(entityId + " : " + reportDate);
                        }

                        result.close();

                        Thread.sleep(5000);
                    } catch (Exception e){
                        try {
                            if (conn != null)
                                conn.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            } finally {
                if (conn != null)
                    conn.close();
            }
        } else {
            throw new IllegalArgumentException(Errors.compose(Errors.E219));
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
            while (st.hasMoreTokens())
                args.add(st.nextToken().trim());
        } else {
            return;
        }

        if (command.startsWith("#"))
            return;

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
            } else if (command.equals("global")) {
                commandGlobal();
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
            } else if (command.equals("importJob")) {
                commandImportJob();
            } else if (command.equals("collectids")) {
                commandCollectIds();
            } else if (command.equals("rstat")) {
                commandRStat();
            } else if (command.equals("sstat")) {
                commandSStat();
            } else if (command.equals("cstat")) {
                commandCStat();
            }else if (command.equals("sqlstat")) {
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
            } else if (command.equals("showcase")) {
                commandShowCase();
            } else if (command.equals("merge")) {
                mergeEntity();
            } else if (command.equals("getbatch")) {
                commandGetBatch();
            } else if (command.equals("mnt")) {
                commandMaintenance(line);
            } else if (command.equals("cp")) {
                commandCp();
            } else if (command.equals("processUploads")) {
                processUploadsCommand();
            } else {
                System.out.println("No such command: " + command);
            }
        } catch (Exception e) {
            System.err.println(Errors.decompose(e.getMessage()));
            lastException = e;
        }
    }
    private void commandCp() {

        final String usage = "cp sourceFolder [targetFolder]";

        if (args.size() < 1) {
            System.out.println(usage);
            return;
        }

        String sourceFolder = args.get(0);
        String targetFolder = "E:\\Zips";

        if (args.size() > 1)
            targetFolder = args.get(1);

        File[] files = new File(sourceFolder).listFiles();
        Arrays.sort(files);
        for (File source : files) {
            if (source.isDirectory())
                continue;

            File dest = new File(targetFolder+"/"+source.getName());

            try{

                File lockFile =  new File(dest.getAbsolutePath()+".lock");
                lockFile.createNewFile();

                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new FileInputStream(source);
                    os = new FileOutputStream(dest);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    is.close();
                    os.close();
                }

                lockFile.delete();

            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        System.out.println("Done " + files.length + " files copied.");
    }

    private void commandMaintenance(String line) {
        if (mnt == null)
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

            Batch batch = batchService.getBatch(batchId);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(batch.getContent());

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
                Date reportDate = simpleDateFormat.parse(repDate);
                // checkme!
                long creditorId = 0L;
                CLIXMLReader reader = new CLIXMLReader(fileName, metaClassRepository, batchService, reportDate, creditorId);
                BaseEntity entityToWrite;
                IBaseEntity savedEntity;
                while ((entityToWrite = reader.read()) != null) {
                    try {
                        savedEntity = baseEntityProcessorDao.process(entityToWrite);
                        entityList.add(savedEntity);
                        System.out.println("Запись сохранилась с ИД: " + savedEntity.getId());
                    } catch (Exception ex) {
                        lastException = ex;
                        System.out.println("Ошибка: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("File " + fileName + " not found, with error: " + e.getMessage());
            } catch (ParseException e) {
                System.out.println("Can't parse date " + repDate + " must be in format " + simpleDateFormat.toString());
            }
            IBaseEntityMergeManager mergeManager = constructMergeManagerFromJson(mergeManagerFileName);

            System.out.println("Merging two base entities " + entityList.get(0).getId() + "  "
                    + entityList.get(1).getId());
            System.out.println(">>>>>>>>>>>>>>LEFT ENTITY<<<<<<<<<<<<<<<<<<<<<");
            System.out.println(entityList.get(0));
            System.out.println("\n >>>>>>>>>>>>>>RIGHT ENTITY<<<<<<<<<<<<<<<<<<<<<");
            System.out.println(entityList.get(1));
            System.out.println("\n >>>>>>>>>>>>>>>RESULT!!!<<<<<<<<<<<<<<<<<<< ");
            IBaseEntity result = baseEntityMergeDao.merge(entityList.get(0), entityList.get(1), mergeManager,
                    IBaseEntityMergeDao.MergeResultChoice.LEFT, true);

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
        boolean intersects(DispatcherJob job);

        boolean isAlive();

        void start();

        void prepare();
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
}

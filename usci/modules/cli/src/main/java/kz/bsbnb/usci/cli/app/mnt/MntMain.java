package kz.bsbnb.usci.cli.app.mnt;

import kz.bsbnb.usci.cli.app.common.ICommonUtils;
import kz.bsbnb.usci.cli.app.common.ICreditUtils;
import kz.bsbnb.usci.cli.app.exceptions.CreditorNotFoundException;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.impl.BaseEntityXmlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Bauyrzhan.Makhambeto on 17/03/2015.
 */
@Component
public class MntMain {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    ICommonUtils commonUtils;

    @Autowired
    ICreditUtils creditUtils;

    @Autowired
    IStorage storage;

    MntThread mntThread;

    @Autowired
    ApplicationContext context;

    public void commandMaintenance(String line) {

        String commandUsage = "Arguments: mnt [delScript|keyScript] from core:core_sep_2014@10.10.11.44:CREDITS [creditor_id=36] [--all] > C:\\zips\\batch.zip";

        if(line.contains("changeScript")) {
            commandMaintenanceChange(line);
            return;
        }

        boolean creditorFlag = false;
        boolean allFlag = false;

        long creditorFilterId = -1;
        String path = "";
        String script = "";
        Matcher m;
        String pattern = "mnt\\s+(delScript|keyScript) from (\\S+):(\\S+)@(\\S+):(\\S+)";

        if (line.contains("creditor_id")) {
            pattern = pattern + "\\s+creditor_id=(\\d+)";
            creditorFlag = true;
        }

        if (line.contains("--all")) {
            pattern = pattern + "\\s+--all";
            allFlag = true;
        }

        pattern = pattern + "\\s+>\\s+(\\S+)\\.zip";

        m = Pattern.compile(pattern).matcher(line);
        int gid = 1;
        List<Long> listSuccess = new ArrayList<Long>();
        Map<Long, ArrayList<BaseEntity>> byCreditor = new HashMap<Long, ArrayList<BaseEntity>>();

        if (m.find()) {
            script = m.group(gid++);
            String user = m.group(gid++);
            String pwd = m.group(gid++);
            String address = m.group(gid++);
            String sid = m.group(gid++);

            if (creditorFlag)
                creditorFilterId = Long.valueOf(m.group(gid++));

            path = m.group(gid++);

            Connection conn = null;

            try {
                conn = commonUtils.connectToDB(String.format("jdbc:oracle:thin:@%s:1521:%s", address, sid), user, pwd);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    conn.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                return;
            }

            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;

            if (script.equals("delScript")) {

                String select = "select t.ID, root.CREDITOR_ID, t.CONTRACT_NO, t.CONTRACT_DATE" +
                        "  from MAINTENANCE.CREDREG_DELETE_CREDIT t ," +
                        "       MAINTENANCE.CREDREG_EDIT root" +
                        " where root.ID = t.EDIT_ID ";

                if (!allFlag)
                    select += "   and t.PROCESSED_USCI = 0";


                if (creditorFlag)
                    select += " and root.CREDITOR_ID = " + creditorFilterId;

                try {
                    preparedStatement = conn.prepareStatement(select);
                    resultSet = preparedStatement.executeQuery();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }


                try {
                    while (resultSet.next()) {
                        String contractNo = resultSet.getString("contract_no");
                        Date contractDate = resultSet.getDate("contract_date");
                        Long creditorId = resultSet.getLong("creditor_id");
                        Long mntId = resultSet.getLong("ID");

                        IBaseEntity credit = null;
                        try {
                            credit = creditUtils.getCreditHull(conn, creditorId, contractNo, contractDate);
                        } catch (CreditorNotFoundException e) {
                            storage.simpleSql(String.format("insert into mnt_logs" +
                                    "(mnt_operation_id, foreign_id, execution_time, status, error_msg) " +
                                    "  values (1,%d,sysdate,1,'creditor_not_found')", mntId));
                            System.out.println("creditor not found with id " + creditorId);
                            continue;
                        }

                        baseEntityProcessorDao.prepare(credit);

                        if (credit.getId() <= 0) {
                            //storage.simpleSql(String.format("insert into MNT_LOGS (MNT_OPERATION_ID, FOREIGN_ID, EXECUTION_TIME, status, error_msg) values (1,%d,sysdate,1,'%s')", mntId, "credit not found"));
                            System.out.println(String.format("credit not found contract_no = %s " +
                                    "contract_date = %s creditor_id = %d", contractNo, contractDate, creditorId));
                            continue;
                        } else {
                            ((BaseEntity) credit).setOperation(OperationType.DELETE);
                            listSuccess.add(mntId);
                            //storage.simpleSql(String.format("insert into MNT_LOGS (MNT_OPERATION_ID, FOREIGN_ID, EXECUTION_TIME, STATUS) values (%d,1,sysdate,0)", mntId));
                        }

                        if (!byCreditor.containsKey(creditorId)) {
                            byCreditor.put(creditorId, new ArrayList<BaseEntity>());
                        }

                        byCreditor.get(creditorId).add((BaseEntity) credit);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else if (script.equals("keyScript")) {

                String select = "select t.ID, root.CREDITOR_ID, t.CONTRACT_NO, t.CONTRACT_DATE, t.CONTRACT_NO_OLD, t.CONTRACT_DATE_OLD" +
                        "  from MAINTENANCE.CREDREG_edit_credit t ," +
                        "       MAINTENANCE.CREDREG_EDIT root" +
                        " where root.ID = t.EDIT_ID " +
                        "  and (t.CONTRACT_NO_OLD is not null or t.CONTRACT_DATE_OLD is not null)";

                if (!allFlag)
                    select += "   and t.PROCESSED_USCI = 0";

                if (creditorFlag)
                    select += " and root.CREDITOR_ID = " + creditorFilterId;

                try {
                    preparedStatement = conn.prepareStatement(select);
                    resultSet = preparedStatement.executeQuery();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                Batch batch = new Batch(new Date());
                batch.setId(777);

                try {
                    while (resultSet.next()) {
                        String contractNo = resultSet.getString("contract_no_old");
                        Date contractDate = resultSet.getDate("contract_date_old");
                        String contractNoNew = resultSet.getString("contract_no");
                        Date contractDateNew = resultSet.getDate("contract_date");
                        Long creditorId = resultSet.getLong("creditor_id");
                        Long mntId = resultSet.getLong("ID");

                        IBaseEntity credit = new BaseEntity(metaClassRepository.getMetaClass("credit"),
                                new Date());

                        IBaseEntity primaryContract = new BaseEntity(metaClassRepository.getMetaClass("primary_contract"),
                                new Date());

                        IBaseValue contractNoValue = BaseValueFactory.create(BaseContainerType.BASE_ENTITY, primaryContract.getMemberType("no"), batch, 0, contractNo);
                        if (!contractNo.equals(contractNoNew))
                            contractNoValue.setNewBaseValue(BaseValueFactory.create(BaseContainerType.BASE_ENTITY, primaryContract.getMemberType("no"), batch, 0, contractNoNew));

                        IBaseValue contractDateValue = BaseValueFactory.create(BaseContainerType.BASE_ENTITY, primaryContract.getMemberType("date"), batch, 0, contractDate);
                        if (!contractDate.equals(contractDateNew))
                            contractDateValue.setNewBaseValue(BaseValueFactory.create(BaseContainerType.BASE_ENTITY, primaryContract.getMemberType("date"), batch, 0, contractDateNew));

                        primaryContract.put("no", contractNoValue);
                        primaryContract.put("date", contractDateValue);


                        IBaseEntity creditor = creditUtils.getCreditor(conn, creditorId);

                        if (creditor == null) {
                            //storage.simpleSql(String.format("insert into mnt_logs(mnt_operation_id, foreign_id, execution_time, status, error_msg) values (1,%d,sysdate,1,'creditor_not_found')",mntId));
                            System.out.println("creditor not found with id " + creditorId);
                            continue;
                        }

                        credit.put("primary_contract", new BaseValue(batch, 0, primaryContract));
                        credit.put("creditor", new BaseValue(batch, 0, creditor));

                        baseEntityProcessorDao.prepare(credit);

                        if (credit.getId() <= 0) {
                            //storage.simpleSql(String.format("insert into MNT_LOGS (MNT_OPERATION_ID, FOREIGN_ID, EXECUTION_TIME, status, error_msg) values (1,%d,sysdate,1,'%s')", mntId, "credit not found"));
                            System.out.println(String.format("credit not found contract_no = %s " +
                                    "contract_date = %s creditor_id=%d", contractNo, contractDate, creditorId));
                            continue;
                        } else {
                            listSuccess.add(mntId);
                            //storage.simpleSql(String.format("insert into MNT_LOGS (MNT_OPERATION_ID, FOREIGN_ID, EXECUTION_TIME, STATUS) values (%d,1,sysdate,0)", mntId));
                        }

                        if (!byCreditor.containsKey(creditorId)) {
                            byCreditor.put(creditorId, new ArrayList<BaseEntity>());
                        }

                        byCreditor.get(creditorId).add((BaseEntity) credit);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            for (long creditorId : byCreditor.keySet()) {
                ArrayList<BaseEntity> entities = byCreditor.get(creditorId);
                //IBaseEntity creditor = getCreditor(conn, creditorId);
                Creditor crCreditor = creditUtils.getCrCreditor(conn, creditorId);
                BaseEntityXmlGenerator baseEntityXmlGenerator = new BaseEntityXmlGenerator();
                Document document;
                if (script.equals("delScript"))
                    document = baseEntityXmlGenerator.getGeneratedDeleteDocument(entities);
                else
                    document = baseEntityXmlGenerator.getGeneratedDocument(entities);

                String creditorLabel = path + "_";
                if (crCreditor.getBIK() == null)
                    creditorLabel += crCreditor.getBIN();
                else
                    creditorLabel += crCreditor.getBIK();

                creditorLabel += ".zip";

                baseEntityXmlGenerator.writeToZip(new Document[] {
                                        document,
                                        creditUtils.getManifest(crCreditor, entities.size())}
                        , new String[]{"data.xml", "manifest.xml"}, creditorLabel);
            }

            MntUtils.updateMntStatus(conn, script, listSuccess);

        } else {
            System.out.println(commandUsage);
        }
    }

    public void commandMaintenanceChange(String line) {
        String commandUsage = "Arguments: mnt changeScript [stop| pause | resume | from core:core_sep_2014@10.10.20.44:CREDITS [creditor_id=31] [report_date=11.11.15] [edit_id=1] [limit=100] > C:/zips/]";

        Matcher m;

        if(!line.contains("from") ) {
            m = Pattern.compile("mnt changeScript (stop|pause|resume)").matcher(line);
            if(m.find()) {
                if(mntThread == null) {
                    System.out.println("Thread not started");
                    return;
                }

                String command = m.group(1);
                System.out.println("command" + command);

                if(command.equals("stop")) {
                    mntThread.interrupt();
                } else if(command.equals("pause")) {
                    mntThread.pause();
                } else if(command.equals("resume")) {
                    mntThread.unpause();
                    synchronized (mntThread){
                        mntThread.notify();
                    }
                }
            } else {
                System.out.println(commandUsage);
                return;
            }
        } else {
            Connection conn= null;
            String pattern = "mnt changeScript from (\\S+):(\\S+)@(\\S+):(\\S+)";
            long creditorId = -1;
            Date reportDate = null;
            long editId = -1;
            int limit = -1;
            boolean creditorFlag = false;
            boolean editIdFlag = false;
            boolean limitFlag = false;
            boolean reportDateFlag = false;
            String path;

            if(line.contains("creditor_id")) {
                pattern += "\\s+creditor_id=(\\d+)";
                creditorFlag = true;
            }

            if(line.contains("report_date")) {
                pattern += "\\s+report_date=(\\S+)";
                reportDateFlag = true;
            }

            if(line.contains("edit_id")) {
                pattern += "\\s+edit_id=(\\d+)";
                editIdFlag = true;
            }

            if(line.contains("limit")) {
                pattern += "\\s+limit=(\\d+)";
                limitFlag = true;
            }

            pattern += "\\s+>\\s+(\\S+)";

            m = Pattern.compile(pattern).matcher(line);
            int gid = 1;

            if(m.find()) {
                String user = m.group(gid++);
                String pwd = m.group(gid++);
                String address = m.group(gid++);
                String sid = m.group(gid++);

                if (creditorFlag) {
                    creditorId = Long.valueOf(m.group(gid++));
                }

                if(reportDateFlag) {
                    try {
                        reportDate = new SimpleDateFormat("dd.MM.yyyy").parse(m.group(gid++));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        System.out.println(commandUsage);
                        return;
                    }
                }


                if (editIdFlag) {
                    editId = Long.valueOf(m.group(gid++));
                }
                if (limitFlag) {
                    limit = Integer.valueOf(m.group(gid++));
                }
                path = m.group(gid++);


                try {
                    conn = commonUtils.connectToDB(String.format("jdbc:oracle:thin:@%s:1521:%s", address, sid), user, pwd);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                File tempDir = new File(path);

                if(!tempDir.isDirectory())
                    throw new RuntimeException("must be directory");

                mntThread = new MntThread(conn, creditorId, reportDate, editId, limit,  path);
                mntThread.setBaseEntityProcessorDao(baseEntityProcessorDao);
                mntThread.setCreditUtils(creditUtils);
                mntThread.start();

            } else {
                System.out.println(commandUsage);
            }
        }
    }
}

package kz.bsbnb.usci.receiver;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.queue.JobLauncherQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by emles on 06.09.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class LoadFileTest {

    protected final Logger logger = LoggerFactory.getLogger(LoadFileTest.class);

    @Autowired
    protected ZipFilesMonitor filesMonitor;

    @Autowired
    private JobLauncherQueue jobLauncherQueue;

    private JdbcTemplate jdbcTemplate_CORE_E;
    private JdbcTemplate jdbcTemplate_SHOWCASE_E;
    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    @Qualifier("dataSource_CORE_E")
    public void setDataSource_CORE_E(DataSource dataSource_CORE_E) {
        this.jdbcTemplate_CORE_E = new JdbcTemplate(dataSource_CORE_E);
    }

    @Autowired
    @Qualifier("dataSource_SHOWCASE_E")
    public void setDataSource_SHOWCASE_E(DataSource dataSource_SHOWCASE_E) {
        this.jdbcTemplate_SHOWCASE_E = new JdbcTemplate(dataSource_SHOWCASE_E);
    }

    protected void printRows(String title, List<Map<String, Object>> rows) {

        StringBuffer buffer = new StringBuffer(title);
        for (Map<String, Object> row : rows) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                buffer.append(entry.getKey()).append(" = <").append(entry.getValue()).append("> ");
            }
            buffer.append("\n");
        }

        logger.info(buffer.toString());

    }

    protected void printInfo(DB db, String title, String sql, Object[] params) {

        List<Map<String, Object>> rows = (db == DB.CORE ? jdbcTemplate_CORE_E : db == DB.SHOWCASE ? jdbcTemplate_SHOWCASE_E : null).queryForList(sql, params);

        printRows(title, rows);

    }

    protected void printCreditorInfo(Creditor creditor) {

        StringBuffer buffer = new StringBuffer("Creditor info:\n");

        buffer.append("batch creditor ID: ").append(creditor.getId()).append("\n");
        buffer.append("batch creditor BIN: ").append(creditor.getBIN()).append("\n");
        buffer.append("batch creditor BIK: ").append(creditor.getBIK()).append("\n");
        buffer.append("batch creditor Code: ").append(creditor.getCode()).append("\n");

        logger.info(buffer.toString());

    }

    protected void printCreditorInfo(long creditorId) {

        String title = "Creditor info:\n";
        String sql = "SELECT\n" +
                "  rc.*,\n" +
                "  cd.*,\n" +
                "  rc.ID,\n" +
                "  rc.REF_CREDITOR_ID,\n" +
                "  rc.CODE,\n" +
                "  rc.SHORT_NAME,\n" +
                "  rc.NAME,\n" +
                "  cd.NO\n" +
                "FROM R_REF_CREDITOR rc\n" +
                "  JOIN R_REF_CREDITOR_DOC cd ON cd.REF_CREDITOR_ID = rc.REF_CREDITOR_ID\n" +
                "WHERE\n" +
                "  rc.REF_CREDITOR_ID = ?";
        Object[] params = new Object[]{creditorId};

        printInfo(DB.SHOWCASE, title, sql, params);

    }

    protected void printBatchInfo(long batchId) {

        String title = "Batch info:\n";
        String sql = "SELECT\n" +
                "  bts.USER_ID,\n" +
                "  bts.CREDITOR_ID,\n" +
                "  bts.ID AS BATCH_ID,\n" +
                "  bts.FILE_NAME,\n" +
                "  bss.RECEIPT_DATE,\n" +
                "  gl.CODE,\n" +
                "  gl.VALUE,\n" +
                "  gl.DESCRIPTION,\n" +
                "  bss.DESCRIPTION\n" +
                "FROM EAV_BATCHES bts\n" +
                "  JOIN EAV_BATCH_STATUSES bss ON bts.ID = bss.BATCH_ID\n" +
                "  JOIN EAV_GLOBAL gl ON bss.STATUS_ID = gl.ID\n" +
                "WHERE bts.ID = ?\n" +
                "ORDER BY bts.id, bss.RECEIPT_DATE";
        Object[] params = new Object[]{batchId};

        printInfo(DB.CORE, title, sql, params);

    }

    protected void printEntitiesInfo(long batchId) {

        String title = "Entities info:\n";
        String sql = "SELECT\n" +
                "  es.BATCH_ID,\n" +
                "  es.ENTITY_ID,\n" +
                "  es.RECEIPT_DATE,\n" +
                "  es.DESCRIPTION,\n" +
                "  es.OPERATION,\n" +
                "  es.STATUS_ID,\n" +
                "  es.ERROR_CODE,\n" +
                "  es.DEV_DESCRIPTION,\n" +
                "  gl.CODE,\n" +
                "  gl.DESCRIPTION\n" +
                "FROM EAV_ENTITY_STATUSES es\n" +
                "  JOIN EAV_GLOBAL gl ON es.STATUS_ID = gl.ID\n" +
                "WHERE es.BATCH_ID = ?";
        Object[] params = new Object[]{batchId};

        List<Map<String, Object>> rows = jdbcTemplate_CORE_E.queryForList(sql, params);

        for (Map<String, Object> row : rows) {
            String errDescription = null;
            try {
                String errCode = row.get("ERROR_CODE").toString().trim();
                errDescription = Errors.replaceTags(Errors.valueOf(errCode), new Object[]{row.get("DEV_DESCRIPTION")});
            } catch (Exception e) {
            }
            row.put("ERROR_DESCRIPTION", errDescription);
        }

        printRows(title, rows);

    }

    private void printBatchEntitiesInfo(long batchId, String[] reportDates) {

        StringBuffer buffer = new StringBuffer("Entities history: ");

        List<Map<String, Object>> rows = getBatchEntitiesInfo(batchId);

        SortedSet<Long> ids = new TreeSet<>();

        for (Map<String, Object> row : rows) {
            Long entityId = ((BigDecimal) row.get("ENTITY_ID")).longValue();
            if (ids.contains(entityId)) continue;
            ids.add(entityId);
            for (String reportDate : reportDates) {
                buffer.append("\n");
                buffer.append("BATCH ID: ").append(batchId)
                        .append(" REPORT DATE: ").append(reportDate);
                buffer.append("\n");
                for (int i = 0; i < 64; i++) {
                    buffer.append("=");
                }
                buffer.append("\n");
                printEntity(buffer, entityId, reportDate);
                for (int i = 0; i < 64; i++) {
                    buffer.append("-");
                }
            }
        }

        logger.info(buffer.toString());

    }

    private List<Map<String, Object>> getBatchEntitiesInfo(long batchId) {

        String sql = "SELECT\n" +
                "  es.BATCH_ID,\n" +
                "  es.ENTITY_ID,\n" +
                "  es.RECEIPT_DATE,\n" +
                "  es.DESCRIPTION,\n" +
                "  es.OPERATION\n" +
                "FROM EAV_ENTITY_STATUSES es\n" +
                "  JOIN EAV_GLOBAL gl ON es.STATUS_ID = gl.ID\n" +
                "WHERE\n" +
                "  es.ENTITY_ID > 0 AND es.STATUS_ID = 15 AND\n" +
                "  es.BATCH_ID = ?";

        Object[] params = new Object[]{batchId};

        return jdbcTemplate_CORE_E.queryForList(sql, params);

    }

    public void printEntity(StringBuffer buffer, long id, String reportDate) {

        IBaseEntity entity = null;

        BLOCK:
        {
            if (reportDate == null) {
                entity = baseEntityLoadDao.load(id);
                break BLOCK;
            }


            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date date;

            try {
                date = dateFormat.parse(reportDate);
            } catch (ParseException e) {
                logger.error("Error printing Entity!", e);
                return;
            }

            if (date == null) break BLOCK;

            entity = baseEntityLoadDao.loadByMaxReportDate(id, date);

        }

        if (entity == null) {
            buffer.append("No such entity with id: ").append(id).append("\n");
        } else {
            buffer.append(entity.toString().trim()).append("\n");
        }

    }

    @Test
    public void loadFile() throws InterruptedException {

        String[] reportDates = new String[]{
                "01.07.2017",
                "01.08.2017",
                "01.09.2017",
                "01.10.2017"
        };

        logger.info("Loading file...");
        Batch batch = filesMonitor.readFiles("/opt/projects/info/batches/in/1/TEST1.zip", 10196L, false);
        if (batch == null) {
            logger.info("Finished loading, no batches...");
            return;
        }
        logger.info("Finished loading, batchId = " + batch.getId() + ".");

        while (true) {
            String status = jobLauncherQueue.getStatus();
            logger.info("Status: " + status);
            Thread.sleep(2000);
            if (status.contains("(empty)")) break;
        }


        printCreditorInfo(batch.getCreditor());

        printCreditorInfo(batch.getCreditor().getId());

        printBatchInfo(batch.getId());

        printEntitiesInfo(batch.getId());

        printBatchEntitiesInfo(batch.getId(), reportDates);

    }

    enum DB {
        CORE,
        SHOWCASE
    }

}




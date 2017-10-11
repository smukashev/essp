package kz.bsbnb.usci.receiver.tools

import com.google.gson.GsonBuilder
import kz.bsbnb.usci.cr.model.Creditor
import kz.bsbnb.usci.eav.model.base.IBaseEntity
import kz.bsbnb.usci.eav.model.output.BaseToShortTool
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao
import kz.bsbnb.usci.eav.util.Errors
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

import javax.sql.DataSource
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Created by emles on 17.09.17
 */
@Component
class PrintUtils {

    protected Logger logger = LoggerFactory.getLogger(PrintUtils.class)

    private JdbcTemplate jdbcTemplate_CORE_E
    private JdbcTemplate jdbcTemplate_SHOWCASE_E
    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao

    @Autowired
    @Qualifier("dataSource_CORE_E")
    void setDataSource_CORE_E(DataSource dataSource_CORE_E) {
        this.jdbcTemplate_CORE_E = new JdbcTemplate(dataSource_CORE_E)
    }

    @Autowired
    @Qualifier("dataSource_SHOWCASE_E")
    void setDataSource_SHOWCASE_E(DataSource dataSource_SHOWCASE_E) {
        this.jdbcTemplate_SHOWCASE_E = new JdbcTemplate(dataSource_SHOWCASE_E)
    }

    @Autowired
    JustDao justDao

    enum DB {
        CORE,
        SHOWCASE
    }

    protected static String preTag(String in_) {
        return in_
    }

    protected void printRows(String title, List<Map<String, Object>> rows) {

        StringBuffer buffer = new StringBuffer(title)
        buffer.append("<table><tr>")
        if (rows.size() > 0)
            for (Map.Entry<String, Object> entry : rows.get(0).entrySet()) {
                buffer.append("<th>").append(entry.getKey()).append("</th>")
            }
        buffer.append("</tr>")
        for (Map<String, Object> row : rows) {
            buffer.append("<tr>")
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                buffer.append("<td>").append(entry.getValue()).append("</td>")
            }
            buffer.append("</tr>")
        }
        buffer.append("</table>")

        logger.info(preTag(buffer.toString()))

    }

    protected void printInfo(DB db, String title, String sql, Object[] params) {

        List<Map<String, Object>> rows = (db == DB.CORE ? jdbcTemplate_CORE_E : db == DB.SHOWCASE ? jdbcTemplate_SHOWCASE_E : null).queryForList(sql, params)

        printRows(title, rows)

    }

    protected void printCreditorInfo(Creditor creditor) {

        StringBuffer buffer = new StringBuffer("Creditor info:\n")

        buffer.append("batch creditor ID: ").append(creditor.getId()).append("<br>")
        buffer.append("batch creditor BIN: ").append(creditor.getBIN()).append("<br>")
        buffer.append("batch creditor BIK: ").append(creditor.getBIK()).append("<br>")
        buffer.append("batch creditor Code: ").append(creditor.getCode()).append("<br>")

        logger.info(preTag(buffer.toString()))

    }

    protected void printCreditorInfo(long creditorId) {

        String title = "Creditor info:\n"
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
                "  rc.REF_CREDITOR_ID = ?"
        Object[] params = [creditorId]

        printInfo(DB.SHOWCASE, title, sql, params)

    }

    protected void printBatchInfo(long batchId) {

        String title = "Batch info:\n"
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
                "ORDER BY bts.id, bss.RECEIPT_DATE"
        Object[] params = [batchId]

        printInfo(DB.CORE, title, sql, params)

    }

    protected void printEntitiesInfo(long batchId) {

        String title = "Entities info:\n"
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
                "WHERE es.BATCH_ID = ?"
        Object[] params = [batchId]

        List<Map<String, Object>> rows = jdbcTemplate_CORE_E.queryForList(sql, params)

        for (Map<String, Object> row : rows) {
            String errDescription = null
            try {
                String errCode = row.get("ERROR_CODE").toString().trim()
                errDescription = Errors.replaceTags(Errors.valueOf(errCode), [
                        row.get("DEV_DESCRIPTION")
                ])
            } catch (Exception e) {
            }
            row.put("ERROR_DESCRIPTION", errDescription)
        }

        printRows(title, rows)

    }

    private void printBatchEntitiesInfo(long batchId, String[] reportDates) {

        StringBuffer buffer = new StringBuffer("Entities history: ")

        List<Map<String, Object>> rows = getBatchEntitiesInfo(batchId)

        SortedSet<Long> ids = new TreeSet<>()

        for (Map<String, Object> row : rows) {
            Long entityId = ((BigDecimal) row.get("ENTITY_ID")).longValue()
            if (ids.contains(entityId)) continue
            ids.add(entityId)
            buffer.append("<table><tr>")
            for (String reportDate : reportDates) {
                buffer.append("<th>")
                buffer.append("BATCH ID: ").append(batchId)
                        .append(" REPORT DATE: ").append(reportDate)
                buffer.append("</th>")
            }
            buffer.append("</tr><tr>")
            for (String reportDate : reportDates) {
                buffer.append("<td align=\"left\" valign=\"top\"><pre>")
                printEntity(buffer, entityId, reportDate)
                buffer.append("</pre></td>")
            }
            buffer.append("</tr></table>")
        }

        logger.info(preTag(buffer.toString()))

    }

    void printEntity(StringBuffer buffer, long id, String reportDate) {

        IBaseEntity entity = null

        ({
            if (reportDate == null) {
                entity = baseEntityLoadDao.load(id)
                return
            }

            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy")
            Date date

            try {
                date = dateFormat.parse(reportDate)
            } catch (ParseException e) {
                logger.error("Error printing Entity!", e)
                return
            }

            if (date == null) return

            entity = baseEntityLoadDao.loadByMaxReportDate(id, date)

        }).call()

        if (entity == null) {
            buffer.append("No such entity with id: ").append(id).append("\n")
        } else {
            if (true)
                BaseToShortTool.print(buffer, entity)
            else
                buffer.append(entity.toString().trim()).append("\n")
        }

    }

    void printBatchEntitiesInfoDetailed(long batchId, String[] reportDates) {

        StringBuffer buffer = new StringBuffer("Entities history: ")

        List<Map<String, Object>> rows = getBatchEntitiesInfo(batchId)

        SortedSet<Long> ids = new TreeSet<>()

        for (Map<String, Object> row : rows) {

            Long entityId = ((BigDecimal) row.get("ENTITY_ID")).longValue()
            if (ids.contains(entityId)) continue
            ids.add(entityId)

            Set<String> headers = []
            Map<String, Map<String, String>> maps = [:]

            for (String reportDate : reportDates) {
                Map<String, String> map = printEntityDetailed(entityId, reportDate)
                maps.put(reportDate, map)
                map.each { key, value ->
                    headers.add(key)
                }
            }
            headers = headers.sort { String a, String b -> a.compareTo(b) }

            buffer.append("<table>")

            buffer.append("<tr>")
            buffer.append("<th></th>")
            for (String reportDate : reportDates) {
                buffer.append("<th>")
                buffer.append("BATCH ID: ").append(batchId)
                        .append(" REPORT DATE: ").append(reportDate)
                buffer.append("</th>")
            }
            buffer.append("</tr>")

            for (String header : headers) {

                buffer.append("<tr>")

                buffer.append("<th align=\"left\" valign=\"top\">")
                buffer.append(header)
                buffer.append("</th>")

                maps.each { String reportDate, Map<String, String> map ->
                    buffer.append("<td><pre>")
                    buffer.append(map.get(header))
                    buffer.append("</pre></td>")
                }

                buffer.append("</tr>")

            }

            buffer.append("</table>")

        }

        logger.info(preTag(buffer.toString()))

    }

    void printBatchEntitiesInfoDetailedWithShowCases(long batchId, String[] reportDates) {

        StringBuffer buffer = new StringBuffer("Entities history: ")

        final List<Map<String, Object>> rows = getBatchEntitiesInfo(batchId)

        final def keys = ["CREDIT_ID", "SUBJECT_ID"] as String[]
        final Map<Long, Set<Long>> searchIds = getBatchInfo$creditIds$subjectIds(batchId)

        Map<Long, List<Map<String, Object>>> rowsByIds = justDao.getDataForShowcases(searchIds, keys)

        SortedSet<Long> ids = new TreeSet<>()

        for (Map<String, Object> row : rows) {

            Long entityId = ((BigDecimal) row.get("ENTITY_ID")).longValue()
            if (ids.contains(entityId)) continue
            ids.add(entityId)

            Set<String> headers = []
            Map<String, Map<String, String>> maps = [:]

            for (String reportDate : reportDates) {
                Map<String, String> map = printEntityDetailed(entityId, reportDate)
                maps.put(reportDate, map)
                map.each { key, value ->
                    headers.add(key)
                }
            }
            headers = headers.sort { String a, String b -> a.compareTo(b) }

            buffer.append("<table>")

            buffer.append("<tr>")
            buffer.append("<th></th>")
            for (String reportDate : reportDates) {
                buffer.append("<th>")
                buffer.append("EAV BATCH ID: ").append(batchId)
                        .append(" REPORT DATE: ").append(reportDate)
                buffer.append("</th>")
                buffer.append("<th>")
                buffer.append("SHOWCASE BATCH ID: ").append(batchId)
                        .append(" REPORT DATE: ").append(reportDate)
                buffer.append("</th>")
            }
            buffer.append("</tr>")

            for (String header : headers) {

                buffer.append("<tr>")

                buffer.append("<th align=\"left\" valign=\"top\">")
                buffer.append(header)
                buffer.append("</th>")

                maps.each { String reportDate, Map<String, String> map ->

                    Date report = new Date().parse("dd.MM.yyyy", reportDate)

                    buffer.append("<td><pre>")
                    buffer.append(map.get(header))
                    buffer.append("</pre></td>")

                    buffer.append("<td><pre>")

                    String vMetaClass
                    Long vEntityId
                    block:
                    {
                        List hdPath = header.split("\\.")
                        String last = hdPath.last()
                        (last =~ /(\w+)<(\d+)>/).each { exp, cl, id ->
                            vMetaClass = cl
                            vEntityId = id as Long
                        }
                    }

                    rowsByIds.each { ky, rowById ->
                        rowById.findAll {

                            def date = { k ->
                                String val
                                (val = it["$k"]) && !val.isEmpty() ? new Date().parse("yyyy-MM-dd HH:mm:SS", val) : null
                            }

                            Long id = it["ID_VALUE"] as Long

                            Date cdc = date("CDC")
                            Date open = date("OPEN_DATE")
                            Date close = date("CLOSE_DATE")

                            !(id != vEntityId || (open != null && open.after(report)) || (close != null && close.before(report)))

                        }
                        .each { Map<String, Object> r ->
                            buffer.append(GsonBuilder.newInstance().setPrettyPrinting().create().toJson(r))
                        }
                    }

                    buffer.append("</pre></td>")

                }

                buffer.append("</tr>")

            }

            buffer.append("</table>")

        }

        logger.info(preTag(buffer.toString()))

    }

    Map<String, String> printEntityDetailed(long id, String reportDate) {

        IBaseEntity entity = null

        ({
            if (reportDate == null) {
                entity = baseEntityLoadDao.load(id)
                return
            }

            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy")
            Date date

            try {
                date = dateFormat.parse(reportDate)
            } catch (ParseException e) {
                logger.error("Error printing Entity!", e)
                return
            }

            if (date == null) return

            entity = baseEntityLoadDao.loadByMaxReportDate(id, date)

        }).call()

        Map<String, String> map = new TreeMap<String, String>()
        if (entity == null) {
            return map
        } else {
            BaseToShortTool.print(map, entity)
            return map
        }

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
                "  es.BATCH_ID = ?"

        Object[] params = [batchId]

        return jdbcTemplate_CORE_E.queryForList(sql, params)

    }

    protected void printHistory(List<String> history) {

        StringBuffer buffer = new StringBuffer("History:\n")

        buffer.append("<table>")
        int count = 0
        for (String age : history) {
            buffer.append("<tr>")
            buffer.append("<td align=\"left\" valign=\"top\"><pre>")
            buffer.append((++count) as String)
            buffer.append("</pre></td>")
            buffer.append("<td align=\"left\" valign=\"top\"><pre>")
            buffer.append(age)
            buffer.append("</pre></td>")
            buffer.append("</tr>")
        }
        buffer.append("</table>")

        logger.info(preTag(buffer.toString()))

    }

    Map<Long, Set<Long>> getBatchInfo$creditIds$subjectIds(Long batchId) {
        Map<Long, Set<Long>> map = new TreeMap<>()
        Set<Long> creditIds = justDao.getCreditIds(batchId)
        creditIds.each { creditId ->
            Set<Long> subjectIds = justDao.getSubjectIds(creditId)
            map.put(creditId, subjectIds)
        }
        return map
    }

    void printBatchInfo$creditIds$subjectIds(Long batchId) {

        StringBuffer buffer = new StringBuffer("Batch Info: ")

        Map<Long, Set<Long>> rows = getBatchInfo$creditIds$subjectIds(batchId)

        SortedSet<Long> ids = new TreeSet<>()

        for (Map.Entry<Long, Set<Long>> row : rows) {
            Long entityId = row.key
            if (ids.contains(entityId)) continue
            ids.add(entityId)
            buffer.append("<table><tr>")
            buffer.append("<td align=\"left\" valign=\"top\"><pre>")
            buffer.append(entityId)
            buffer.append("</pre></td>")
            buffer.append("<td align=\"left\" valign=\"top\"><pre>")
            buffer.append(row.value)
            buffer.append("</pre></td>")
            buffer.append("</tr></table>")
        }

        logger.info(preTag(buffer.toString()))

    }

}




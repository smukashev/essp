package kz.bsbnb.usci.receiver.tools

import kz.bsbnb.eav.persistance.generated.CoreE
import kz.bsbnb.showcase.persistance.generated.ShowcaseE
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Select
import org.jooq.Table
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

import javax.sql.DataSource

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_ENTITY_STATUSES
import static kz.bsbnb.showcase.persistance.generated.Tables.R_CORE_DEBTOR
import static kz.bsbnb.showcase.persistance.generated.Tables.R_CORE_DEBTOR_HIS

/**
 * Created by emles on 10.10.17
 */
@Component
class JustDao {

    private final Logger logger = LoggerFactory.getLogger(JustDao.class)

    private JdbcTemplate jdbcTemplateSC

    @Autowired
    @Qualifier("dataSourceSC")
    void setDataSourceSC(DataSource dataSource) {
        this.jdbcTemplateSC = new JdbcTemplate(dataSource)
    }

    private JdbcTemplate jdbcTemplateCR

    @Autowired
    @Qualifier("dataSourceCR")
    void setDataSourceCR(DataSource dataSource) {
        this.jdbcTemplateCR = new JdbcTemplate(dataSource)
    }

    @Autowired
    private DSLContext context

    private ShowcaseE SHOWCASE_E = ShowcaseE.SHOWCASE_E

    private CoreE CORE_E = CoreE.CORE_E

    @Autowired
    ShowCaseUtils showCaseUtils

    Set<Long> getCreditIds(Long batchId) {

        final Set<Long> subjectIds = []

        /*
        * select es.ENTITY_ID from EAV_ENTITY_STATUSES es where es.ENTITY_ID is not null and es.ENTITY_ID <> 0 and es.STATUS_ID = 15 and es.BATCH_ID = 512 GROUP BY es.ENTITY_ID
        * */

        String tableAlias = "es"
        Select select =
                context
                        .select(EAV_ENTITY_STATUSES.as(tableAlias).ENTITY_ID)
                        .from(EAV_ENTITY_STATUSES.as(tableAlias))
                        .where(
                        EAV_ENTITY_STATUSES.as(tableAlias).ENTITY_ID.isNotNull()
                                .and(EAV_ENTITY_STATUSES.as(tableAlias).ENTITY_ID.notEqual(0L))
                                .and(EAV_ENTITY_STATUSES.as(tableAlias).STATUS_ID.eq(15L))
                                .and(EAV_ENTITY_STATUSES.as(tableAlias).BATCH_ID.eq(batchId)))
                        .groupBy(EAV_ENTITY_STATUSES.as(tableAlias).ENTITY_ID)
                        .orderBy(EAV_ENTITY_STATUSES.as(tableAlias).ENTITY_ID.desc())


        logger.info(select.toString())

        List<Map<String, Object>> rows = jdbcTemplateCR.queryForList(select.getSQL(), select.getBindValues().toArray())

        for (Map<String, Object> row : rows) {
            Long subjectId = ((BigDecimal) row.get(EAV_ENTITY_STATUSES.as(tableAlias).ENTITY_ID.getName())).longValue()
            subjectIds.add(subjectId)
        }

        return subjectIds

    }

    Set<Long> getSubjectIds(Long creditId) {

        final Set<Long> subjectIds = []

        String tableAlias = "d"
        Select select =
                context
                        .select(R_CORE_DEBTOR.as(tableAlias).SUBJECT_ID)
                        .from(R_CORE_DEBTOR.as(tableAlias))
                        .where(R_CORE_DEBTOR.as(tableAlias).CREDIT_ID.equal(creditId))
                        .unionAll(
                        context
                                .select(R_CORE_DEBTOR_HIS.as(tableAlias).SUBJECT_ID)
                                .from(R_CORE_DEBTOR_HIS.as(tableAlias))
                                .where(R_CORE_DEBTOR_HIS.as(tableAlias).CREDIT_ID.equal(creditId))
                )

        logger.info(select.toString())

        List<Map<String, Object>> rows = jdbcTemplateSC.queryForList(select.getSQL(), select.getBindValues().toArray())

        for (Map<String, Object> row : rows) {
            Long subjectId = ((BigDecimal) row.get(R_CORE_DEBTOR.as(tableAlias).SUBJECT_ID.getName())).longValue()
            subjectIds.add(subjectId)
        }

        return subjectIds

    }

    def getShowcaseTables() {

        List<Table> tables = []

        SHOWCASE_E.getTables()
                .findAll { Table table -> table.name.startsWith("R_CORE_") || table.name.startsWith("R_REF_") }
                .each { Table table ->
            tables << table
        }

        tables

    }

    def getShowcases() {
        List<ShowCaseUtils.Table> tables = showCaseUtils.genTables()
        tables
    }

    def loop = { List<ShowCaseUtils.Table> showcases, List<Table> tables, Closure closure ->
        int sc = 0, tb = 0
        while (true) {
            if (tb >= tables.size()) {
                tb = 0; ++sc
            }
            if (sc >= showcases.size()) break
            closure(showcases[sc], tables[tb])
            ++tb
        }
    }

    def mearge$Showcases$Tables() {

        final List<ShowCaseUtils.Table> showcases = getShowcases()
        final List<Table> tables = getShowcaseTables()

        loop(showcases, tables) { ShowCaseUtils.Table showcase, Table table ->
            if (table.name ==~ /(?i)r_$showcase.name(_his)?/) {
                showcase.tables << table
            }
        }

        showcases

    }

    def splitShowcases(String... keys) {

        final List<ShowCaseUtils.Table> showcases = mearge$Showcases$Tables()
        final List<Table> tables = getShowcaseTables()

        Map<String, List<ShowCaseUtils.Table>> splitShowcases = [:]

        keys.each { key ->
            splitShowcases["${key}"] = []
        }

        loop(showcases, tables) { ShowCaseUtils.Table showcase, Table table ->
            if ("R_$showcase.name" ==~ "(?i)$table.name") {
                table.fields().each { Field field ->
                    keys.each { key ->
                        if (field.name ==~ /(?i)$key/) {
                            splitShowcases["${key}"] << showcase
                        }
                    }
                }
            }
        }

        splitShowcases

    }

    def getDataForShowcases(Map<Long, Set<Long>> map, String... keys) {

        final Map<String, List<ShowCaseUtils.Table>> splitShowcases = splitShowcases(keys)

        Map<Long, List<Map<String, Object>>> rowsByCredit = [:]

        map.each { k, v ->
            rowsByCredit["$k"] = []
        }

        def closure = { String key, Long value, ShowCaseUtils.Table showcase, Table table ->

            String tableAlias = "t"
            Select select =
                    context
                            .select()
                            .from(table.as(tableAlias))
                            .where(
                            table.as(tableAlias).field(key).eq(value))
                            .orderBy(table.as(tableAlias).field("ID").desc())


            logger.info(select.toString())

            List<Map<String, Object>> out = jdbcTemplateSC.queryForList(select.getSQL(), select.getBindValues().toArray())

            out.each {
                rowsByCredit["$value"] << it
                String idKey = "${showcase.meta.className}_id"
                String idValue = it.find { it.key ==~ /(?i)${idKey}/ }?.value
                it["META_CLASS"] = showcase.meta.className
                if (idValue && !idValue.isEmpty()) {
                    it["ID_KEY"] = idKey
                    it["ID_VALUE"] = idValue
                } else {
                    it["ID_KEY"] = null
                    it["ID_VALUE"] = null
                }
            }

        }

        keys.each { key ->
            map.each { creditId, subjectIds ->
                splitShowcases.findAll {
                    it.key ==~ /(?i)$key/
                }
                .each { String field, List<ShowCaseUtils.Table> showcases ->
                    showcases.each { ShowCaseUtils.Table showcase ->
                        showcase.tables.each { Table table ->
                            closure(key, creditId, showcase, table)
                        }
                    }
                }
            }
        }

        rowsByCredit

    }

}




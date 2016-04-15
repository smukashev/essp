package kz.bsbnb.usci.eav.rule.impl;

import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.rule.*;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.Comparator;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Component
public class RuleDaoImpl extends JDBCSupport implements IRuleDao {

    private final String PREFIX_ = "LOGIC_";

    private final Logger logger = LoggerFactory.getLogger(RuleDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    public boolean testConnection()
    {
        try
        {
            return !jdbcTemplate.getDataSource().getConnection().isClosed();
        }
        catch (SQLException e)
        {
            return false;
        }
    }


    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public List<Rule> load(PackageVersion packageVersion) {
        //if (packageVersion.getId() < 1)
        //    throw new IllegalArgumentException("Batchversion has no id.");

        Table ruleHistory = getRuleHistoryTable(packageVersion.getReportDate());

        Select select = context.select(ruleHistory.fields())
                .from(ruleHistory)
                .join(LOGIC_RULE_PACKAGE)
                .on(ruleHistory.field("ID").eq(LOGIC_RULE_PACKAGE.RULE_ID))
                .and(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(packageVersion.getRulePackage().getId()));

        List<Rule> ruleList = new ArrayList<>();

        List<Map<String,Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for(Map<String,Object> row : rows) {
            Rule rule = getRule(row);
            ruleList.add(rule);
        }

        return ruleList;
    }

    @Override
    public long update(Rule rule) {
        if (rule.getId() < 1){
            throw new IllegalArgumentException(Errors.compose(Errors.E266));
        }

        Update update = context.update(LOGIC_RULES)
                .set(LOGIC_RULES.TITLE, rule.getTitle())
                .set(LOGIC_RULES.RULE, rule.getRule())
                .set(LOGIC_RULES.OPEN_DATE, DataUtils.convert(rule.getOpenDate()))
                .where(LOGIC_RULES.ID.eq(rule.getId()));

        /*String SQL = "UPDATE " + PREFIX_ + "rules SET title=?, rule=? WHERE id=?";
        jdbcTemplate.update(SQL,rule.getTitle(),rule.getRule(),rule.getId());
        return rule.getId();*/

        updateWithStats(update.getSQL(), update.getBindValues().toArray());

        return rule.getId();
    }

    @Override
    public List<Rule> getAllRules() {
        String SQL = "SELECT * FROM " + PREFIX_ + "rules order by id";
        List<Rule> ruleList = jdbcTemplate.query(SQL,new BeanPropertyRowMapper(Rule.class));

        return ruleList;
    }


    @Override
    public List<SimpleTrack> getRuleTitles(Long packageId, Date reportDate) {
        /*String SQL = "SELECT id, title AS NAME, is_active as isActive FROM " + PREFIX_ + "rules WHERE id IN (\n" +
                "SELECT rule_id FROM " + PREFIX_ + "rule_package_versions WHERE package_versions_id = \n" +
                "(SELECT id FROM " + PREFIX_ + "package_versions WHERE OPEN_DATE = \n" +
                "    (SELECT MAX(OPEN_DATE) FROM " + PREFIX_ + "package_versions WHERE package_id = ? AND OPEN_DATE <= ? ) \n" +
                " AND package_id = ? AND rownum = 1\n" +
                " )) order by id";*/


        Table ruleHistory = getRuleHistoryTable();


        Select select = context.select(ruleHistory.field("ID"), ruleHistory.field("TITLE"))
                .from(ruleHistory)
                .join(LOGIC_RULE_PACKAGE).on(LOGIC_RULE_PACKAGE.RULE_ID.eq(ruleHistory.field("ID")))
                .where(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(packageId))
                .and(ruleHistory.field("OPEN_DATE").lessOrEqual(DataUtils.convert(reportDate)))
                .and(ruleHistory.field("CLOSE_DATE").greaterThan(DataUtils.convert(reportDate)).or(ruleHistory.field("CLOSE_DATE").isNull()));


        //List<SimpleTrack> ret = jdbcTemplate.query(SQL, new Object[]{packageId, reportDate, packageId}, new BeanPropertyRowMapper(SimpleTrack.class));
        List<Map<String,Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        List<SimpleTrack> ret = new ArrayList<>();

        for(Map<String,Object> row: rows) {
            SimpleTrack s = new SimpleTrack();
            s.setId(((BigDecimal) row.get(LOGIC_RULES.ID.getName())).longValue());
            s.setName(((String) row.get(LOGIC_RULES.TITLE.getName())));
            s.setIsActive(true);
            ret.add(s);
        }

        return ret;
    }

    @Override
    public List<SimpleTrack> getRuleTitles(Long packageId, Date reportDate, String searchText) {
        //return new LinkedList<>();
        searchText = searchText.toLowerCase();
        Select select = context.select(LOGIC_RULES.ID, LOGIC_RULES.TITLE.as("name"), LOGIC_RULES.RULE)
                .from(LOGIC_RULES)
                .join(LOGIC_RULE_PACKAGE).on(LOGIC_RULES.ID.eq(LOGIC_RULE_PACKAGE.RULE_ID))
                .where(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(packageId))
                .and(LOGIC_RULES.OPEN_DATE.lessOrEqual(DataUtils.convert(reportDate)))
                .and(LOGIC_RULES.CLOSE_DATE.greaterThan(DataUtils.convert(reportDate)).or(LOGIC_RULES.CLOSE_DATE.isNull()))
                .and(LOGIC_RULES.RULE.lower().like("%" + searchText + "%").or(LOGIC_RULES.TITLE.lower().like("%" + searchText + "%")));

        return jdbcTemplate.query(select.getSQL(), select.getBindValues().toArray(), new BeanPropertyRowMapper<>(SimpleTrack.class));
    }

    public long save(Rule rule, PackageVersion packageVersion){
        /*String SQL = "INSERT INTO " + PREFIX_ + "rules(title, rule) VALUES(?, ?)";
        jdbcTemplate.update(SQL,rule.getTitle(),rule.getRule());

        SQL = "SELECT id FROM " + PREFIX_ + "rules WHERE rule = ?";
        long id = jdbcTemplate.queryForLong(SQL,rule.getRule());*/

//        if (batchVersion.getId() > 0){
//            SQL = "Insert into rule_package_versions(rule_id,package_versions_id) values(?,?)";
//            jdbcTemplate.update(SQL,id,batchVersion.getId());
//        }

        //return id;

        Insert insert = context.insertInto(LOGIC_RULES)
                .set(LOGIC_RULES.TITLE, rule.getTitle())
                .set(LOGIC_RULES.RULE, rule.getRule())
                .set(LOGIC_RULES.OPEN_DATE, DataUtils.convert(packageVersion.getReportDate()));

        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public Rule getRule(Rule rule) {

        if(rule.getId()< 1 || rule.getOpenDate() == null)
            throw new RuntimeException("Ид или отчетная дата указаны неверно");

        Table ruleHistory = getRuleHistoryTable();

        Select select = context.selectFrom(ruleHistory)
                .where(ruleHistory.field("ID").eq(rule.getId()))
                .and(ruleHistory.field("OPEN_DATE").lessOrEqual(rule.getOpenDate()))
                .and(ruleHistory.field("CLOSE_DATE").greaterThan(rule.getOpenDate()).or(ruleHistory.field("CLOSE_DATE").isNull()));


        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return getRule(rows.iterator().next());
        /*
        String SQL = "SELECT * FROM " + PREFIX_ + "rules WHERE id = ?";
        List<Rule> rules = jdbcTemplate.query(SQL, new Object[]{rule}, new BeanPropertyRowMapper<Rule>(Rule.class));
        if(rules.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E264));
        return rules.get(0);*/
    }

    @Override
    public boolean deleteRule(long ruleId, RulePackage rulePackage) {
        /*jdbcTemplate.update("DELETE FROM " + PREFIX_ + "rule_package_versions WHERE rule_id = ? " +
                "AND package_versions_id = ?", ruleId, packageVersion);*/
        Delete delete = context.delete(LOGIC_RULE_PACKAGE)
                .where(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(rulePackage.getId()))
                .and(LOGIC_RULE_PACKAGE.RULE_ID.eq(ruleId));

        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        return true;
    }

    @Override
    public long createEmptyRule(final String title){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String defaultRuleBody = "rule \" rule_" + (new Random().nextInt()) + "\"\nwhen\nthen\nend";
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO " + PREFIX_ + "rules (title, rule) " +
                        "VALUES(?, ?)", new String[]{"id"});
                ps.setString(1,title);
                ps.setString(2, defaultRuleBody);
                return ps;
            }
        },keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void save(long ruleId, long batchVersionId) {
        String sql = "INSERT INTO " + PREFIX_ + "rule_package_versions (rule_id , package_versions_id) VALUES (?,?)";
        jdbcTemplate.update(sql,new Object[]{ruleId,batchVersionId});
    }

    @Override
    public void saveInPackage(Rule rule, PackageVersion packageVersion) {
        Insert insert = context.insertInto(LOGIC_RULE_PACKAGE)
                .set(LOGIC_RULE_PACKAGE.RULE_ID, rule.getId())
                .set(LOGIC_RULE_PACKAGE.PACKAGE_ID, packageVersion.getRulePackage().getId());

        insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public RulePackage getPackage(String name) {
        Select select = context.selectFrom(LOGIC_PACKAGES).where(LOGIC_PACKAGES.NAME.eq(name));

        List<Map<String,Object> > rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if(rows.size() < 1)
            throw new IllegalArgumentException("Отсутствует пакет: " + name);

        Map<String,Object> row = rows.iterator().next();

        RulePackage ret = new RulePackage();
        ret.setId(((BigDecimal) row.get(LOGIC_PACKAGES.ID.getName())).longValue());
        ret.setName(name);
        return ret;
    }

    @Override
    public void updateBody(Rule rule) {
        /*String sql = "UPDATE " + PREFIX_ + "rules SET rule = ? WHERE id=?";
        jdbcTemplate.update(sql, new Object[]{body, rule});*/

        Select select = context.select(LOGIC_RULES.ID)
                .from(LOGIC_RULES)
                .where(LOGIC_RULES.ID.eq(rule.getId()))
                .and(LOGIC_RULES.OPEN_DATE.eq(DataUtils.convert(rule.getOpenDate())));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Table table = LOGIC_RULES;
        Field field = LOGIC_RULES.ID;

        if(rows.size() < 1) {
            table = LOGIC_RULES_HIS;
            field = LOGIC_RULES_HIS.RULE_ID;
        }

        Update update = context.update(table).
                set(DSL.field("RULE"), rule.getRule())
                .where(field.eq(rule.getId()))
                .and(DSL.field("OPEN_DATE").eq(rule.getOpenDate()));

        updateWithStats(update.getSQL(), update.getBindValues().toArray());

    }

    @Override
    public void copyExistingRule(long ruleId, long batchVersionId) {
        String sql = "INSERT INTO " + PREFIX_ + "rule_package_versions (rule_id, package_versions_id) VALUES (?,?)";
        jdbcTemplate.update(sql, new Object[]{ruleId, batchVersionId});
    }

    @Override
    public long createCopy(final long ruleId, final String title) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO " + PREFIX_ + "rules (rule, title)\n" +
                        "  SELECT rule, ? AS title FROM rules WHERE id = ?", new String[]{"id"});
                ps.setString(1, title);
                ps.setLong(2,ruleId);
                return ps;
            }
        },keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public long createRule(Rule rule, PackageVersion packageVersion) {
        Insert insert = context.insertInto(LOGIC_RULES)
                .set(LOGIC_RULES.TITLE, rule.getTitle())
                .set(LOGIC_RULES.RULE, rule.getRule())
                .set(LOGIC_RULES.OPEN_DATE, DataUtils.convert(packageVersion.getReportDate()));

        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        /*KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO " + PREFIX_ + "rules (title, rule)" +
                        "values (?, ?)", new String[]{"id"});
                ps.setString(1, rule.getTitle());
                ps.setString(2, rule.getRule());
                return ps;
            }
        }, keyHolder);
        return keyHolder.getKey().longValue();*/
    }

    @Override
    public void renameRule(long ruleId, String title) {
        String sql = "Update " + PREFIX_ + "rules set title = ? where id = ?";
        jdbcTemplate.update(sql, title, ruleId);
    }

    @Override
    public void clearAllRules() {
        Delete delete = context.delete(LOGIC_RULE_PACKAGE);
        jdbcTemplate.update(delete.getSQL());

        delete = context.delete(LOGIC_RULES);
        jdbcTemplate.update(delete.getSQL());
    }

    @Override
    public List<PackageVersion> getPackageVersions(RulePackage rulePackage) {

        Table rulesTable = getRuleHistoryTable();

        Select select = context.selectDistinct(rulesTable.field("OPEN_DATE"))
                .from(rulesTable)
                .join(LOGIC_RULE_PACKAGE).on(rulesTable.field("ID").eq(LOGIC_RULE_PACKAGE.RULE_ID))
                .and(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(rulePackage.getId()));

        List<Map<String,Object>> rows
                = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Set<Date> dates = new HashSet<>();

        for(Map<String,Object> row: rows) {
            dates.add((Date)row.get("OPEN_DATE"));
        }

        select = context.selectDistinct(rulesTable.field("CLOSE_DATE"))
                .from(rulesTable)
                .join(LOGIC_RULE_PACKAGE).on(rulesTable.field("ID").eq(LOGIC_RULE_PACKAGE.RULE_ID))
                .and(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(rulePackage.getId()))
                .and(rulesTable.field("CLOSE_DATE").isNotNull());

        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for(Map<String,Object> row: rows) {
            dates.add((Date)row.get(LOGIC_RULES.CLOSE_DATE.getName()));
        }

        List<PackageVersion> ret = new LinkedList<>();

        for(Date date: dates) {
            ret.add(new PackageVersion(rulePackage, date));
        }

        Collections.sort(ret, new Comparator<PackageVersion>() {
            @Override
            public int compare(PackageVersion o1, PackageVersion o2) {
                return o1.getReportDate().compareTo(o2.getReportDate());
            }
        });

        return ret;
    }

    @Override
    public void insertHistory(Rule rule, Date closeDate) {
        Insert insert = context.insertInto(LOGIC_RULES_HIS)
                .set(LOGIC_RULES_HIS.RULE_ID, rule.getId())
                .set(LOGIC_RULES_HIS.TITLE, rule.getTitle())
                .set(LOGIC_RULES_HIS.RULE, rule.getRule())
                .set(LOGIC_RULES_HIS.OPEN_DATE, DataUtils.convert(rule.getOpenDate()))
                .set(LOGIC_RULES_HIS.CLOSE_DATE, DataUtils.convert(closeDate));

        updateWithStats(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public List<Rule> getRuleHistory(long ruleId) {
        Select select = context.selectFrom(getRuleHistoryTable()).where(DSL.field("ID").eq(ruleId));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        List<Rule> ruleList = new ArrayList<>();

        long t = 1;
        for(Map<String,Object> row : rows) {
            Rule rule = getRule(row);
            rule.setId(t++ );
            ruleList.add(rule);
        }
        
        return ruleList;
    }

    @Override
    public List<RulePackage> getRulePackages(Rule rule) {
        Select select = context.selectDistinct(LOGIC_RULE_PACKAGE.PACKAGE_ID)
                .from(LOGIC_RULE_PACKAGE)
                .where(LOGIC_RULE_PACKAGE.RULE_ID.eq(rule.getId()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<RulePackage> rulePackageList = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            RulePackage rulePackage = new RulePackage();
            rulePackage.setId(((BigDecimal) row.get(LOGIC_RULE_PACKAGE.PACKAGE_ID.getName())).longValue());
            rulePackageList.add(rulePackage);
        }
        return rulePackageList;
    }

    private Rule getRule(Map<String,Object> row){
        Rule rule = new Rule();
        rule.setId(((BigDecimal) row.get(LOGIC_RULES.ID.getName())).longValue());
        rule.setRule(((String) row.get(LOGIC_RULES.RULE.getName())));
        rule.setOpenDate(((Date) row.get(LOGIC_RULES.OPEN_DATE.getName())));
        rule.setTitle(((String) row.get(LOGIC_RULES.TITLE.getName())));
        rule.setCloseDate((Date) row.get(LOGIC_RULES.CLOSE_DATE.getName()));
        return rule;
    }

    private Table getRuleHistoryTable(){
        Select ruleSelect = context.select(LOGIC_RULES.ID,
                LOGIC_RULES.RULE,
                LOGIC_RULES.TITLE,
                LOGIC_RULES.OPEN_DATE,
                LOGIC_RULES.CLOSE_DATE)
                .from(LOGIC_RULES);

        Select ruleHistorySelect = context.select(LOGIC_RULES_HIS.RULE_ID.as("ID"),
                LOGIC_RULES_HIS.RULE,
                LOGIC_RULES_HIS.TITLE,
                LOGIC_RULES_HIS.OPEN_DATE,
                LOGIC_RULES_HIS.CLOSE_DATE)
                .from(LOGIC_RULES_HIS);


        return ruleSelect.unionAll(ruleHistorySelect).asTable("v_rule_his");
    }

    private Table getRuleHistoryTable(Date date){
        Table t = getRuleHistoryTable();
        return context.selectFrom(t).where(t.field("OPEN_DATE")
                .lessOrEqual(DataUtils.convert(date)))
                .and(t.field("CLOSE_DATE").greaterThan(DataUtils.convert(date))
                        .or(t.field("CLOSE_DATE").isNull())).asTable("v_rule_snap");

    }
}

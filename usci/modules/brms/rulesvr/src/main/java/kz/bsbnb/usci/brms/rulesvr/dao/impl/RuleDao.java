package kz.bsbnb.usci.brms.rulesvr.dao.impl;

import kz.bsbnb.usci.brms.rulemodel.model.impl.PackageVersion;
import kz.bsbnb.usci.brms.rulemodel.model.impl.Rule;
import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.brms.rulemodel.model.impl.SimpleTrack;
import kz.bsbnb.usci.brms.rulesvr.persistable.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import kz.bsbnb.usci.brms.rulesvr.dao.IRuleDao;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static kz.bsbnb.usci.brms.rulesvr.generated.Tables.*;

/**
 * @author abukabayev
 */
public class RuleDao extends JDBCSupport implements IRuleDao {

    private final String PREFIX_ = "LOGIC_";

    private final Logger logger = LoggerFactory.getLogger(RuleDao.class);

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

        Select select = context.select(LOGIC_RULES.ID,
                LOGIC_RULES.RULE,LOGIC_RULES.TITLE, LOGIC_RULES.OPEN_DATE)
                .from(LOGIC_RULES).join(LOGIC_RULE_PACKAGE)
                .on(LOGIC_RULE_PACKAGE.RULE_ID.eq(LOGIC_RULES.ID))
                .where(LOGIC_RULES.OPEN_DATE.lessOrEqual(DataUtils.convert(packageVersion.getReportDate())))
                .and(LOGIC_RULES.CLOSE_DATE.greaterThan(DataUtils.convert(packageVersion.getReportDate()))
                        .or(LOGIC_RULES.CLOSE_DATE.isNull()));

        //List<Rule> ruleList = jdbcTemplate.query(select.getSQL(),select.getBindValues().toArray(),
        //        new BeanPropertyRowMapper(Rule.class));

        List<Rule> ruleList = new ArrayList<>();

        List<Map<String,Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for(Map<String,Object> row : rows) {
            Rule rule = new Rule();
            rule.setId(((BigDecimal) row.get(LOGIC_RULES.ID.getName())).longValue());
            rule.setRule(((String) row.get(LOGIC_RULES.RULE.getName())));
            rule.setOpenDate(((Date) row.get(LOGIC_RULES.OPEN_DATE.getName())));
            rule.setTitle(((String) row.get(LOGIC_RULES.TITLE.getName())));
        }

        return ruleList;
    }

    @Override
    public long update(Rule rule) {
        if (rule.getId() < 1){
            throw new IllegalArgumentException("Rule has no id.");
        }
        String SQL = "UPDATE " + PREFIX_ + "rules SET title=?, rule=? WHERE id=?";
        jdbcTemplate.update(SQL,rule.getTitle(),rule.getRule(),rule.getId());
        return rule.getId();
    }

    @Override
    public List<Rule> getAllRules() {
        String SQL = "SELECT * FROM " + PREFIX_ + "rules order by id";
        List<Rule> ruleList = jdbcTemplate.query(SQL,new BeanPropertyRowMapper(Rule.class));

        return ruleList;
    }


    @Override
    public List<SimpleTrack> getRuleTitles(Long packageId, Date repDate) {
        String SQL = "SELECT id, title AS NAME, is_active as isActive FROM " + PREFIX_ + "rules WHERE id IN (\n" +
                "SELECT rule_id FROM " + PREFIX_ + "rule_package_versions WHERE package_versions_id = \n" +
                "(SELECT id FROM " + PREFIX_ + "package_versions WHERE OPEN_DATE = \n" +
                "    (SELECT MAX(OPEN_DATE) FROM " + PREFIX_ + "package_versions WHERE package_id = ? AND OPEN_DATE <= ? ) \n" +
                " AND package_id = ? AND rownum = 1\n" +
                " )) order by id";


        List<SimpleTrack> ret = jdbcTemplate.query(SQL, new Object[]{packageId, repDate, packageId}, new BeanPropertyRowMapper(SimpleTrack.class));

        return ret;
    }

    @Override
    public List<SimpleTrack> getRuleTitles(Long batchVersionId, String searchText) {
        return new LinkedList<>();
        /*searchText = searchText.toLowerCase();
        Select select = context.select(LOGIC_RULES.ID, LOGIC_RULES.TITLE.as("name"), LOGIC_RULES.IS_ACTIVE, LOGIC_RULES.RULE)
                .from(LOGIC_RULES)
                .join(LOGIC_RULE_PACKAGE_VERSIONS).on(LOGIC_RULES.ID.eq(LOGIC_RULE_PACKAGE_VERSIONS.RULE_ID))
                .where(LOGIC_RULE_PACKAGE_VERSIONS.PACKAGE_VERSIONS_ID.eq(batchVersionId))
                .and(LOGIC_RULES.RULE.lower().like("%" + searchText + "%").or(LOGIC_RULES.TITLE.lower().like("%" + searchText + "%")));
        return jdbcTemplate.query(select.getSQL(), select.getBindValues().toArray(), new BeanPropertyRowMapper<SimpleTrack>(SimpleTrack.class));*/
    }

    public long save(Rule rule, PackageVersion packageVersion){
        String SQL = "INSERT INTO " + PREFIX_ + "rules(title, rule) VALUES(?, ?)";
        jdbcTemplate.update(SQL,rule.getTitle(),rule.getRule());

        SQL = "SELECT id FROM " + PREFIX_ + "rules WHERE rule = ?";
        long id = jdbcTemplate.queryForLong(SQL,rule.getRule());

//        if (batchVersion.getId() > 0){
//            SQL = "Insert into rule_package_versions(rule_id,package_versions_id) values(?,?)";
//            jdbcTemplate.update(SQL,id,batchVersion.getId());
//        }

        return id;
    }

    @Override
    public Rule getRule(Long ruleId) {
        String SQL = "SELECT * FROM " + PREFIX_ + "rules WHERE id = ?";
        List<Rule> rules = jdbcTemplate.query(SQL, new Object[]{ruleId}, new BeanPropertyRowMapper<Rule>(Rule.class));
        if(rules.size() > 1)
            throw new RuntimeException("several rules with same id");
        return rules.get(0);
    }

    @Override
    public boolean deleteRule(long ruleId, long batchVersionId) {
        jdbcTemplate.update("DELETE FROM " + PREFIX_ + "rule_package_versions WHERE rule_id = ? " +
                "AND package_versions_id = ?", ruleId, batchVersionId);
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
    public void saveInPackage(Rule rule, RulePackage rulePackage) {
        Insert insert = context.insertInto(LOGIC_RULE_PACKAGE)
                .set(LOGIC_RULE_PACKAGE.RULE_ID, rule.getId())
                .set(LOGIC_RULE_PACKAGE.PACKAGE_ID, rulePackage.getId());

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
    public void updateBody(Long ruleId, String body) {
        String sql = "UPDATE " + PREFIX_ + "rules SET rule = ? WHERE id=?";
        jdbcTemplate.update(sql, new Object[]{body, ruleId});
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
    public long createRule(Rule rule) {
        Insert insert = context.insertInto(LOGIC_RULES)
                .set(LOGIC_RULES.TITLE, rule.getTitle())
                .set(LOGIC_RULES.RULE, rule.getRule())
                .set(LOGIC_RULES.OPEN_DATE, DataUtils.convert(rule.getOpenDate()));

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
        Select select = context.select(LOGIC_RULES.OPEN_DATE)
                .from(LOGIC_RULES)
                .join(LOGIC_RULE_PACKAGE).on(LOGIC_RULES.ID.eq(LOGIC_RULE_PACKAGE.RULE_ID))
                .and(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(rulePackage.getId()));

        List<Map<String,Object>> rows
                = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Set<Date> dates = new HashSet<>();

        for(Map<String,Object> row: rows) {
            dates.add((Date)row.get(LOGIC_RULES.OPEN_DATE.getName()));
        }

        select = context.select(LOGIC_RULES.CLOSE_DATE)
                .from(LOGIC_RULES)
                .join(LOGIC_RULE_PACKAGE).on(LOGIC_RULES.ID.eq(LOGIC_RULE_PACKAGE.RULE_ID))
                .and(LOGIC_RULE_PACKAGE.PACKAGE_ID.eq(rulePackage.getId()))
                .and(LOGIC_RULES.CLOSE_DATE.isNotNull());


        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for(Map<String,Object> row: rows) {
            dates.add((Date)row.get(LOGIC_RULES.CLOSE_DATE.getName()));
        }

        List<PackageVersion> ret = new LinkedList<>();

        for(Date date: dates) {
            ret.add(new PackageVersion(rulePackage.getName(), date));
        }

        return ret;
    }
}

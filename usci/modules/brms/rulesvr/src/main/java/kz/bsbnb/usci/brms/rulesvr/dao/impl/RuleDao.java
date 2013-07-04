package kz.bsbnb.usci.brms.rulesvr.dao.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import kz.bsbnb.usci.brms.rulesvr.dao.IRuleDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * @author abukabayev
 */
public class RuleDao implements IRuleDao {

    private JdbcTemplate jdbcTemplate;


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


    public List<Rule> load(BatchVersion batchVersion){
        if (batchVersion.getId() < 1){
            throw new IllegalArgumentException("Batchversion has no id.");
        }
        String SQL = "Select * from rules where id in(Select rule_id from rule_package_versions where package_versions_id = ?)";
        List<Rule> ruleList = jdbcTemplate.query(SQL,new Object[]{batchVersion.getId()},new BeanPropertyRowMapper(Rule.class));

        return ruleList;
    }

    @Override
    public long update(Rule rule) {
        if (rule.getId() < 1){
            throw new IllegalArgumentException("Rule has no id.");
        }
        String SQL = "Update rules set title=?,rule=? where id=?";
        jdbcTemplate.update(SQL,rule.getTitle(),rule.getRule(),rule.getId());
        return rule.getId();
    }

    @Override
    public List<Rule> getAllRules() {
        String SQL = "Select * from rules";
        List<Rule> ruleList = jdbcTemplate.query(SQL,new BeanPropertyRowMapper(Rule.class));

        return ruleList;
    }


    public long save(Rule rule,BatchVersion batchVersion){
        String SQL = "Insert into rules(title,rule) values(?,?)";
        jdbcTemplate.update(SQL,rule.getTitle(),rule.getRule());

        SQL = "Select id from rules where rule = ?";
        long id = jdbcTemplate.queryForLong(SQL,rule.getRule());

//        if (batchVersion.getId() > 0){
//            SQL = "Insert into rule_package_versions(rule_id,package_versions_id) values(?,?)";
//            jdbcTemplate.update(SQL,id,batchVersion.getId());
//        }

        return id;
    }

}

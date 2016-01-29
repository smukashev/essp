package kz.bsbnb.usci.brms.rulesvr.dao.mapper;

import kz.bsbnb.usci.brms.rulemodel.model.impl.Rule;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author abukabayev
 */
public class RuleMapper implements RowMapper<Rule> {
    @Override
    public Rule mapRow(ResultSet resultSet, int i) throws SQLException {
        Rule rule = new Rule();
        rule.setId(resultSet.getLong("id"));
        rule.setRule(resultSet.getString("rule"));
        rule.setTitle(resultSet.getString("title"));
        rule.setIsActive(resultSet.getString("is_active").equals("1"));
        return rule;
    }
}

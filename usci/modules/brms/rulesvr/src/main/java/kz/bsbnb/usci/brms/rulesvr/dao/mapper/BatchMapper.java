package kz.bsbnb.usci.brms.rulesvr.dao.mapper;

import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author abukabayev
 */
public class BatchMapper implements RowMapper<RulePackage> {
    @Override
    public RulePackage mapRow(ResultSet resultSet, int i) throws SQLException {
        RulePackage batch = new RulePackage();
        batch.setId(resultSet.getLong("id"));
        batch.setName(resultSet.getString("name"));
        batch.setRepDate(resultSet.getDate("report_date"));
        batch.setDescription(resultSet.getString("description"));
        return batch;
    }
}

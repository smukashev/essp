package kz.bsbnb.usci.brms.rulesvr.dao.mapper;

import kz.bsbnb.usci.brms.rulemodel.model.impl.Batch;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author abukabayev
 */
public class BatchMapper implements RowMapper<Batch> {
    @Override
    public Batch mapRow(ResultSet resultSet, int i) throws SQLException {
        Batch batch = new Batch();
        batch.setId(resultSet.getLong("id"));
        batch.setName(resultSet.getString("name"));
        batch.setRepDate(resultSet.getDate("report_date"));
        batch.setDescription(resultSet.getString("description"));
        return batch;
    }
}

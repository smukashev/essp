package kz.bsbnb.usci.brms.rulesvr.dao.mapper;

import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
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
        batch.setRepoDate(resultSet.getDate("repdate"));
        batch.setDescription(resultSet.getString("description"));
        return batch;
    }
}

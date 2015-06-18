package kz.bsbnb.usci.brms.rulesvr.dao.mapper;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author abukabayev
 */
public class BatchVersionMapper implements RowMapper<BatchVersion> {
    @Override
    public BatchVersion mapRow(ResultSet resultSet, int i) throws SQLException {
        BatchVersion batchVersion = new BatchVersion();
        batchVersion.setId(resultSet.getLong("id"));
        batchVersion.setOpenDate(resultSet.getTimestamp("report_date"));
        return batchVersion;
    }
}

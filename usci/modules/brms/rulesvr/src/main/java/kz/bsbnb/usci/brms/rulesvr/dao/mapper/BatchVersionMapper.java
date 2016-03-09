package kz.bsbnb.usci.brms.rulesvr.dao.mapper;

import kz.bsbnb.usci.brms.rulemodel.model.impl.PackageVersion;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author abukabayev
 */
public class BatchVersionMapper implements RowMapper<PackageVersion> {
    @Override
    public PackageVersion mapRow(ResultSet resultSet, int i) throws SQLException {
        PackageVersion packageVersion = new PackageVersion();
        packageVersion.setId(resultSet.getLong("id"));
        packageVersion.setOpenDate(resultSet.getTimestamp("report_date"));
        return packageVersion;
    }
}

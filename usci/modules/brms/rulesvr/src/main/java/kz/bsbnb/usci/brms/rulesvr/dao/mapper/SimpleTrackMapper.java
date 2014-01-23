package kz.bsbnb.usci.brms.rulesvr.dao.mapper;

import kz.bsbnb.usci.brms.rulesvr.model.impl.SimpleTrack;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 21.01.14
 * Time: 12:47
 * To change this template use File | Settings | File Templates.
 */
public class SimpleTrackMapper implements RowMapper<SimpleTrack> {
    @Override
    public SimpleTrack mapRow(ResultSet resultSet, int i) throws SQLException {
        return new SimpleTrack(resultSet.getLong("id"), resultSet.getString("name"));
    }
}

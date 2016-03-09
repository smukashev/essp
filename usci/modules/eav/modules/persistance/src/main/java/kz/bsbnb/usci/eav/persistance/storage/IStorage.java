package kz.bsbnb.usci.eav.persistance.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public interface IStorage {
    void initialize();

    void clear();

    void empty();

    boolean isClean();

    boolean testConnection();

    HashMap<String, Long> tableCounts();

    boolean simpleSql(String sql);

    public Connection getConnection() throws SQLException;
}
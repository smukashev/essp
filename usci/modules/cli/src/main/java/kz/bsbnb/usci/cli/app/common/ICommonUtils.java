package kz.bsbnb.usci.cli.app.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Bauyrzhan.Makhambeto on 17/03/2015.
 */
public interface ICommonUtils {
    public Connection connectToDB(String url, String name, String password)
            throws ClassNotFoundException, SQLException;
}

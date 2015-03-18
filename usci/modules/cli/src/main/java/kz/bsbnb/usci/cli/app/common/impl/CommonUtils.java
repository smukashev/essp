package kz.bsbnb.usci.cli.app.common.impl;

import kz.bsbnb.usci.cli.app.common.ICommonUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Bauyrzhan.Makhambeto on 17/03/2015.
 */
@Component
public class CommonUtils implements ICommonUtils {
    public Connection connectToDB(String url, String name, String password)
            throws ClassNotFoundException, SQLException
    {
        Class.forName("oracle.jdbc.OracleDriver");
        return DriverManager.getConnection(url, name, password);
    }
}

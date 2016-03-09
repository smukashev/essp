package kz.bsbnb.usci.cli.app.common.impl;

/**
 * Created by baur on 05.03.16.
 */
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import sun.nio.cs.KOI8_U;


public class SqlRunner {
    private final boolean autoCommit;
    private final Connection connection;

    public SqlRunner(final Connection connection, final boolean autoCommit) {
        if (connection == null) {
            throw new RuntimeException("SqlRunner requires an SQL Connection");
        }
        this.connection = connection;
        this.autoCommit = autoCommit;
    }
    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public boolean runScript(final String FilePath) throws SQLException {
        final boolean originalAutoCommit = this.connection.getAutoCommit();
        try {
            if (originalAutoCommit != this.autoCommit) {
                this.connection.setAutoCommit(this.autoCommit);
            }
            Charset charset = new KOI8_U();
            String script = readFile(FilePath, charset);
            CallableStatement cs = connection.prepareCall(script);
            cs.execute();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.connection.setAutoCommit(originalAutoCommit);
        }
        return true;
    }
}
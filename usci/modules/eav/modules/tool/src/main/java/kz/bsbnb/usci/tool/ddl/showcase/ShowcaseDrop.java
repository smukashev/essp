package kz.bsbnb.usci.tool.ddl.showcase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by bauka on 9/29/16.
 */
public class ShowcaseDrop {
    public static void main(String [] args){

        try {
            Class.forName("oracle.jdbc.OracleDriver");

            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl","showcase","showcase");
            Statement stmt = conn.createStatement();
            Statement ddlStmnt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("select * from user_tables where TABLE_NAME  like 'R_%'");

            int deleted = 0;

            while (rs.next()) {
                ddlStmnt.executeQuery("drop table " + rs.getString("TABLE_NAME"));
                deleted ++;
            }

            System.out.println(deleted + " R_ like tables dropped");

            stmt.close();
            ddlStmnt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

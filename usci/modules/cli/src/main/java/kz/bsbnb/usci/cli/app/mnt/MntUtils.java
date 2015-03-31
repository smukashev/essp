package kz.bsbnb.usci.cli.app.mnt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 16/03/2015.
 */
public class MntUtils {
    public static boolean updateMntStatus(Connection connection, String script, List<Long> listSuccess){
        if (listSuccess.size() > 0) {
            int start = 0, nextStart;
            while(start < listSuccess.size()) {
                nextStart = start + 100;
                StringBuilder sql;
                if (script.equals("delScript"))
                    sql = new StringBuilder("update MAINTENANCE.CREDREG_DELETE_CREDIT SET PROCESSED_USCI = 1 where ID IN ( ?");
                else
                    sql = new StringBuilder("update MAINTENANCE.CREDREG_EDIT_CREDIT SET PROCESSED_USCI = 1 where ID IN ( ?");

                for (int i=start + 1; i < Math.min(nextStart, listSuccess.size()); i++)
                    sql.append(",?");

                sql.append(")");

                try {
                    PreparedStatement statement = connection.prepareStatement(sql.toString());
                    //System.out.println(sql.toString());
                    for (int i = start; i < Math.min(nextStart, listSuccess.size() ); i++)
                        statement.setLong(i + 1 - start, listSuccess.get(i));
                    statement.executeQuery();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
                start = nextStart;
            }

        } else {
            System.out.println("warning: empty success list");
        }

        return true;
    }

}

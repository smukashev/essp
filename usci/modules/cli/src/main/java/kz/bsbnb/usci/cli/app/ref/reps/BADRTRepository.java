package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.BADRT;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Bauyrzhan.Makhambeto on 13/06/2015.
 */
public class BADRTRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    //private static String QUERY = "SELECT * FROM ref.ba_ct t" + " where t.open_date <= to_date('" + repDate + "', 'dd.MM.yyyy') \n"+
    //         "   and (t.close_date > to_date('" + repDate + "', 'dd.MM.yyyy') or t.close_date is null)";

    private static String QUERY = "select ba.no_, sh.code\n" +
            "  from ref.ba_drt ba_drt,\n" +
            "       ref.balance_account ba,\n" +
            "       ref.shared sh\n" +
            "       where ba_drt.balance_account_id = ba.id\n" +
            "         and ba_drt.debt_remains_type_id = sh.id\n" +
            "         and ba_drt.open_date <= to_date('"+repDate+"','dd.MM.yyyy')\n" +
            "         and (ba_drt.close_date >= to_date('"+repDate+"','dd.MM.yyyy') or ba_drt.close_date is null)";


    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='BA_CT'";

    public static HashMap getRepository() {
        if(repository ==null)
            repository = construct();
        return repository;
    }

    public static HashMap construct(){
        try {
            ResultSet rows = getStatement().executeQuery(QUERY);

            HashMap hm = new HashMap();
            int i = 0;
            while(rows.next()){
                HashMap tmp = new HashMap();

                if(rows.getString("NO_") == null || rows.getString("CODE") == null)
                    continue;

                tmp.put("NO_", rows.getString("NO_"));
                tmp.put("CODE", rows.getString("CODE"));
                BADRT bact = new BADRT(tmp);

                hm.put(i++,bact);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BADRT getById(String id){
        throw new RuntimeException("badrt not searchable!!!");
    }
}

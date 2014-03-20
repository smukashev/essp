
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.EconTrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EconTradeRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.ECON_TRADE";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='ECON_TRADE'";

    public static HashMap getRepository() {
        if(repository ==null)
            repository = construct();
        return repository;
    }

    public static HashMap construct(){
        try {
            ResultSet rows = getStatement().executeQuery(QUERY);

            HashMap hm = new HashMap();
            while(rows.next()){
                HashSet hs = getColumns();
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                EconTrade dt = new EconTrade(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EconTrade[] getByProperty(String key,String value){
        EconTrade [] ret = new EconTrade[0];
        List<EconTrade> list = new ArrayList<EconTrade>();
        for(Object v: getRepository().values()){
            if(((EconTrade) v).get(key) != null)
                if(((EconTrade) v).get(key).equals(value)) list.add((EconTrade)v);
        }
        return list.toArray(ret);
    }

    public static EconTrade getById(String id){
        return (EconTrade) getRepository().get(id);
    }

    public static HashSet getColumns() {
        try {
            if(columns ==null){
                ResultSet rows = getStatement().executeQuery(COLUMNS_QUERY);
                HashSet hs = new HashSet();
                while(rows.next()){
                    hs.add(rows.getString("column_name"));
                }
                return columns = hs;
            }
            return columns;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

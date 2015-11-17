
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
    private static String QUERY = "SELECT * FROM ref.ECON_TRADE t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='ECON_TRADE'";

    public HashMap construct(){
        try {
            HashSet hs = getColumns();
            ResultSet rows = getStatement().executeQuery(QUERY.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
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

    public EconTrade[] getByProperty(String key,String value){
        EconTrade [] ret = new EconTrade[0];
        List<EconTrade> list = new ArrayList<EconTrade>();
        for(Object v: getRepository().values()){
            if(((EconTrade) v).get(key) != null)
                if(((EconTrade) v).get(key).equals(value)) list.add((EconTrade)v);
        }
        return list.toArray(ret);
    }

    public EconTrade getById(String id){
        return (EconTrade) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}


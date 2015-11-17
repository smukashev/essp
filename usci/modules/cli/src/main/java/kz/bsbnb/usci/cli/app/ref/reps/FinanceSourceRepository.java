
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.FinanceSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FinanceSourceRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.FINANCE_SOURCE t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='FINANCE_SOURCE'";

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
                FinanceSource dt = new FinanceSource(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public FinanceSource[] getByProperty(String key,String value){
        FinanceSource [] ret = new FinanceSource[0];
        List<FinanceSource> list = new ArrayList<FinanceSource>();
        for(Object v: getRepository().values()){
            if(((FinanceSource) v).get(key) != null)
                if(((FinanceSource) v).get(key).equals(value)) list.add((FinanceSource)v);
        }
        return list.toArray(ret);
    }

    public FinanceSource getById(String id){
        return (FinanceSource) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}


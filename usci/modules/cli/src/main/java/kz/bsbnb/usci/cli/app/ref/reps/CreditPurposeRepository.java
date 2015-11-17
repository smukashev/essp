
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.CreditPurpose;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CreditPurposeRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.CREDIT_PURPOSE t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDIT_PURPOSE'";*/


    public CreditPurposeRepository() {
        QUERY_ALL = "SELECT * FROM ref.credit_purpose";
        QUERY_OPEN = "SELECT * FROM ref.credit_purpose where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null)";
        QUERY_CLOSE = "SELECT * FROM ref.%s where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDIT_PURPOSE'";
    }

    @Override
    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();
            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                CreditPurpose dt = new CreditPurpose(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CreditPurpose[] getByProperty(String key,String value){
        CreditPurpose [] ret = new CreditPurpose[0];
        List<CreditPurpose> list = new ArrayList<CreditPurpose>();
        for(Object v: getRepository().values()){
            if(((CreditPurpose) v).get(key) != null)
                if(((CreditPurpose) v).get(key).equals(value)) list.add((CreditPurpose)v);
        }
        return list.toArray(ret);
    }

    public CreditPurpose getById(String id){
        return (CreditPurpose) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}


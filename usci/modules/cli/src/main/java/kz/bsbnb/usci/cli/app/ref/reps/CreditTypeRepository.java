
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.CreditType;
import kz.bsbnb.usci.cli.app.ref.refs.Shared;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CreditTypeRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.CREDIT_TYPE";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDIT_TYPE'";

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
                tmp.put("shared",SharedRepository.getById((String) tmp.get("KIND_ID")));
                tmp.put("debtor_type",DebtorTypeRepository.getByCode((String) tmp.get("CODE")));

                //System.out.println( ((Shared)SharedRepository.getById((String) tmp.get("KIND_ID")) ).get("CODE") );
                CreditType dt = new CreditType(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CreditType[] getByProperty(String key,String value){
        CreditType [] ret = new CreditType[0];
        List<CreditType> list = new ArrayList<CreditType>();
        for(Object v: getRepository().values()){
            if(((CreditType) v).get(key) != null)
                if(((CreditType) v).get(key).equals(value)) list.add((CreditType)v);
        }
        return list.toArray(ret);
    }

    public static CreditType getById(String id){
        return (CreditType) getRepository().get(id);
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

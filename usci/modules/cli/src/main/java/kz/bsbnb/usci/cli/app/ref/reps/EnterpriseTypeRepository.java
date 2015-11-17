
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.EnterpriseType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EnterpriseTypeRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.ENTERPRISE_TYPE t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='ENTERPRISE_TYPE'";*/


    public EnterpriseTypeRepository() {
        QUERY_ALL = "SELECT * FROM ref.enterprise_type";
        QUERY_OPEN = "SELECT * FROM ref.enterprise_type where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null)";
        QUERY_CLOSE = "SELECT * FROM ref.enterprise_type where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='ENTERPRISE_TYPE'";
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
                EnterpriseType dt = new EnterpriseType(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public EnterpriseType[] getByProperty(String key,String value){
        EnterpriseType [] ret = new EnterpriseType[0];
        List<EnterpriseType> list = new ArrayList<EnterpriseType>();
        for(Object v: getRepository().values()){
            if(((EnterpriseType) v).get(key) != null)
                if(((EnterpriseType) v).get(key).equals(value)) list.add((EnterpriseType)v);
        }
        return list.toArray(ret);
    }

    public EnterpriseType getById(String id){
        return (EnterpriseType) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}


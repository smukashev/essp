
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.PledgeType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PledgeTypeRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.PLEDGE_TYPE t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='PLEDGE_TYPE'";

    /*
    public static HashMap getRepository() {
        if(repository==null)
            repository = construct();
        return repository;
    }*/

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
                PledgeType dt = new PledgeType(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PledgeType[] getByProperty(String key,String value){
        PledgeType [] ret = new PledgeType[0];
        List<PledgeType> list = new ArrayList<PledgeType>();
        for(Object v: getRepository().values()){
            if(((PledgeType) v).get(key) != null)
                if(((PledgeType) v).get(key).equals(value)) list.add((PledgeType)v);
        }
        return list.toArray(ret);
    }

    public PledgeType getById(String id){
        return (PledgeType) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}


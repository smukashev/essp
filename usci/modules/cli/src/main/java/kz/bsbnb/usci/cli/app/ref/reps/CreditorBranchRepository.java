
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.CreditorBranch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CreditorBranchRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT c.* FROM ref.v_CREDITOR_his t, ref.creditor c WHERE c.id = t.id"  + " and t.open_date <= to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR'";

    public static HashMap getRepository() {
        if(repository ==null)
            repository = construct();
        return repository;
    }

    public static HashMap construct(){
        try {
            ResultSet rows = getStatement().executeQuery(QUERY.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashSet hs = getColumns();
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                tmp.put("docs",CreditorDocRepository.getByProperty("CREDITOR_ID",(String) tmp.get("ID")));
                if (tmp.get("MAIN_OFFICE_ID") == null) {
                    tmp.put("main_office",CreditorRepository.getById((String) tmp.get("ID")));
                } else {
                    tmp.put("main_office",CreditorRepository.getById((String) tmp.get("MAIN_OFFICE_ID")));
                }

                if(tmp.get("REGION_ID") != null)
                    tmp.put("region",RegionRepository.getById((String)tmp.get("REGION_ID")));
                CreditorBranch dt = new CreditorBranch(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CreditorBranch[] getByProperty(String key,String value){
        CreditorBranch [] ret = new CreditorBranch[0];
        List<CreditorBranch> list = new ArrayList<CreditorBranch>();
        for(Object v: getRepository().values()){
            if(((CreditorBranch) v).get(key) != null)
                if(((CreditorBranch) v).get(key).equals(value)) list.add((CreditorBranch)v);
        }
        return list.toArray(ret);
    }

    public static CreditorBranch getById(String id){
        return (CreditorBranch) getRepository().get(id);
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

    public static void rc(){
        repository = null;
    }
}


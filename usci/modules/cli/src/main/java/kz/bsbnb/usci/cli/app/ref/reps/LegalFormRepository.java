
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.LegalForm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LegalFormRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.LEGAL_FORM t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='LEGAL_FORM'";

    public static HashMap getRepository() {
        if(BaseRepository.closeMode) QUERY = BaseRepository.QUERY;if(repository==null)
            repository = construct();
        return repository;
    }

    public static HashMap construct(){
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
                LegalForm dt = new LegalForm(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LegalForm[] getByProperty(String key,String value){
        LegalForm [] ret = new LegalForm[0];
        List<LegalForm> list = new ArrayList<LegalForm>();
        for(Object v: getRepository().values()){
            if(((LegalForm) v).get(key) != null)
                if(((LegalForm) v).get(key).equals(value)) list.add((LegalForm)v);
        }
        return list.toArray(ret);
    }

    public static LegalForm getById(String id){
        return (LegalForm) getRepository().get(id);
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

